package com.fairtriage.core

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.fairtriage.ui.components.FairColors

private val FairTriageColors: ColorScheme = lightColorScheme(
    primary = FairColors.PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCEBFF),
    onPrimaryContainer = Color(0xFF09233F),
    secondary = FairColors.SecondaryTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFBEEBE3),
    onSecondaryContainer = Color(0xFF002E28),
    tertiary = Color(0xFF7B1FA2),
    error = FairColors.CriticalRed,
    background = FairColors.Background,
    surface = FairColors.Surface,
    surfaceVariant = FairColors.Divider,
    outline = FairColors.Divider
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FairTriageColors,
        typography = MaterialTheme.typography,
        content = content
    )
}
