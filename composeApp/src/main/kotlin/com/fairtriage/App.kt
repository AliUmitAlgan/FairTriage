package com.fairtriage

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import com.fairtriage.core.AppTheme
import com.fairtriage.ui.screens.SplashScreen

@Composable
fun App() {
    AppTheme {
        Navigator(SplashScreen())
    }
}
