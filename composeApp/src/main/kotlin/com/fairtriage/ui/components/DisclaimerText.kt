package com.fairtriage.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fairtriage.core.DISCLAIMER

@Composable
fun DisclaimerText(modifier: Modifier = Modifier) {
    Text(
        text = DISCLAIMER,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp, top = 4.dp),
        color = Color(0xFF90A4AE),
        textAlign = TextAlign.Center,
        fontSize = 11.sp
    )
}
