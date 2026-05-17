package com.fairtriage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

        var selectedLevel by remember { mutableStateOf(currentTriageLevel.ifBlank { "Stable" }) }
        var reason by remember { mutableStateOf("") }
        var showErrors by remember { mutableStateOf(false) }

        LaunchedEffect(actionState) {
            when (val currentAction = actionState) {
                SubmitState.Success -> {
                    snackbarHostState.showSnackbar("Override applied")
                    navigator.pop()
                }
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
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)
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
                Text("Select new triage level", style = FairTypography.LabelLarge, color = Color(0xFF64748B), modifier = Modifier.padding(bottom = 10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    LevelSelectorCard("Critical", selectedLevel, FairColors.CriticalFill, Modifier.weight(1f)) { selectedLevel = "Critical" }
                    LevelSelectorCard("Urgent", selectedLevel, FairColors.UrgentFill, Modifier.weight(1f)) { selectedLevel = "Urgent" }
                    LevelSelectorCard("Stable", selectedLevel, FairColors.StableFill, Modifier.weight(1f)) { selectedLevel = "Stable" }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Clinical override reason (required)", style = FairTypography.LabelSmall.copy(fontWeight = FontWeight.Medium), color = FairColors.TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                val isReasonError = showErrors && reason.isBlank()
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    isError = isReasonError,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = FairColors.ScreenBg,
                        unfocusedContainerColor = FairColors.ScreenBg,
                        focusedBorderColor = FairColors.Border,
                        unfocusedBorderColor = FairColors.Border,
                        errorBorderColor = FairColors.DangerRed
                    )
                )
                if (isReasonError) {
                    Text("Reason is required", color = FairColors.DangerRed, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }

                Spacer(modifier = Modifier.weight(1f))

                val isConfirmEnabled = selectedLevel.isNotEmpty() && reason.isNotBlank()
                Button(
                    onClick = {
                        showErrors = true
                        if (isConfirmEnabled) {
                            screenModel.submit(patientId, com.fairtriage.model.OverrideRequest(new_triage_level = selectedLevel, override_reason = reason))
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

    @Composable
    private fun LevelSelectorCard(level: String, selectedLevel: String, fillColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
        val isSelected = level == selectedLevel
        Box(
            modifier = modifier
                .clickable(onClick = onClick)
                .background(if (isSelected) fillColor else Color.White, RoundedCornerShape(12.dp))
                .border(1.dp, if (isSelected) fillColor else FairColors.Border, RoundedCornerShape(12.dp))
                .padding(vertical = 12.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(level, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            } else {
                Text(level, color = Color(0xFF64748B), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
