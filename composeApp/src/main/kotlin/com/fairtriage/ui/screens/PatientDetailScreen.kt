package com.fairtriage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.fairtriage.core.ScreenState
import com.fairtriage.model.Patient
import com.fairtriage.screenmodel.PatientActionState
import com.fairtriage.screenmodel.PatientDetailScreenModel
import com.fairtriage.ui.components.AppBarStyle
import com.fairtriage.ui.components.ClinicalCard
import com.fairtriage.ui.components.ErrorState
import com.fairtriage.ui.components.FairColors
import com.fairtriage.ui.components.LoadingState
import com.fairtriage.ui.components.ScoreBreakdownCard
import com.fairtriage.ui.components.ScreenScaffold
import com.fairtriage.ui.components.TriageBadge
import com.fairtriage.ui.components.triageColor
import com.fairtriage.ui.components.triageTint

data class PatientDetailScreen(private val patientId: Int) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val snackbarHostState = remember { SnackbarHostState() }
        val screenModel = rememberScreenModel { PatientDetailScreenModel() }
        val state by screenModel.state.collectAsState()
        val actionState by screenModel.actionState.collectAsState()
        val patientName = (state as? ScreenState.Success)?.data?.full_name ?: "Patient Detail"

        LaunchedEffect(patientId) {
            screenModel.load(patientId)
        }

        LaunchedEffect(actionState) {
            when (val currentAction = actionState) {
                PatientActionState.Completed -> navigator.pop()
                is PatientActionState.Error -> {
                    snackbarHostState.showSnackbar(currentAction.message)
                    screenModel.clearActionError()
                }
                PatientActionState.Idle,
                PatientActionState.Loading -> Unit
            }
        }

        ScreenScaffold(
            title = patientName,
            showBack = true,
            appBarStyle = AppBarStyle.White,
            snackbarHostState = snackbarHostState
        ) { paddingValues ->
            when (val currentState = state) {
                ScreenState.Loading -> LoadingState(modifier = Modifier.padding(paddingValues))
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
                            OverrideDecisionScreen(
                                patientId = currentState.data.id,
                                currentTriageLevel = currentState.data.triage_level ?: "Stable"
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FairColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HeroCard(patient)
        VitalSignsCard(patient)
        ScoreBreakdownCard(patient = patient)
        DecisionRationaleCard(patient)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onOverride,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Override Decision", color = FairColors.PrimaryBlue, fontSize = 13.sp)
            }
            Button(
                onClick = onComplete,
                modifier = Modifier.weight(1f).height(52.dp),
                enabled = !completing,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FairColors.SecondaryTeal,
                    contentColor = Color.White
                )
            ) {
                if (completing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp).padding(end = 4.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                }
                Text("Mark as Completed", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun HeroCard(patient: Patient) {
    ClinicalCard(containerColor = triageTint(patient.triage_level)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            TriageBadge(level = patient.triage_level)
            Text(
                text = patient.full_name,
                color = FairColors.TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "Queue Position #${patient.queue_position ?: "-"}",
                color = FairColors.TextSecondary,
                fontSize = 12.sp
            )
            if (patient.overridden_by_doctor) {
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .background(Color(0xFF6A1B9A), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Doctor Override Applied",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun VitalSignsCard(patient: Patient) {
    ClinicalCard {
        Text(
            text = "VITAL SIGNS",
            color = FairColors.TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            VitalItem("HR", "${patient.heart_rate}", "Heart Rate", patient.heart_rate > 100, Modifier.weight(1f))
            VitalItem("BP", "${patient.blood_pressure_systolic}/${patient.blood_pressure_diastolic}", "Blood Pressure", patient.blood_pressure_systolic < 90 || patient.blood_pressure_systolic > 160, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            VitalItem("P", patient.pain_level.toString(), "Pain Level", patient.pain_level >= 7, Modifier.weight(1f))
            VitalItem("F", if (patient.fever) "Yes" else "No", "Fever", patient.fever, Modifier.weight(1f))
        }
    }
}

@Composable
private fun VitalItem(
    icon: String,
    value: String,
    label: String,
    abnormal: Boolean,
    modifier: Modifier = Modifier
) {
    val color = if (abnormal) FairColors.CriticalRed else FairColors.SecondaryTeal
    Row(
        modifier = modifier
            .background(FairColors.Background, RoundedCornerShape(10.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Column {
            Text(value, color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(label, color = FairColors.TextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
private fun DecisionRationaleCard(patient: Patient) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .height(IntrinsicSize.Min)
            .background(Color(0xFFE3F2FD), RoundedCornerShape(12.dp))
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(FairColors.PrimaryBlue)
        )
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("i", color = FairColors.PrimaryBlue, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "AI Decision Rationale",
                    color = FairColors.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = patient.decision_rationale ?: "No rationale returned by backend.",
                color = FairColors.TextPrimary,
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic
            )
            if (patient.overridden_by_doctor && patient.doctor_override_reason != null) {
                Text(
                    text = "Doctor Override Reason: ${patient.doctor_override_reason}",
                    color = Color(0xFF6A1B9A),
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}
