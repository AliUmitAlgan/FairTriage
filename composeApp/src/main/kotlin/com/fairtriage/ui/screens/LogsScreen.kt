package com.fairtriage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import com.fairtriage.core.ScreenState
import com.fairtriage.model.DecisionLog
import com.fairtriage.screenmodel.LogsScreenModel
import com.fairtriage.ui.components.ActionTypeChip
import com.fairtriage.ui.components.AppBarStyle
import com.fairtriage.ui.components.EmptyState
import com.fairtriage.ui.components.ErrorState
import com.fairtriage.ui.components.FairColors
import com.fairtriage.ui.components.LoadingState
import com.fairtriage.ui.components.ScreenScaffold
import com.fairtriage.ui.components.actionColor
import com.fairtriage.ui.components.formatCreatedAt

class LogsScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel { LogsScreenModel() }
        val state by screenModel.state.collectAsState()
        var selectedFilter by remember { mutableStateOf("All") }

        ScreenScaffold(
            title = "Decision Logs",
            showBack = true,
            appBarStyle = AppBarStyle.Blue
        ) { paddingValues ->
            when (val currentState = state) {
                ScreenState.Loading -> LoadingState(modifier = Modifier.padding(paddingValues))
                is ScreenState.Error -> ErrorState(
                    errorMessage = currentState.message,
                    onRetry = screenModel::refresh,
                    modifier = Modifier.padding(paddingValues)
                )
                is ScreenState.Success -> LogsContent(
                    logs = currentState.data,
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun LogsContent(
    logs: List<DecisionLog>,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredLogs = logs.filter { log ->
        when (selectedFilter) {
            "Created" -> log.action_type == "created"
            "Score" -> log.action_type == "score_calculated"
            "Override" -> log.action_type == "doctor_override"
            "Completed" -> log.action_type == "completed"
            else -> true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FairColors.Background)
    ) {
        FilterRow(selectedFilter = selectedFilter, onFilterSelected = onFilterSelected)
        if (filteredLogs.isEmpty()) {
            EmptyState(
                icon = "L",
                title = "No decision logs yet",
                hint = "Clinical actions will appear here.",
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredLogs, key = { log -> log.id }) { log ->
                    DecisionLogCard(log = log)
                }
            }
        }
    }
}

@Composable
private fun FilterRow(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf("All", "Created", "Score", "Override", "Completed").forEach { filter ->
            val selected = selectedFilter == filter
            Surface(
                modifier = Modifier.clickable { onFilterSelected(filter) },
                shape = RoundedCornerShape(20.dp),
                color = if (selected) FairColors.PrimaryBlue else FairColors.Surface
            ) {
                Text(
                    text = filter,
                    color = if (selected) Color.White else FairColors.TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                )
            }
        }
    }
}

@Composable
private fun DecisionLogCard(log: DecisionLog) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = FairColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(8.dp)
                    .background(actionColor(log.action_type), CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ActionTypeChip(actionType = log.action_type)
                    Text(
                        text = "Patient #${log.patient_id}",
                        color = FairColors.TextSecondary.copy(alpha = 0.72f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.End
                    )
                }
                if (
                    log.old_triage_level != null &&
                    log.new_triage_level != null &&
                    log.old_triage_level != log.new_triage_level
                ) {
                    Text(
                        text = "${log.old_triage_level} -> ${log.new_triage_level}",
                        color = actionColor(log.action_type),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = log.explanation,
                    color = FairColors.TextPrimary,
                    fontSize = 14.sp,
                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!expanded && log.explanation.length > 90) {
                    Text("Show more", color = FairColors.PrimaryBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text(
                        text = formatCreatedAt(log.created_at),
                        color = FairColors.TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
