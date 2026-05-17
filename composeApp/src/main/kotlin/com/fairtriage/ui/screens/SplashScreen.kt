package com.fairtriage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.fairtriage.ui.components.DisclaimerText
import com.fairtriage.ui.components.FairColors
import com.fairtriage.ui.components.FairTypography
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
                .background(FairColors.NavyDark)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(1f))
                
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(FairColors.AccentBlueBg, RoundedCornerShape(24.dp))
                        .border(1.dp, FairColors.AccentBlueBrd, RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MonitorHeart,
                        contentDescription = "Logo",
                        tint = FairColors.AccentBlue,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "FairTriage",
                    style = FairTypography.DisplayLarge,
                    color = FairColors.NavyText,
                    letterSpacing = (-1).sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "AI-Powered Fair Clinical\nPatient Queue System",
                    style = FairTypography.BodyMedium,
                    color = Color(0x8C94D2EC), // rgba(148,210,236,0.55)
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0x1A38BDF8), // rgba(56,189,248,0.10)
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3338BDF8))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = FairColors.AccentBlue,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Edge AI · Privacy First",
                            fontSize = 11.sp,
                            color = FairColors.AccentBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                CircularProgressIndicator(
                    color = FairColors.AccentBlue,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp)
            ) {
                // To apply opacity to DisclaimerText, wrapping in CompositionLocalProvider could work
                // But simple way is it defaults to CBD5E1. We will use a wrapper if we need it specifically dim
                DisclaimerText()
            }
        }
    }
}
