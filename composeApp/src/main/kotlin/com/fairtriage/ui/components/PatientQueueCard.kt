package com.fairtriage.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fairtriage.model.Patient

@Composable
fun PatientQueueCard(
    patient: Patient,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val level = patient.triage_level ?: "Stable"
    val tint = if (level.equals("Stable", ignoreCase = true)) FairColors.Surface else triageTint(level)
    val borderColor = if (level.equals("Stable", ignoreCase = true)) FairColors.Border else triageBorder(level)
    val stripColor = triageFill(level)

    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = tint),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, borderColor)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            BoxStrip(color = stripColor)
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                QueuePositionCircle(position = patient.queue_position ?: 0, triageLevel = level)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = patient.full_name,
                            style = FairTypography.BodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = FairColors.TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TriageBadge(level = level)
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = "Risk ${formatScore(patient.clinical_risk_score)} - Priority ${formatScore(patient.final_priority_score)}",
                        style = FairTypography.LabelSmall,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Wait ${formatMinutes(patient.waiting_minutes)} - Factor ${formatWaitFactor(patient.waiting_time_factor)} - ${patient.status}",
                        style = FairTypography.Caption,
                        color = FairColors.TextHint
                    )
                    if (patient.max_waiting_exceeded) {
                        Spacer(modifier = Modifier.height(5.dp))
                        Box(
                            modifier = Modifier
                                .background(FairColors.WarningBg, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Max-waiting fairness review active",
                                style = FairTypography.Caption,
                                color = FairColors.WarningText
                            )
                        }
                    }
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFFCBD5E1),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private fun formatMinutes(value: Double?): String {
    if (value == null) return "-- min"
    return "${value.toInt()} min"
}

@Composable
private fun BoxStrip(color: Color) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .width(6.dp)
            .fillMaxHeight()
            .background(color)
    )
}
