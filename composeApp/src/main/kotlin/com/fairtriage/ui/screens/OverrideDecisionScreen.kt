package com.fairtriage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.fairtriage.screenmodel.OverrideDecisionScreenModel
import com.fairtriage.screenmodel.SubmitState
import com.fairtriage.ui.components.*

data class OverrideDecisionScreen(
    private val patientId: Int,
    private val currentTriageLevel: String
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { OverrideDecisionScreenModel() }
        val actionState by screenModel.submitState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }

        var selectedReasons by remember { mutableStateOf(setOf<String>()) }
        var showErrors by remember { mutableStateOf(false) }
        val reasonGroups = remember {
            listOf(
                OverrideReasonGroup(
                    title = "Immediate clinical concern",
                    options = listOf(
                        "Severe pain escalation",
                        "Chest pain or cardiac concern",
                        "Respiratory distress concern",
                        "Neurologic red flag",
                        "Clinical deterioration observed"
                    )
                ),
                OverrideReasonGroup(
                    title = "Abnormal measurements",
                    options = listOf(
                        "Abnormal heart rate",
                        "Abnormal blood pressure",
                        "Persistent fever or infection concern",
                        "Pain level increased or uncontrolled"
                    )
                ),
                OverrideReasonGroup(
                    title = "Risk context",
                    options = listOf(
                        "High-risk chronic disease",
                        "Age-related frailty risk",
                        "Waiting time increases urgency",
                        "AI score underestimates bedside risk"
                    )
                ),
                OverrideReasonGroup(
                    title = "Clinical judgement",
                    options = listOf(
                        "Doctor or nurse bedside assessment",
                        "Patient safety precaution",
                        "Symptoms suggest clinical deterioration",
                        "Requires faster physician review"
                    )
                )
            )
        }

        LaunchedEffect(actionState) {
            when (val currentAction = actionState) {
                is SubmitState.Success -> navigator.pop()
                is SubmitState.Error -> {
                    snackbarHostState.showSnackbar(currentAction.message)
                    screenModel.clearError()
                }
                else -> Unit
            }
        }

        Scaffold(
            topBar = {
                FairTriageTopBar(title = "Override decision", onBack = { navigator.pop() })
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = FairColors.ScreenBg
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // WARNING CARD
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = FairColors.WarningBg),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFFEF08A))
                ) {
                    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                        Box(modifier = Modifier.width(3.dp).fillMaxHeight().background(FairColors.WarningBorder))
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = FairColors.WarningBorder, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("You are overriding an AI decision", style = FairTypography.BodyMedium.copy(fontWeight = FontWeight.Medium), color = FairColors.WarningText)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("This action will be logged for compliance and audit.", style = FairTypography.LabelSmall, color = Color(0xFFA16207))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Current triage level", style = FairTypography.LabelLarge, color = Color(0xFF64748B), modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Spacer(modifier = Modifier.height(6.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TriageBadge(level = currentTriageLevel, large = true)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = FairColors.InfoBlueBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FairColors.InfoBlueBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Backend will determine the safest triage level",
                            style = FairTypography.BodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = FairColors.InfoBlueDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Select the bedside clinical reasons. FairTriage will re-score the case and log the resulting override level automatically.",
                            style = FairTypography.LabelSmall,
                            color = FairColors.TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Select clinical reason(s)", style = FairTypography.LabelLarge, color = Color(0xFF64748B))
                    Text("${selectedReasons.size} selected", style = FairTypography.LabelSmall, color = FairColors.TextHint)
                }
                Spacer(modifier = Modifier.height(8.dp))
                reasonGroups.forEach { group ->
                    Text(
                        text = group.title,
                        style = FairTypography.LabelLarge,
                        color = FairColors.InfoBlueDark,
                        modifier = Modifier.padding(top = 8.dp, bottom = 6.dp)
                    )
                    group.options.forEach { option ->
                        ReasonOptionCard(
                            text = option,
                            selected = option in selectedReasons,
                            onClick = {
                                selectedReasons = if (option in selectedReasons) {
                                    selectedReasons - option
                                } else {
                                    selectedReasons + option
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                val isReasonError = showErrors && selectedReasons.isEmpty()
                if (isReasonError) {
                    Text("Select at least one clinical reason", color = FairColors.DangerRed, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                val isConfirmEnabled = selectedReasons.isNotEmpty()
                Button(
                    onClick = {
                        showErrors = true
                        if (isConfirmEnabled) {
                            val reasons = reasonGroups.flatMap { it.options }.filter { it in selectedReasons }
                            screenModel.submit(
                                patientId,
                                com.fairtriage.model.OverrideRequest(
                                    override_reasons = reasons,
                                    override_reason = reasons.joinToString("; ")
                                )
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isConfirmEnabled) FairColors.DangerRed else FairColors.Border,
                        contentColor = if (isConfirmEnabled) Color.White else FairColors.TextHint
                    )
                ) {
                    if (actionState == SubmitState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Text("Confirm override", style = FairTypography.BodyLarge.copy(fontWeight = FontWeight.Medium))
                    }
                }
                
                DisclaimerText()
            }
        }
    }

    private data class OverrideReasonGroup(
        val title: String,
        val options: List<String>
    )

    @Composable
    private fun ReasonOptionCard(text: String, selected: Boolean, onClick: () -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .background(if (selected) FairColors.InfoBlueBg else Color.White, RoundedCornerShape(12.dp))
                .border(1.dp, if (selected) FairColors.InfoBlueBorder else FairColors.Border, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(if (selected) FairColors.InfoBlueText else Color.White, RoundedCornerShape(7.dp))
                    .border(1.dp, if (selected) FairColors.InfoBlueText else FairColors.Border, RoundedCornerShape(7.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                color = FairColors.TextPrimary,
                style = FairTypography.BodyMedium.copy(fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal)
            )
        }
    }
}
