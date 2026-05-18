package com.fairtriage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.fairtriage.core.LocalTriageCache
import com.fairtriage.core.ScreenState
import com.fairtriage.model.Patient
import com.fairtriage.screenmodel.DashboardScreenModel
import com.fairtriage.ui.components.*

class DashboardScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { DashboardScreenModel() }
        val lifecycleOwner = LocalLifecycleOwner.current
        val state by screenModel.state.collectAsState()

        DisposableEffect(lifecycleOwner, screenModel) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    screenModel.refresh()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        Scaffold(
            topBar = {
                FairTriageTopBar(
                    title = "FairTriage",
                    subtitle = "Emergency Dashboard - live hospital queue",
                    actions = {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(FairColors.SuccessGreen, CircleShape)
                                .align(Alignment.CenterVertically)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color(0x99FFFFFF)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                )
            },
            containerColor = FairColors.ScreenBg
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // WELCOME STRIP
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = FairColors.NavyDark)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Emergency department overview", style = FairTypography.BodyLarge.copy(fontWeight = FontWeight.Medium), color = FairColors.NavyText)
                                val patientCount = if (state is ScreenState.Success) (state as ScreenState.Success).data.size else 0
                                val pending = LocalTriageCache.pendingCreateCount()
                                val syncText = if (pending > 0) "$pending pending offline sync" else "live sync ready"
                                Text("$patientCount patients - $syncText", style = FairTypography.LabelSmall, color = Color(0x8094D2EC))
                            }
                            Box(modifier = Modifier.size(10.dp).background(FairColors.SuccessGreen, CircleShape))
                        }
                    }
                }

                item {
                    when (val currentState = state) {
                        is ScreenState.Loading -> LoadingState(modifier = Modifier.fillMaxWidth().height(240.dp))
                        is ScreenState.Error -> ErrorState(
                            errorMessage = currentState.message,
                            onRetry = screenModel::refresh,
                            modifier = Modifier.fillMaxWidth().height(240.dp)
                        )
                        is ScreenState.Success -> {
                            val patients = currentState.data
                            val criticalCount = patients.count { it.triage_level.equals("Critical", ignoreCase = true) }
                            val urgentCount = patients.count { it.triage_level.equals("Urgent", ignoreCase = true) }
                            val stableCount = patients.count { it.triage_level.equals("Stable", ignoreCase = true) }

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    StatCard(title = "Total Patients", value = patients.size, color = FairColors.InfoBlueText, icon = Icons.Default.Group, modifier = Modifier.weight(1f))
                                    StatCard(title = "Critical", value = criticalCount, color = FairColors.CriticalFill, icon = Icons.Default.Warning, modifier = Modifier.weight(1f))
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    StatCard(title = "Urgent", value = urgentCount, color = FairColors.UrgentFill, icon = Icons.Default.Schedule, modifier = Modifier.weight(1f))
                                    StatCard(title = "Stable", value = stableCount, color = FairColors.StableFill, icon = Icons.Default.CheckCircle, modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Quick actions",
                        style = FairTypography.LabelLarge,
                        color = Color(0xFF64748B)
                    )
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionCard(
                            title = "Patient Queue",
                            subtitle = "View prioritized patient list",
                            icon = Icons.Default.FormatListNumbered,
                            iconBg = FairColors.InfoBlueBg,
                            iconTint = FairColors.InfoBlueText,
                            onClick = { navigator.push(QueueScreen()) }
                        )
                        ActionCard(
                            title = "Add new patient",
                            subtitle = "Enter symptoms & vitals",
                            icon = Icons.Default.PersonAdd,
                            iconBg = FairColors.InfoBlueBg,
                            iconTint = FairColors.InfoBlueText,
                            onClick = { navigator.push(AddPatientScreen()) }
                        )
                        ActionCard(
                            title = "Decision logs",
                            subtitle = "Audit trail & overrides",
                            icon = Icons.Default.History,
                            iconBg = Color(0xFFF1F5F9),
                            iconTint = Color(0xFF475569),
                            onClick = { navigator.push(LogsScreen()) }
                        )
                    }
                }

                item { DisclaimerText() }
            }
        }
    }

    @Composable
    private fun StatCard(title: String, value: Int, color: Color, icon: ImageVector, modifier: Modifier = Modifier) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = FairColors.Surface),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, FairColors.Border)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = value.toString(), style = FairTypography.StatNumber, color = color)
                Text(text = title, style = FairTypography.LabelSmall, color = Color(0xFF64748B))
            }
        }
    }

    @Composable
    private fun ActionCard(
        title: String,
        subtitle: String,
        icon: ImageVector,
        iconBg: Color,
        iconTint: Color,
        border: Color = FairColors.Border,
        titleColor: Color = FairColors.TextPrimary,
        onClick: () -> Unit
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = FairColors.Surface),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, border)
        ) {
            Row(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(36.dp).background(iconBg, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, style = FairTypography.BodyLarge.copy(fontWeight = FontWeight.Medium), color = titleColor)
                    Text(text = subtitle, style = FairTypography.BodyMedium.copy(fontSize = 12.sp), color = FairColors.TextSecondary)
                }
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFCBD5E1))
            }
        }
    }
}
