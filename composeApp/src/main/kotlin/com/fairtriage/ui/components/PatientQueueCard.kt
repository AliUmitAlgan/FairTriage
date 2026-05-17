package com.fairtriage.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fairtriage.model.Patient

@Composable
fun PatientQueueCard(
    patient: Patient,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val levelColor = triageColor(patient.triage_level)
    val backgroundTint = when (patient.triage_level?.lowercase()) {
        "critical" -> FairColors.CriticalTint
        "urgent" -> FairColors.UrgentTint
        else -> FairColors.Surface
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundTint),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(levelColor)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = levelColor,
                    contentColor = Color.White
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = patient.queue_position?.toString() ?: "-",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = patient.full_name,
                            modifier = Modifier.weight(1f),
                            color = FairColors.TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TriageBadge(level = patient.triage_level)
                    }
                    Text(
                        text = "Risk: ${patient.clinical_risk_score.scoreText()}  |  Priority: ${patient.final_priority_score.scoreText()}",
                        color = FairColors.TextSecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Wait factor: +${patient.waiting_time_factor.scoreText()}  |  Status: ${patient.status}",
                        color = FairColors.TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Text(
                    text = ">",
                    color = FairColors.TextSecondary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
