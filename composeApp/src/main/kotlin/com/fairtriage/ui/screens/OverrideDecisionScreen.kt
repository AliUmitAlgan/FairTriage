package com.fairtriage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.fairtriage.model.OverrideRequest
import com.fairtriage.screenmodel.OverrideDecisionScreenModel
import com.fairtriage.screenmodel.SubmitState
import com.fairtriage.ui.components.AppBarStyle
import com.fairtriage.ui.components.ClinicalCard
import com.fairtriage.ui.components.FairColors
import com.fairtriage.ui.components.ScreenScaffold
import com.fairtriage.ui.components.SelectableOptionCard
import com.fairtriage.ui.components.TriageBadge
import com.fairtriage.ui.components.triageColor
import kotlinx.coroutines.delay

data class OverrideDecisionScreen(
    private val patientId: Int,
    private val currentTriageLevel: String
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val snackbarHostState = remember { SnackbarHostState() }
        val screenModel = rememberScreenModel { OverrideDecisionScreenModel() }
        val submitState by screenModel.submitState.collectAsState()
        val options = listOf("Critical", "Urgent", "Stable")
        var selectedLevel by remember { mutableStateOf(currentTriageLevel.takeIf { it in options } ?: "Stable") }
        var reason by remember { mutableStateOf("") }
        var showReasonError by remember { mutableStateOf(false) }
        var successVisible by remember { mutableStateOf(false) }
        val reasonError = showReasonError && reason.isBlank()
        val isSubmitting = submitState == SubmitState.Loading

        LaunchedEffect(submitState) {
            when (val state = submitState) {
                SubmitState.Success -> {
                    successVisible = true
                    delay(700)
                    navigator.replace(PatientDetailScreen(patientId))
                }
                is SubmitState.Error -> {
                    snackbarHostState.showSnackbar(state.message)
                    screenModel.clearError()
                }
                SubmitState.Idle,
                SubmitState.Loading -> Unit
            }
        }

        ScreenScaffold(
            title = "Override Decision",
            showBack = true,
            appBarStyle = AppBarStyle.White,
            snackbarHostState = snackbarHostState
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(FairColors.Background)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ClinicalCard(containerColor = Color(0xFFFFF8E1)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("!", color = FairColors.UrgentOrange, fontSize = 24.sp, fontWeight = FontWeight.Black)
                            Column {
                                Text(
                                    text = "You are about to override an AI decision",
                                    color = FairColors.TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "This action will be logged for audit purposes.",
                                    color = FairColors.TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    ClinicalCard {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text("Current triage level", color = FairColors.TextSecondary, fontSize = 12.sp)
                            Box(modifier = Modifier.padding(top = 8.dp)) {
                                TriageBadge(level = currentTriageLevel)
                            }
                        }
                    }

                    ClinicalCard {
                        Text(
                            text = "New Triage Level",
                            color = FairColors.TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            options.forEach { option ->
                                SelectableOptionCard(
                                    text = option,
                                    selected = selectedLevel == option,
                                    color = triageColor(option),
                                    onClick = { selectedLevel = option },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    ClinicalCard {
                        OutlinedTextField(
                            value = reason,
                            onValueChange = { reason = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Clinical Override Reason") },
                            isError = reasonError,
                            minLines = 3,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FairColors.PrimaryBlue,
                                focusedLabelColor = FairColors.PrimaryBlue,
                                cursorColor = FairColors.PrimaryBlue,
                                errorBorderColor = FairColors.CriticalRed,
                                errorLabelColor = FairColors.CriticalRed
                            )
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                text = if (reasonError) "Clinical override reason is required." else "",
                                color = FairColors.CriticalRed,
                                fontSize = 12.sp
                            )
                            Text("${reason.length}/1000", color = FairColors.TextSecondary, fontSize = 12.sp)
                        }
                    }

                    Button(
                        onClick = {
                            showReasonError = true
                            if (reason.isNotBlank()) {
                                screenModel.submit(
                                    patientId = patientId,
                                    request = OverrideRequest(
                                        new_triage_level = selectedLevel,
                                        override_reason = reason.trim()
                                    )
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(56.dp),
                        enabled = reason.isNotBlank() && !isSubmitting,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FairColors.CriticalRed,
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFB0BEC5),
                            disabledContentColor = Color.White
                        )
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(end = 8.dp).size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        }
                        Text("Confirm Override", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (successVisible) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = 0.88f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(FairColors.StableGreen, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("OK", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                            }
                            Text(
                                text = "Override saved",
                                modifier = Modifier.padding(top = 12.dp),
                                color = FairColors.TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
