package com.fairtriage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.fairtriage.core.ScreenState
import com.fairtriage.model.Patient
import com.fairtriage.screenmodel.PatientActionState
import com.fairtriage.screenmodel.PatientDetailScreenModel
import com.fairtriage.ui.components.DisclaimerText
import com.fairtriage.ui.components.ErrorState
import com.fairtriage.ui.components.FairColors
import com.fairtriage.ui.components.FairTriageTopBar
import com.fairtriage.ui.components.FairTypography
import com.fairtriage.ui.components.LoadingState
import com.fairtriage.ui.components.SectionCardTitle
import com.fairtriage.ui.components.StandardCard
import com.fairtriage.ui.components.TriageBadge
import com.fairtriage.ui.components.formatScore
import com.fairtriage.ui.components.formatWaitFactor
import com.fairtriage.ui.components.triageFill
import com.fairtriage.ui.components.triageTint

data class PatientDetailScreen(private val patientId: Int) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { PatientDetailScreenModel() }
        val lifecycleOwner = LocalLifecycleOwner.current
        val state by screenModel.state.collectAsState()
        val actionState by screenModel.actionState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }

        DisposableEffect(lifecycleOwner, patientId, screenModel) {
            screenModel.load(patientId)
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    screenModel.load(patientId)
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        LaunchedEffect(actionState) {
            when (val currentAction = actionState) {
                PatientActionState.Completed -> navigator.pop()
                is PatientActionState.Error -> {
                    snackbarHostState.showSnackbar(currentAction.message)
                    screenModel.clearActionError()
                }
                else -> Unit
            }
        }

        Scaffold(
            topBar = {
                FairTriageTopBar(title = "Patient Detail", onBack = { navigator.pop() })
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = FairColors.ScreenBg
        ) { paddingValues ->
            when (val currentState = state) {
                is ScreenState.Loading -> LoadingState(modifier = Modifier.padding(paddingValues))
                is ScreenState.Error -> ErrorState(
                    errorMessage = currentState.message,
                    onRetry = { screenModel.load(patientId) },
                    modifier = Modifier.padding(paddingValues)
                )
                is ScreenState.Success -> PatientDetailContent(
                    patient = currentState.data,
                    completing = actionState == PatientActionState.Loading,
                    onOverride = {
                        navigator.push(
                            AddPatientScreen(
                                patientId = currentState.data.id,
                                overrideMode = true
                            )
                        )
                    },
                    onComplete = { screenModel.complete(currentState.data.id) },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun PatientDetailContent(
    patient: Patient,
    completing: Boolean,
    onOverride: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val level = patient.triage_level ?: "Stable"
    val accent = triageFill(level)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FairColors.NavyDark)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TriageBadge(level = level, large = true)
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = patient.full_name, style = FairTypography.TitleLarge, color = FairColors.NavyText)

                val chronicText = if (!patient.chronic_disease_description.isNullOrBlank()) {
                    " - ${patient.chronic_disease_description}"
                } else {
                    ""
                }
                Text(
                    text = "Queue #${patient.queue_position ?: "-"} - Age ${patient.age} - ${patient.gender}$chronicText",
                    style = FairTypography.LabelSmall,
                    color = Color(0x7394D2EC),
                    modifier = Modifier.padding(top = 5.dp),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Waiting ${formatMinutes(patient.waiting_minutes)}" + (patient.max_waiting_minutes?.let { " / max $it min" } ?: ""),
                    style = FairTypography.LabelSmall,
                    color = if (patient.max_waiting_exceeded) FairColors.WarningBorder else Color(0x7394D2EC),
                    modifier = Modifier.padding(top = 4.dp),
                    textAlign = TextAlign.Center
                )

                if (patient.overridden_by_doctor) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0x337C3AED), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Doctor override applied", fontSize = 10.sp, color = Color(0xFFA78BFA))
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StandardCard {
                    SectionCardTitle("Vital signs", Icons.Default.Favorite)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        VitalItem("Heart rate", "${patient.heart_rate} bpm", patient.heart_rate > 100, Modifier.weight(1f))
                        VitalItem(
                            "Blood pressure",
                            "${patient.blood_pressure_systolic}/${patient.blood_pressure_diastolic}",
                            patient.blood_pressure_systolic !in 90..140,
                            Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        VitalItem("Pain level", "${patient.pain_level}/10", patient.pain_level >= 7, Modifier.weight(1f))
                        VitalItem("Fever", if (patient.fever) "Yes" else "No", patient.fever, Modifier.weight(1f))
                    }
                }

                StandardCard {
                    SectionCardTitle("AI risk analysis", Icons.Default.Psychology)

                    ScoreRow("Symptom score", formatScore(patient.symptom_score))
                    SoftDivider()
                    ScoreRow("Image score", formatScore(patient.image_score))
                    SoftDivider()
                    ScoreRow("History score", formatScore(patient.history_score))
                    SoftDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Clinical risk score", style = FairTypography.BodyLarge)
                        Text(formatScore(patient.clinical_risk_score), style = FairTypography.ScoreBig, color = accent)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { ((patient.clinical_risk_score ?: 0.0) / 100.0).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = accent,
                        trackColor = triageTint(level),
                        drawStopIndicator = {}
                    )
                    SoftDivider()

                    ScoreRow("Waiting time factor", formatWaitFactor(patient.waiting_time_factor))
                    SoftDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Final priority score", style = FairTypography.BodyLarge.copy(fontWeight = FontWeight.Medium))
                        Text(formatScore(patient.final_priority_score), style = FairTypography.ScoreFinal, color = accent)
                    }
                }

                DecisionRationaleCard(patient)
                ProductLogicCard(patient)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = onOverride,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FairColors.Surface,
                            contentColor = FairColors.TextSecondary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Override", style = FairTypography.BodyMedium.copy(fontWeight = FontWeight.Medium))
                    }

                    Button(
                        onClick = onComplete,
                        modifier = Modifier.weight(1f).height(48.dp),
                        enabled = !completing,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FairColors.ActionTeal, contentColor = Color.White)
                    ) {
                        if (completing) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp).padding(end = 4.dp))
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mark completed", style = FairTypography.BodyMedium.copy(fontWeight = FontWeight.Medium))
                    }
                }

                DisclaimerText()
            }
        }
    }
}

