package com.fairtriage.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.fairtriage.model.CreatePatientRequest
import com.fairtriage.screenmodel.AddPatientScreenModel
import com.fairtriage.screenmodel.SubmitState
import com.fairtriage.ui.components.AppBarStyle
import com.fairtriage.ui.components.FairColors
import com.fairtriage.ui.components.ScreenScaffold
import com.fairtriage.ui.components.SectionHeader
import kotlin.math.roundToInt

class AddPatientScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val snackbarHostState = remember { SnackbarHostState() }
        val screenModel = rememberScreenModel { AddPatientScreenModel() }
        val submitState by screenModel.submitState.collectAsState()

        var fullName by remember { mutableStateOf("") }
        var age by remember { mutableStateOf("") }
        var gender by remember { mutableStateOf("Male") }
        var symptoms by remember { mutableStateOf("") }
        var painLevel by remember { mutableFloatStateOf(5f) }
        var fever by remember { mutableStateOf(false) }
        var heartRate by remember { mutableStateOf("") }
        var systolic by remember { mutableStateOf("") }
        var diastolic by remember { mutableStateOf("") }
        var hasChronicDisease by remember { mutableStateOf(false) }
        var chronicDescription by remember { mutableStateOf("") }
        var imageScore by remember { mutableFloatStateOf(0.5f) }
        var showErrors by remember { mutableStateOf(false) }

        LaunchedEffect(submitState) {
            when (val state = submitState) {
                SubmitState.Success -> navigator.replace(QueueScreen())
                is SubmitState.Error -> {
                    snackbarHostState.showSnackbar(state.message)
                    screenModel.clearError()
                }
                SubmitState.Idle,
                SubmitState.Loading -> Unit
            }
        }

        val ageValue = age.toIntOrNull()
        val heartRateValue = heartRate.toIntOrNull()
        val systolicValue = systolic.toIntOrNull()
        val diastolicValue = diastolic.toIntOrNull()
        val fullNameError = showErrors && fullName.isBlank()
        val ageError = showErrors && (ageValue == null || ageValue !in 1..120)
        val heartRateError = showErrors && (heartRateValue == null || heartRateValue !in 30..250)
        val systolicError = showErrors && (systolicValue == null || systolicValue !in 50..250)
        val symptomsError = showErrors && symptoms.isBlank()
        val isSubmitting = submitState == SubmitState.Loading

        ScreenScaffold(
            title = "New Patient",
            showBack = true,
            appBarStyle = AppBarStyle.White,
            snackbarHostState = snackbarHostState
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item { SectionHeader("Personal Information") }
                item {
                    FormSection {
                        RequiredTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = "Full Name",
                            isError = fullNameError,
                            error = "Name cannot be empty."
                        )
                        RequiredTextField(
                            value = age,
                            onValueChange = { age = it.filter(Char::isDigit) },
                            label = "Age",
                            isError = ageError,
                            error = "Age must be between 1 and 120.",
                            keyboardType = KeyboardType.Number
                        )
                        DropdownSelector(
                            label = "Gender",
                            selected = gender,
                            options = listOf("Male", "Female", "Other"),
                            onSelected = { gender = it }
                        )
                        RequiredTextField(
                            value = symptoms,
                            onValueChange = { symptoms = it },
                            label = "Symptoms Description",
                            isError = symptomsError,
                            error = "Symptoms description is required.",
                            minLines = 3
                        )
                    }
                }

                item { SectionHeader("Vital Signs") }
                item {
                    FormSection {
                        ColoredSlider(
                            label = "Pain Level",
                            value = painLevel,
                            valueText = painLevel.roundToInt().toString(),
                            color = painColor(painLevel),
                            valueRange = 0f..10f,
                            steps = 10,
                            onValueChange = { painLevel = it }
                        )
                        SwitchRow(
                            icon = "F",
                            label = "Fever Present",
                            checked = fever,
                            alert = fever,
                            onCheckedChange = { fever = it }
                        )
                        RequiredTextField(
                            value = heartRate,
                            onValueChange = { heartRate = it.filter(Char::isDigit) },
                            label = "Heart Rate",
                            isError = heartRateError,
                            error = "Heart rate must be between 30 and 250.",
                            keyboardType = KeyboardType.Number
                        )
                        RequiredTextField(
                            value = systolic,
                            onValueChange = { systolic = it.filter(Char::isDigit) },
                            label = "Blood Pressure Systolic",
                            isError = systolicError,
                            error = "Systolic BP must be between 50 and 250.",
                            keyboardType = KeyboardType.Number
                        )
                        RequiredTextField(
                            value = diastolic,
                            onValueChange = { diastolic = it.filter(Char::isDigit) },
                            label = "Blood Pressure Diastolic",
                            isError = showErrors && diastolicValue == null,
                            error = "Diastolic BP is required.",
                            keyboardType = KeyboardType.Number
                        )
                    }
                }

                item { SectionHeader("Medical History") }
                item {
                    FormSection {
                        SwitchRow(
                            icon = "H",
                            label = "Has Chronic Disease",
                            checked = hasChronicDisease,
                            alert = false,
                            onCheckedChange = { hasChronicDisease = it }
                        )
                        AnimatedVisibility(visible = hasChronicDisease) {
                            OutlinedTextField(
                                value = chronicDescription,
                                onValueChange = { chronicDescription = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Chronic Disease Description") },
                                minLines = 2,
                                shape = RoundedCornerShape(8.dp),
                                colors = fieldColors()
                            )
                        }
                        ColoredSlider(
                            label = "Mock Image Analysis Score",
                            value = imageScore,
                            valueText = "${(imageScore * 100).roundToInt()}%",
                            color = imageScoreColor(imageScore),
                            valueRange = 0f..1f,
                            steps = 0,
                            onValueChange = { imageScore = it }
                        )
                    }
                }

                item {
                    Button(
                        onClick = {
                            showErrors = true
                            val valid = fullName.isNotBlank() &&
                                ageValue != null && ageValue in 1..120 &&
                                symptoms.isNotBlank() &&
                                heartRateValue != null && heartRateValue in 30..250 &&
                                systolicValue != null && systolicValue in 50..250 &&
                                diastolicValue != null
                            if (valid) {
                                screenModel.submit(
                                    CreatePatientRequest(
                                        full_name = fullName.trim(),
                                        age = ageValue,
                                        gender = gender,
                                        symptoms_description = symptoms.trim(),
                                        pain_level = painLevel.roundToInt(),
                                        fever = fever,
                                        heart_rate = heartRateValue,
                                        blood_pressure_systolic = systolicValue,
                                        blood_pressure_diastolic = diastolicValue,
                                        has_chronic_disease = hasChronicDisease,
                                        chronic_disease_description = chronicDescription.takeIf { value ->
                                            hasChronicDisease && value.isNotBlank()
                                        }?.trim(),
                                        image_score = imageScore.toDouble()
                                    )
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .height(56.dp),
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FairColors.PrimaryBlue,
                            contentColor = Color.White
                        )
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        }
                        Text("Submit & Calculate Risk", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun FormSection(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .background(FairColors.Surface, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = content
    )
}

@Composable
private fun RequiredTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean,
    error: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    minLines: Int = 1
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            isError = isError,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            minLines = minLines,
            shape = RoundedCornerShape(8.dp),
            colors = fieldColors()
        )
        if (isError) {
            Text(text = error, color = FairColors.CriticalRed, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ColoredSlider(
    label: String,
    value: Float,
    valueText: String,
    color: Color,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, color = FairColors.TextSecondary, fontSize = 12.sp)
            Text(text = valueText, color = color, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = FairColors.Divider
            )
        )
    }
}

@Composable
private fun SwitchRow(
    icon: String,
    label: String,
    checked: Boolean,
    alert: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (alert) FairColors.CriticalTint else FairColors.Surface,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, color = if (alert) FairColors.CriticalRed else FairColors.PrimaryBlue, fontWeight = FontWeight.Bold)
            Text(label, color = FairColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = if (alert) FairColors.CriticalRed else FairColors.SecondaryTeal,
                uncheckedTrackColor = FairColors.Divider
            )
        )
    }
}

@Composable
private fun DropdownSelector(
    label: String,
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = label, color = FairColors.TextSecondary, fontSize = 12.sp)
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(selected, color = FairColors.TextPrimary)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
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

private fun painColor(value: Float): Color {
    return when {
        value <= 3f -> FairColors.StableGreen
        value <= 6f -> FairColors.UrgentOrange
        else -> FairColors.CriticalRed
    }
}

private fun imageScoreColor(value: Float): Color {
    return when {
        value < 0.34f -> FairColors.StableGreen
        value < 0.67f -> FairColors.UrgentOrange
        else -> FairColors.CriticalRed
    }
}
