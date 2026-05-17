package com.fairtriage.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator

enum class AppBarStyle {
    Blue,
    White
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenScaffold(
    title: String? = null,
    subtitle: String? = null,
    showBack: Boolean = false,
    appBarStyle: AppBarStyle = AppBarStyle.White,
    snackbarHostState: SnackbarHostState? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    val navigator = LocalNavigator.current
    val isBlue = appBarStyle == AppBarStyle.Blue
    val appBarContainer = if (isBlue) FairColors.PrimaryBlue else FairColors.Surface
    val appBarContent = if (isBlue) Color.White else FairColors.TextPrimary
    val navigationTint = if (isBlue) Color.White else FairColors.PrimaryBlue

    Scaffold(
        containerColor = FairColors.Background,
        topBar = {
            if (title != null) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = title,
                                color = appBarContent,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (subtitle != null) {
                                Text(
                                    text = subtitle,
                                    color = appBarContent.copy(alpha = 0.78f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        if (showBack && navigator?.canPop == true) {
                            IconButton(onClick = { navigator.pop() }) {
                                Text(
                                    text = "<",
                                    color = navigationTint,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = appBarContainer,
                        titleContentColor = appBarContent,
                        navigationIconContentColor = navigationTint
                    )
                )
            }
        },
        bottomBar = {
            DisclaimerText(modifier = Modifier.padding(horizontal = 16.dp))
        },
        snackbarHost = {
            if (snackbarHostState != null) {
                SnackbarHost(snackbarHostState)
            }
        },
        content = content
    )
}
