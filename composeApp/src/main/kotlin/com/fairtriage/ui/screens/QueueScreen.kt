package com.fairtriage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.fairtriage.core.ScreenState
import com.fairtriage.model.Patient
import com.fairtriage.screenmodel.QueueScreenModel
import com.fairtriage.ui.components.DisclaimerText
import com.fairtriage.ui.components.EmptyState
import com.fairtriage.ui.components.ErrorState
import com.fairtriage.ui.components.FairColors
import com.fairtriage.ui.components.FairTriageTopBar
import com.fairtriage.ui.components.FairTypography
import com.fairtriage.ui.components.LoadingState
import com.fairtriage.ui.components.PatientQueueCard

class QueueScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { QueueScreenModel() }
        val state by screenModel.state.collectAsState()

        Column(modifier = Modifier.fillMaxSize().background(FairColors.ScreenBg)) {
            FairTriageTopBar(
                title = "Patient Queue",
                onBack = { navigator.pop() }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FairColors.NavyDark)
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 20.dp)
            ) {
                Text("Patient Queue", style = FairTypography.TitleLarge.copy(fontSize = 16.sp), color = FairColors.NavyText)
                Spacer(modifier = Modifier.height(2.dp))
                Text("Sorted by AI priority score - live clinical queue", style = FairTypography.LabelSmall, color = Color(0x7394D2EC))

                if (state is ScreenState.Success) {
                    Spacer(modifier = Modifier.height(12.dp))
                    QueueStatusBanner(patients = (state as ScreenState.Success).data)
                }
            }

            Box(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                when (val currentState = state) {
                    is ScreenState.Loading -> LoadingState()
                    is ScreenState.Error -> ErrorState(currentState.message, onRetry = screenModel::refresh)
                    is ScreenState.Success -> {
                        val patients = currentState.data
                        if (patients.isEmpty()) {
                            EmptyState(icon = "Inbox", title = "No patients in queue", hint = "Queue is clear.")
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp)
                            ) {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("${patients.size} patients waiting", style = FairTypography.LabelLarge, color = Color(0xFF64748B))
                                        OutlinedButton(
                                            onClick = screenModel::refresh,
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, FairColors.Border),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF475569), modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Refresh", fontSize = 11.sp, color = Color(0xFF475569))
                                        }
                                    }
                                }

                                items(patients, key = { it.id }) { patient ->
                                    PatientQueueCard(
                                        patient = patient,
                                        onClick = { navigator.push(PatientDetailScreen(patient.id)) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            DisclaimerText()
        }
    }
}

@Composable
private fun QueueStatusBanner(patients: List<Patient>) {
    val criticalCount = patients.count { it.triage_level.equals("Critical", ignoreCase = true) }
    val urgentCount = patients.count { it.triage_level.equals("Urgent", ignoreCase = true) }
    val message: String
    val accent: Color
    val background: Color
    val border: Color

    when {
        criticalCount > 0 -> {
            message = "$criticalCount critical patient(s) need immediate review"
            accent = FairColors.CriticalFill
            background = Color(0x26DC2626)
            border = Color(0x4DDC2626)
        }
        urgentCount > 0 -> {
            message = "$urgentCount urgent patient(s) are waiting"
            accent = FairColors.UrgentFill
            background = Color(0x26EA580C)
            border = Color(0x4DEA580C)
        }
        else -> {
            message = "All waiting patients are currently stable"
            accent = FairColors.StableFill
            background = Color(0x2616A34A)
            border = Color(0x4D16A34A)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background, RoundedCornerShape(12.dp))
            .border(1.dp, border, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Warning, contentDescription = null, tint = accent, modifier = Modifier.size(17.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = message,
            style = FairTypography.LabelSmall.copy(fontWeight = FontWeight.Medium),
            color = Color.White
        )
    }
}
