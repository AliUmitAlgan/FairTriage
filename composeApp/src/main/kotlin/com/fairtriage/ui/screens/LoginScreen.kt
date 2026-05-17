package com.fairtriage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.fairtriage.ui.components.DisclaimerText
import com.fairtriage.ui.components.FairColors
import com.fairtriage.ui.components.FairTypography

class LoginScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }
        var showErrors by remember { mutableStateOf(false) }
        val emailError = showErrors && email.isBlank()
        val passwordError = showErrors && password.isBlank()

        Column(modifier = Modifier.fillMaxSize().background(FairColors.NavyDark)) {
            // TOP SECTION
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 36.dp, end = 36.dp, top = 36.dp, bottom = 44.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(FairColors.AccentBlueBg, RoundedCornerShape(16.dp))
                        .border(1.dp, FairColors.AccentBlueBrd, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = "Logo",
                        tint = FairColors.AccentBlue,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "FairTriage",
                    style = FairTypography.HeadlineLarge,
                    color = FairColors.NavyText
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "Emergency Department Portal",
                    style = FairTypography.BodyLarge.copy(fontSize = 12.sp),
                    color = Color(0x8094D2EC) // rgba(148,210,236,0.50)
                )
            }

            // BOTTOM CARD
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .offset(y = (-16).dp),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = FairColors.Surface
            ) {
                Column(
                    modifier = Modifier.padding(start = 28.dp, end = 28.dp, top = 28.dp, bottom = 20.dp)
                ) {
                    Text(
                        text = "Doctor Login",
                        style = FairTypography.TitleLarge,
                        color = FairColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Sign in to access the triage system",
                        style = FairTypography.BodyLarge.copy(fontSize = 12.sp),
                        color = FairColors.TextHint
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Text("Email address", style = FairTypography.LabelSmall.copy(fontWeight = FontWeight.Medium), color = FairColors.TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = FairColors.TextHint) },
                        isError = emailError,
                        supportingText = {
                            if (emailError) Text("Email address is required", color = FairColors.DangerRed)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = FairColors.ScreenBg,
                            unfocusedContainerColor = FairColors.ScreenBg,
                            focusedBorderColor = FairColors.Border,
                            unfocusedBorderColor = FairColors.Border,
                            errorBorderColor = FairColors.DangerRed
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Password", style = FairTypography.LabelSmall.copy(fontWeight = FontWeight.Medium), color = FairColors.TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = FairColors.TextHint) },
                        trailingIcon = {
                            Text(
                                text = if (passwordVisible) "HIDE" else "SHOW",
                                modifier = Modifier.clickable { passwordVisible = !passwordVisible }.padding(end = 12.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = FairColors.TextHint
                            )
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        isError = passwordError,
                        supportingText = {
                            if (passwordError) Text("Password is required", color = FairColors.DangerRed)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = FairColors.ScreenBg,
                            unfocusedContainerColor = FairColors.ScreenBg,
                            focusedBorderColor = FairColors.Border,
                            unfocusedBorderColor = FairColors.Border,
                            errorBorderColor = FairColors.DangerRed
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            showErrors = true
                            if (email.isNotBlank() && password.isNotBlank()) {
                                navigator.replace(DashboardScreen())
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FairColors.NavyDark)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Login as Doctor", style = FairTypography.BodyLarge.copy(fontWeight = FontWeight.Medium), color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Demo - any credentials accepted",
                        style = FairTypography.LabelSmall,
                        color = FairColors.TextHint,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.weight(1f))
                    DisclaimerText()
                }
            }
        }
    }
}
