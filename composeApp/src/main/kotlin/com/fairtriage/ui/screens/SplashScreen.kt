package com.fairtriage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.fairtriage.core.DISCLAIMER
import com.fairtriage.ui.components.FairColors
import kotlinx.coroutines.delay

class SplashScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        LaunchedEffect(Unit) {
            delay(1500)
            navigator.replace(LoginScreen())
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(FairColors.PrimaryBlue)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "+",
                    color = Color.White,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "FairTriage",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "AI-Powered Fair Clinical Queue",
                    color = Color.White.copy(alpha = 0.70f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            }

            Text(
                text = DISCLAIMER,
                modifier = Modifier.align(Alignment.BottomCenter),
                color = Color.White.copy(alpha = 0.50f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
