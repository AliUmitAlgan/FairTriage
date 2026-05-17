package com.fairtriage.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fairtriage.model.Patient
import kotlin.math.max
import kotlin.math.min

@Composable
fun ScoreBreakdownCard(
    patient: Patient,
    modifier: Modifier = Modifier
) {
    ClinicalCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("AI", color = FairColors.PrimaryBlue, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(
                text = "AI Risk Analysis",
                color = FairColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        ScoreRow("Symptom Score", patient.symptom_score.scoreText())
        DividerLine()
        ScoreRow("Image Score", patient.image_score.scoreText())
        DividerLine()
        ScoreRow("History Score", patient.history_score.scoreText())
        DividerLine()
        ScoreRow(
            label = "Clinical Risk",
            value = patient.clinical_risk_score.scoreText(),
            valueColor = triageColor(patient.triage_level),
            bold = true,
            valueSize = 22
        )
        SeverityIndicator(
            score = patient.clinical_risk_score ?: 0.0,
            color = triageColor(patient.triage_level)
        )
        ScoreRow("Waiting Factor", "+${patient.waiting_time_factor.scoreText()}")
        DividerLine()
        ScoreRow(
            label = "Final Priority",
            value = patient.final_priority_score.scoreText(),
            valueColor = triageColor(patient.triage_level),
            bold = true,
            valueSize = 26
        )
    }
}

@Composable
private fun SeverityIndicator(score: Double, color: Color) {
    val fraction = min(1.0, max(0.0, score / 100.0)).toFloat()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .background(FairColors.Divider, RoundedCornerShape(20.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .height(6.dp)
                .background(color, RoundedCornerShape(20.dp))
        )
    }
}

@Composable
private fun ScoreRow(
    label: String,
    value: String,
    valueColor: Color = FairColors.TextPrimary,
    bold: Boolean = false,
    valueSize: Int = 14
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = FairColors.TextSecondary,
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = valueSize.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold
        )
    }
}

@Composable
private fun DividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(FairColors.Divider)
    )
}
