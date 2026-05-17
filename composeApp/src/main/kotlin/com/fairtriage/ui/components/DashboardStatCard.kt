package com.fairtriage.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DashboardStatCard(
    title: String,
    value: Int,
    accentColor: Color,
    icon: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = FairColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Text(
                text = icon,
                modifier = Modifier.align(Alignment.TopEnd),
                color = accentColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = value.toString(),
                    color = accentColor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = title,
                    color = FairColors.TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}
