package com.fairtriage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

class LoginScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }
        var showErrors by remember { mutableStateOf(false) }
        val emailError = showErrors && email.isBlank()
        val passwordError = showErrors && password.isBlank()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(FairColors.Background)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.30f)
                    .background(FairColors.PrimaryBlue),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("+", color = Color.White, fontSize = 54.sp, fontWeight = FontWeight.Black)
                    Text(
                        text = "FairTriage",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.70f)
                    .background(
                        color = FairColors.Surface,
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
                    .padding(horizontal = 24.dp, vertical = 26.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Doctor Login",
                    color = FairColors.TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Emergency Department Access",
                    color = FairColors.TextSecondary,
                    fontSize = 13.sp
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Email Address") },
                    leadingIcon = { Text("@", color = FairColors.PrimaryBlue, fontWeight = FontWeight.Bold) },
                    singleLine = true,
                    isError = emailError,
                    shape = RoundedCornerShape(8.dp),
                    colors = fieldColors()
                )
                if (emailError) {
                    Text("Email address is required.", color = FairColors.CriticalRed, fontSize = 12.sp)
                }

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Password") },
                    leadingIcon = { Text("L", color = FairColors.PrimaryBlue, fontWeight = FontWeight.Bold) },
                    trailingIcon = {
                        Text(
                            text = if (passwordVisible) "Hide" else "Show",
                            color = FairColors.PrimaryBlue,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .clickable { passwordVisible = !passwordVisible }
                                .padding(end = 8.dp)
                        )
                    },
                    singleLine = true,
                    isError = passwordError,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(8.dp),
                    colors = fieldColors()
                )
                if (passwordError) {
                    Text("Password is required.", color = FairColors.CriticalRed, fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        showErrors = true
                        if (email.isNotBlank() && password.isNotBlank()) {
                            navigator.replace(DashboardScreen())
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FairColors.PrimaryBlue,
                        contentColor = Color.White
                    )
                ) {
                    Text("Login as Doctor", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = "Demo prototype - any credentials accepted",
                    modifier = Modifier.fillMaxWidth(),
                    color = FairColors.TextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.weight(1f))
                DisclaimerText()
            }
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = FairColors.PrimaryBlue,
    focusedLabelColor = FairColors.PrimaryBlue,
    cursorColor = FairColors.PrimaryBlue,
    errorBorderColor = FairColors.CriticalRed,
    errorLabelColor = FairColors.CriticalRed
)
