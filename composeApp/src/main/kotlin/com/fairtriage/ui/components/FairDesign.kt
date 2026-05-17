package com.fairtriage.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.round

object FairColors {
    val NavyDark = Color(0xFF0A192F)
    val NavyText = Color(0xFFF0F9FF)
    val NavySubtext = Color(0x7394D2EC) // rgba(148,210,236,0.45)
    val AccentBlue = Color(0xFF38BDF8)
    val AccentBlueBg = Color(0x1F38BDF8) // rgba(56,189,248,0.12)
    val AccentBlueBrd = Color(0x4038BDF8) // rgba(56,189,248,0.25)
    
    val ScreenBg = Color(0xFFF8FAFC)
    val Surface = Color(0xFFFFFFFF)
    val Border = Color(0xFFE2E8F0)
    val Divider = Color(0xFFF1F5F9)
    
    val TextPrimary = Color(0xFF0F172A)
    val TextSecondary = Color(0xFF475569)
    val TextHint = Color(0xFF94A3B8)
    val TextMuted = Color(0xFFCBD5E1)
    
    val CriticalFill = Color(0xFFDC2626)
    val CriticalTint = Color(0xFFFFF5F5)
    val CriticalBorder = Color(0xFFFECACA)
    val CriticalSide = Color(0xFFDC2626)
    
    val UrgentFill = Color(0xFFEA580C)
    val UrgentTint = Color(0xFFFFF7ED)
    val UrgentBorder = Color(0xFFFED7AA)
    val UrgentSide = Color(0xFFEA580C)
    
    val StableFill = Color(0xFF16A34A)
    val StableTint = Color(0xFFF0FFF4)
    val StableBorder = Color(0xFFBBF7D0)
    val StableSide = Color(0xFF16A34A)
    
    val InfoBlueBg = Color(0xFFEFF6FF)
    val InfoBlueBorder = Color(0xFFBFDBFE)
    val InfoBlueText = Color(0xFF1D4ED8)
    val InfoBlueDark = Color(0xFF1E40AF)
    
    val ActionTeal = Color(0xFF0D9488)
    val DangerRed = Color(0xFFDC2626)
    val PurpleBadge = Color(0xFF7C3AED)
    val PurpleBg = Color(0xFFF3E8FF)
    
    val WarningBg = Color(0xFFFFF8E1)
    val WarningBorder = Color(0xFFF9A825)
    val WarningText = Color(0xFF92400E)
    
    val SuccessGreen = Color(0xFF22C55E)
}

fun triageFill(level: String?): Color = when (level?.lowercase()) {
    "critical" -> FairColors.CriticalFill
    "urgent" -> FairColors.UrgentFill
    else -> FairColors.StableFill
}

fun triageTint(level: String?): Color = when (level?.lowercase()) {
    "critical" -> FairColors.CriticalTint
    "urgent" -> FairColors.UrgentTint
    else -> FairColors.StableTint
}

fun triageBorder(level: String?): Color = when (level?.lowercase()) {
    "critical" -> FairColors.CriticalBorder
    "urgent" -> FairColors.UrgentBorder
    else -> FairColors.StableBorder
}

fun formatScore(value: Double?): String {
    if (value == null) return "--"
    return (round(value * 10.0) / 10.0).toString()
}

fun formatWaitFactor(value: Double?): String = if (value == null) "+--" else "+${formatScore(value)}"

object FairTypography {
    val DisplayLarge = androidx.compose.ui.text.TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Medium)
    val HeadlineLarge = androidx.compose.ui.text.TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Medium)
    val TitleLarge = androidx.compose.ui.text.TextStyle(fontSize = 19.sp, fontWeight = FontWeight.Medium)
    val TitleMedium = androidx.compose.ui.text.TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium)
    val BodyLarge = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal)
    val BodyMedium = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal)
    val LabelLarge = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.08.sp)
    val LabelSmall = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Normal)
    val Caption = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Normal)
    
    val ScoreFinal = androidx.compose.ui.text.TextStyle(fontSize = 23.sp, fontWeight = FontWeight.Medium)
    val ScoreBig = androidx.compose.ui.text.TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium)
    val ScoreNormal = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium)
    val StatNumber = androidx.compose.ui.text.TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Medium)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FairTriageTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Column {
                Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = FairColors.NavyText)
                if (subtitle != null) {
                    Text(text = subtitle, fontSize = 10.sp, fontWeight = FontWeight.Normal, color = FairColors.NavySubtext)
                }
            }
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xB3FFFFFF) // rgba(255,255,255,0.7)
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = FairColors.NavyDark,
            actionIconContentColor = Color(0x99FFFFFF) // rgba(255,255,255,0.6)
        )
    )
}

@Composable
fun TriageBadge(level: String, large: Boolean = false) {
    val bgColor = when (level.lowercase()) {
        "critical" -> FairColors.CriticalFill
        "urgent" -> FairColors.UrgentFill
        else -> FairColors.StableFill
    }
    
    val fontSize = if (large) 13.sp else 10.sp
    val paddingHorizontal = if (large) 18.dp else 8.dp
    val paddingVertical = if (large) 5.dp else 2.dp

    Box(
        modifier = Modifier
            .background(color = bgColor, shape = RoundedCornerShape(20.dp))
            .padding(horizontal = paddingHorizontal, vertical = paddingVertical),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = level,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            fontSize = fontSize
        )
    }
}

@Composable
fun QueuePositionCircle(position: Int, triageLevel: String) {
    val bgColor = when (triageLevel.lowercase()) {
        "critical" -> FairColors.CriticalFill
        "urgent" -> FairColors.UrgentFill
        else -> FairColors.StableFill
    }

    Box(
        modifier = Modifier
            .size(36.dp)
            .background(color = bgColor, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = position.toString(),
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SectionCardTitle(text: String, icon: ImageVector? = null) {
    Row(
        modifier = Modifier.padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(14.dp).padding(end = 4.dp)
            )
        }
        Text(
            text = text.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF64748B),
            letterSpacing = 0.8.sp
        )
    }
}

@Composable
fun StandardCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = FairColors.Surface),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, FairColors.Border),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            content = content
        )
    }
}

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = FairColors.NavyDark)
    }
}

@Composable
fun ErrorState(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("!", color = FairColors.DangerRed, fontSize = 48.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text(errorMessage, color = FairColors.DangerRed, fontSize = 14.sp)
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onRetry) {
                Text("Retry", color = FairColors.TextPrimary)
            }
        }
    }
}

@Composable
fun EmptyState(
    icon: String,
    title: String,
    hint: String? = null,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 48.sp)
            Spacer(Modifier.height(12.dp))
            Text(title, color = FairColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            if (hint != null) {
                Spacer(Modifier.height(6.dp))
                Text(hint, color = FairColors.TextSecondary, fontSize = 14.sp)
            }
        }
    }
}
