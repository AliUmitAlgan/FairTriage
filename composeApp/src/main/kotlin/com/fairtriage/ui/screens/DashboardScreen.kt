package com.fairtriage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.fairtriage.screenmodel.DashboardScreenModel
import com.fairtriage.ui.components.AppBarStyle
import com.fairtriage.ui.components.DashboardStatCard
import com.fairtriage.ui.components.EmptyState
import com.fairtriage.ui.components.ErrorState
import com.fairtriage.ui.components.FairColors
import com.fairtriage.ui.components.LoadingState
import com.fairtriage.ui.components.ScreenScaffold

class DashboardScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { DashboardScreenModel() }
        val state by screenModel.state.collectAsState()

        ScreenScaffold(
            title = "FairTriage",
            subtitle = "Emergency Dashboard",
            appBarStyle = AppBarStyle.Blue
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (val currentState = state) {
                    ScreenState.Loading -> LoadingState(modifier = Modifier.fillMaxWidth().height(240.dp))
                    is ScreenState.Error -> ErrorState(
                        errorMessage = currentState.message,
                        onRetry = screenModel::refresh,
                        modifier = Modifier.fillMaxWidth().height(240.dp)
                    )
                    is ScreenState.Success -> {
                        if (currentState.data.isEmpty()) {
                            EmptyState(
                                icon = "+",
                                title = "No patients yet",
                                hint = "Add a patient or seed demo data.",
                                modifier = Modifier.fillMaxWidth().height(240.dp)
                            )
                        } else {
                            DashboardStats(currentState.data)
                        }
                    }
                }

                Text(
                    text = "Quick Actions",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
                    color = FairColors.TextSecondary,
                    fontSize = 14.sp
                )

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionCard(
                        text = "Add New Patient",
                        icon = "+",
                        subtitle = "Register new arrival",
                        color = FairColors.PrimaryBlue,
                        onClick = { navigator.push(AddPatientScreen()) }
                    )
                    QuickActionCard(
                        text = "Patient Queue",
                        icon = "#",
                        subtitle = "View prioritized list",
                        color = FairColors.PrimaryBlue,
                        onClick = { navigator.push(QueueScreen()) }
                    )
                    QuickActionCard(
                        text = "Decision Logs",
                        icon = "H",
                        subtitle = "AI action history",
                        color = FairColors.PrimaryBlue,
                        onClick = { navigator.push(LogsScreen()) }
                    )
                    QuickActionCard(
                        text = "Seed Demo Data",
                        icon = ">",
                        subtitle = "Load demo patients",
                        color = FairColors.SecondaryTeal,
                        onClick = screenModel::seedDemoData
                    )
                    QuickActionCard(
                        text = "Reset All Data",
                        icon = "X",
                        subtitle = "Clear all patients and logs",
                        color = FairColors.CriticalRed,
                        onClick = screenModel::resetAll
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardStats(patients: List<Patient>) {
    val criticalCount = patients.count { it.triage_level.equals("Critical", ignoreCase = true) }
    val urgentCount = patients.count { it.triage_level.equals("Urgent", ignoreCase = true) }
    val stableCount = patients.count { it.triage_level.equals("Stable", ignoreCase = true) }

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DashboardStatCard(
                title = "Total Patients",
                value = patients.size,
                accentColor = FairColors.PrimaryBlue,
                icon = "P",
                modifier = Modifier.weight(1f)
            )
            DashboardStatCard(
                title = "Critical",
                value = criticalCount,
                accentColor = FairColors.CriticalRed,
                icon = "!",
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DashboardStatCard(
                title = "Urgent",
                value = urgentCount,
                accentColor = FairColors.UrgentOrange,
                icon = "T",
                modifier = Modifier.weight(1f)
            )
            DashboardStatCard(
                title = "Stable",
                value = stableCount,
                accentColor = FairColors.StableGreen,
                icon = "OK",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    text: String,
    icon: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = FairColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .background(color.copy(alpha = 0.10f), RoundedCornerShape(7.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = FairColors.TextSecondary, fontSize = 12.sp)
            }
            Text(">", color = FairColors.TextSecondary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
