package com.fairtriage.core

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.fairtriage.ui.components.FairColors

private val FairTriageColors: ColorScheme = lightColorScheme(
    primary = FairColors.NavyDark,
    onPrimary = Color.White,
    primaryContainer = FairColors.InfoBlueBg,
    onPrimaryContainer = FairColors.InfoBlueText,
    secondary = FairColors.AccentBlue,
    onSecondary = Color.White,
    secondaryContainer = FairColors.AccentBlueBg,
    onSecondaryContainer = FairColors.AccentBlue,
    error = FairColors.DangerRed,
    background = FairColors.ScreenBg,
    surface = FairColors.Surface,
    surfaceVariant = FairColors.Border,
    outline = FairColors.Border
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FairTriageColors,
        typography = MaterialTheme.typography,
        content = content
    )
}
