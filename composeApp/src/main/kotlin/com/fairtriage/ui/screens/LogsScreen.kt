package com.fairtriage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.fairtriage.model.DecisionLog
import com.fairtriage.screenmodel.LogsScreenModel
import com.fairtriage.ui.components.*

class LogsScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { LogsScreenModel() }
        val lifecycleOwner = LocalLifecycleOwner.current
        val state by screenModel.state.collectAsState()
        var selectedFilter by remember { mutableStateOf("All") }
        val filters = listOf("All", "Override", "Created", "Score", "Queue", "Completed")

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
                    title = "Decision logs",
                    subtitle = "Full audit trail - clinical review",
                    onBack = { navigator.pop() }
                )
            },
            containerColor = FairColors.ScreenBg
        ) { paddingValues ->
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                // FILTER CHIPS
                LazyRow(
                    modifier = Modifier.fillMaxWidth().background(FairColors.Surface),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    items(filters) { filter ->
                        val isSelected = filter == selectedFilter
                        val bgColor = if (isSelected) FairColors.NavyDark else FairColors.Surface
                        val textColor = if (isSelected) Color.White else Color(0xFF64748B)
                        val borderColor = if (isSelected) FairColors.NavyDark else FairColors.Border
                        
                        Box(
                            modifier = Modifier
                                .clickable { selectedFilter = filter }
                                .background(bgColor, RoundedCornerShape(20.dp))
                                .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                                .padding(horizontal = 13.dp, vertical = 5.dp)
                        ) {
                            Text(filter, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                HorizontalDivider(color = FairColors.Border, thickness = 0.5.dp)

                // LOG CARDS
                Box(modifier = Modifier.weight(1f)) {
                    when (val currentState = state) {
                        is ScreenState.Loading -> LoadingState()
                        is ScreenState.Error -> ErrorState(currentState.message, onRetry = screenModel::refresh)
                        is ScreenState.Success -> {
                            val logs = currentState.data.filter { 
                                selectedFilter == "All" || it.action_type.contains(selectedFilter, ignoreCase = true) 
                            }
                            if (logs.isEmpty()) {
                                EmptyState(icon = "Inbox", title = "No logs found", hint = "Try changing the filter.")
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(logs, key = { it.id }) { log ->
                                        LogCard(log)
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
    private fun LogCard(log: DecisionLog) {
        var expanded by remember { mutableStateOf(false) }
        val (chipBg, chipText) = when (log.action_type) {
            "doctor_override" -> FairColors.PurpleBg to FairColors.PurpleBadge
            "created" -> FairColors.InfoBlueBg to FairColors.InfoBlueText
            "completed" -> Color(0xFFF1F5F9) to Color(0xFF475569)
            "score_calculated" -> Color(0xFFF0FDFA) to FairColors.ActionTeal
            "queue_updated" -> FairColors.UrgentTint to FairColors.UrgentFill
            else -> FairColors.Surface to FairColors.TextPrimary
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = FairColors.Surface),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, FairColors.Border)
        ) {
            Column(modifier = Modifier.padding(13.dp, 14.dp)) {
                // TOP ROW
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.background(chipBg, RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text(log.action_type, color = chipText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("Patient #${log.patient_id}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // TRIAGE CHANGE ROW
                if (log.old_triage_level != log.new_triage_level && log.old_triage_level != null && log.new_triage_level != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(log.old_triage_level, color = FairColors.DangerRed, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp).padding(horizontal = 4.dp))
                        Text(log.new_triage_level, color = FairColors.StableFill, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // EXPLANATION
                Text(
                    text = log.explanation,
                    color = Color(0xFF475569),
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    maxLines = if (expanded) Int.MAX_VALUE else 2
                )
                if (log.explanation.length > 90) {
                    Text(
                        text = if (expanded) "Show less" else "Show more",
                        color = FairColors.InfoBlueText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 5.dp).clickable { expanded = !expanded }
                    )
                }

                // TIMESTAMP
                Text(
                    text = formatTimestamp(log.created_at),
                    color = Color(0xFFCBD5E1),
                    fontSize = 10.sp,
                    modifier = Modifier.fillMaxWidth().padding(top = 7.dp),
                    textAlign = TextAlign.End
                )
            }
        }
    }

    private fun formatTimestamp(ts: String): String {
        // "2024-05-17T14:30:00" -> "17 May 2024 - 14:30"
        // Best effort simple string parsing
        try {
            val parts = ts.split("T")
            if (parts.size == 2) {
                val dateParts = parts[0].split("-")
                val timePart = parts[1].take(5) // HH:mm
                if (dateParts.size == 3) {
                    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                    val monthIdx = dateParts[1].toIntOrNull()?.minus(1) ?: 0
                    val month = months.getOrElse(monthIdx) { "Unk" }
                    return "${dateParts[2]} $month ${dateParts[0]} - $timePart"
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
        return ts
    }
}