@Composable
private fun DecisionRationaleCard(patient: Patient) {
    val explanationBullets = buildExplanationBullets(patient)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = FairColors.InfoBlueBg),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, FairColors.InfoBlueBorder)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(FairColors.InfoBlueText))
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = FairColors.InfoBlueText, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "AI DECISION RATIONALE",
                        style = FairTypography.LabelSmall.copy(fontWeight = FontWeight.Medium),
                        color = FairColors.InfoBlueText
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = patient.decision_rationale ?: "No rationale provided.",
                    fontSize = 12.sp,
                    color = FairColors.InfoBlueDark,
                    fontStyle = FontStyle.Italic,
                    lineHeight = 18.sp
                )

                if (explanationBullets.isNotEmpty()) {
                    HorizontalDivider(color = FairColors.InfoBlueBorder, modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        "WHY THIS TRIAGE?",
                        style = FairTypography.LabelSmall.copy(fontWeight = FontWeight.Medium),
                        color = FairColors.InfoBlueText
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    explanationBullets.forEach { bullet ->
                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                            Text("•", color = FairColors.InfoBlueText, fontSize = 13.sp, modifier = Modifier.padding(end = 6.dp))
                            Text(
                                text = bullet,
                                color = FairColors.InfoBlueDark,
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                if (patient.overridden_by_doctor && !patient.doctor_override_reason.isNullOrEmpty()) {
                    HorizontalDivider(color = FairColors.InfoBlueBorder, modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = "Doctor override reason: ${patient.doctor_override_reason}",
                        fontSize = 12.sp,
                        color = FairColors.PurpleBadge,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductLogicCard(patient: Patient) {
    StandardCard {
        SectionCardTitle("FairTriage model policy", Icons.Default.Info)
        ProductLogicRow("Hybrid scoring", "Risk = 45% symptoms + 25% image + 30% history")
        SoftDivider()
        ProductLogicRow("Safety rules", "Critical indicators can only escalate priority, never downgrade it")
        SoftDivider()
        ProductLogicRow("Fairness", patient.queue_policy_summary ?: "Waiting time increases final priority and activates a max-waiting review boost")
        SoftDivider()
        ProductLogicRow("Doctor control", if (patient.overridden_by_doctor) "Doctor override is active and logged" else "Doctor can override with a required clinical reason")
    }
}

@Composable
private fun ProductLogicRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = FairTypography.BodyMedium.copy(fontWeight = FontWeight.Medium), color = FairColors.TextPrimary)
        Text(
            value,
            style = FairTypography.LabelSmall,
            color = FairColors.TextSecondary,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f).padding(start = 12.dp)
        )
    }
}

private fun buildExplanationBullets(patient: Patient): List<String> {
    val bullets = mutableListOf<String>()
    val level = patient.triage_level ?: "Stable"
    val risk = formatScore(patient.clinical_risk_score)
    val priority = formatScore(patient.final_priority_score)
    bullets += "$level assigned from clinical risk score $risk and final priority score $priority."

    if (patient.heart_rate > 130) {
        bullets += "Heart rate above 130 bpm triggered the safety escalation rule."
    } else if (patient.heart_rate > 100) {
        bullets += "Elevated heart rate increased symptom severity."
    }

    if (patient.blood_pressure_systolic < 90) {
        bullets += "Systolic blood pressure below 90 mmHg triggered Critical safety protection."
    } else if (patient.blood_pressure_systolic > 140) {
        bullets += "Elevated systolic blood pressure contributed to symptom risk."
    }

    if (patient.pain_level >= 9 && patient.fever) {
        bullets += "Pain level ${patient.pain_level}/10 with fever triggered an Urgent safety rule."
    } else if (patient.pain_level >= 7) {
        bullets += "High pain level contributed to the symptom score."
    }

    if (patient.fever) {
        bullets += "Fever increased the symptom risk score."
    }

    if (patient.has_chronic_disease) {
        bullets += "Medical history increased risk because chronic disease was selected."
    }

    val waitFactor = patient.waiting_time_factor ?: 0.0
    when {
        patient.max_waiting_exceeded -> bullets += "Maximum waiting-time constraint is active, so fairness boost is applied within this triage group."
        waitFactor > 0.0 -> bullets += "Waiting time added ${formatWaitFactor(waitFactor)} to prevent unfair delay."
    }

    if (patient.overridden_by_doctor) {
        bullets += "Doctor override is active; this decision is stored in the audit log."
    }

    return bullets.take(7)
}

private fun formatMinutes(value: Double?): String {
    if (value == null) return "-- min"
    return "${value.toInt()} min"
}

@Composable
private fun ScoreRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = FairTypography.BodyLarge)
        Text(value, style = FairTypography.ScoreNormal, color = FairColors.TextPrimary)
    }
}

@Composable
private fun SoftDivider() {
    HorizontalDivider(color = FairColors.Divider, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
}

@Composable
private fun VitalItem(label: String, value: String, abnormal: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(FairColors.ScreenBg, RoundedCornerShape(10.dp))
            .border(0.5.dp, FairColors.Border, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(label, style = FairTypography.Caption, color = FairColors.TextHint)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = if (abnormal) FairColors.DangerRed else FairColors.TextPrimary
        )
    }
}
