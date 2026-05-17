package com.fairtriage.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object FairColors {
    val PrimaryBlue = Color(0xFF1565C0)
    val SecondaryTeal = Color(0xFF00796B)
    val Background = Color(0xFFF5F7FA)
    val Surface = Color(0xFFFFFFFF)
    val CriticalRed = Color(0xFFC62828)
    val CriticalTint = Color(0xFFFFEBEE)
    val UrgentOrange = Color(0xFFE65100)
    val UrgentTint = Color(0xFFFFF3E0)
    val StableGreen = Color(0xFF2E7D32)
    val StableTint = Color(0xFFE8F5E9)
    val TextPrimary = Color(0xFF1A1A2E)
    val TextSecondary = Color(0xFF546E7A)
    val Divider = Color(0xFFECEFF1)
}

@Composable
fun ClinicalCard(
    modifier: Modifier = Modifier,
    containerColor: Color = FairColors.Surface,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}

@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = FairColors.PrimaryBlue,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(
            modifier = Modifier
                .padding(top = 6.dp)
                .height(1.dp)
                .fillMaxWidth()
                .background(FairColors.PrimaryBlue)
        )
    }
}

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = FairColors.PrimaryBlue)
            Spacer(Modifier.height(12.dp))
            Text("Loading...", color = FairColors.TextSecondary, fontSize = 14.sp)
        }
    }
}

@Composable
fun ErrorState(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "!",
                color = FairColors.CriticalRed,
                fontSize = 48.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = errorMessage,
                color = FairColors.CriticalRed,
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onRetry) {
                Text("Retry", color = FairColors.PrimaryBlue)
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = icon,
                color = FairColors.PrimaryBlue,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = title,
                color = FairColors.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            if (hint != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = hint,
                    color = FairColors.TextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun SelectableOptionCard(
    text: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .height(54.dp)
            .background(
                color = if (selected) color else FairColors.Surface,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = if (selected) color else FairColors.Divider,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selected) {
                Text("OK", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                text = text,
                color = if (selected) Color.White else FairColors.TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
