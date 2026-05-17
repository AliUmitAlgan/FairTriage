package com.fairtriage.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

fun triageColor(level: String?): Color {
    return when (level?.lowercase()) {
        "critical" -> FairColors.CriticalRed
        "urgent" -> FairColors.UrgentOrange
        "stable" -> FairColors.StableGreen
        else -> FairColors.TextSecondary
    }
}

fun triageTint(level: String?): Color {
    return when (level?.lowercase()) {
        "critical" -> FairColors.CriticalTint
        "urgent" -> FairColors.UrgentTint
        "stable" -> FairColors.StableTint
        else -> FairColors.Surface
    }
}

fun actionColor(actionType: String): Color {
    return when (actionType.lowercase()) {
        "created" -> FairColors.PrimaryBlue
        "doctor_override" -> Color(0xFF6A1B9A)
        "completed" -> Color(0xFF616161)
        "score_calculated" -> FairColors.SecondaryTeal
        "queue_updated" -> FairColors.UrgentOrange
        else -> FairColors.TextSecondary
    }
}

@Composable
fun TriageBadge(
    level: String?,
    modifier: Modifier = Modifier
) {
    Chip(
        text = level ?: "Unscored",
        color = triageColor(level),
        modifier = modifier
    )
}

@Composable
fun StatusChip(
    status: String,
    modifier: Modifier = Modifier
) {
    val color = when (status.lowercase()) {
        "waiting" -> FairColors.PrimaryBlue
        "in_treatment" -> Color(0xFF6A1B9A)
        "completed" -> Color(0xFF616161)
        else -> FairColors.TextSecondary
    }
    Chip(
        text = status.replace('_', ' '),
        color = color,
        modifier = modifier
    )
}

@Composable
fun ActionTypeChip(
    actionType: String,
    modifier: Modifier = Modifier
) {
    Chip(
        text = actionType.replace('_', ' '),
        color = actionColor(actionType),
        modifier = modifier
    )
}

@Composable
private fun Chip(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = color,
        contentColor = Color.White
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
