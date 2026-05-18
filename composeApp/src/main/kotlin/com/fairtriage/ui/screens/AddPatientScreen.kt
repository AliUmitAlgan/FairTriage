package com.fairtriage.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.fairtriage.ui.components.*
import kotlin.math.roundToInt

class AddPatientScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { AddPatientScreenModel() }
        val actionState by screenModel.submitState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }

        var fullName by remember { mutableStateOf("") }
        var ageStr by remember { mutableStateOf("") }
        var gender by remember { mutableStateOf("Female") }
        var selectedSymptoms by remember { mutableStateOf(setOf<String>()) }
        var clinicalNote by remember { mutableStateOf("") }
        var painLevel by remember { mutableStateOf(0f) }
        var fever by remember { mutableStateOf(false) }
        var heartRateStr by remember { mutableStateOf("") }
        var sysBPStr by remember { mutableStateOf("") }
        var diaBPStr by remember { mutableStateOf("") }
        var hasChronic by remember { mutableStateOf(false) }
        var selectedChronicConditions by remember { mutableStateOf(setOf<String>()) }
        var chronicNote by remember { mutableStateOf("") }
        var mockImageScore by remember { mutableStateOf(0.0f) }
        var showErrors by remember { mutableStateOf(false) }
        val symptomGroups = remember {
            listOf(
                ClinicalOptionGroup(
                    title = "Pain & trauma",
                    options = listOf("Chest pain", "Abdominal pain", "Headache", "Trauma or injury", "Severe localized pain")
                ),
                ClinicalOptionGroup(
                    title = "Infection & respiratory",
                    options = listOf("Fever or chills", "Cough", "Shortness of breath", "Nausea or vomiting", "Rash or swelling")
                ),
                ClinicalOptionGroup(
                    title = "Neurologic & cardiac",
                    options = listOf("Dizziness or fainting", "Confusion", "Weakness or numbness", "Palpitations", "Seizure concern")
                ),
                ClinicalOptionGroup(
                    title = "General risk signs",
                    options = listOf("Bleeding", "Dehydration", "Severe fatigue", "Worsening symptoms", "Other clinical concern")
                )
            )
        }
        val chronicGroups = remember {
            listOf(
                ClinicalOptionGroup(
                    title = "High-risk chronic disease",
                    options = listOf("Heart disease", "Kidney disease", "Cancer treatment", "Immunosuppressed", "Pregnancy risk")
                ),
                ClinicalOptionGroup(
                    title = "Moderate-risk condition",
                    options = listOf("Diabetes", "Hypertension", "Asthma or COPD", "Neurologic disease")
                ),
                ClinicalOptionGroup(
                    title = "Ongoing care context",
                    options = listOf("Uses blood thinners", "Recent surgery", "Frequent ED visits", "Medication allergy")
                )
            )
        }

        LaunchedEffect(actionState) {
            when (val currentAction = actionState) {
                SubmitState.Success -> navigator.push(QueueScreen())
                is SubmitState.Error -> {
                    snackbarHostState.showSnackbar(currentAction.message)
                    screenModel.clearError()
                }
                else -> Unit
            }
        }

        Scaffold(
            topBar = { FairTriageTopBar(title = "New patient", onBack = { navigator.pop() }) },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = FairColors.ScreenBg
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section: Personal information
                item {
                    StandardCard {
                        SectionCardTitle("Personal information", Icons.Default.Person)
                        val nameError = showErrors && fullName.isBlank()
                        OutlinedTextField(
                            value = fullName, onValueChange = { fullName = it },
                            label = { Text("Full name") }, modifier = Modifier.fillMaxWidth(),
                            isError = nameError, shape = RoundedCornerShape(12.dp)
                        )
                        if (nameError) Text("Name required", color = FairColors.DangerRed, fontSize = 12.sp)

                        Spacer(Modifier.height(12.dp))
                        val ageInt = ageStr.toIntOrNull()
                        val ageError = showErrors && (ageInt == null || ageInt !in 1..120)
                        OutlinedTextField(
                            value = ageStr, onValueChange = { ageStr = it },
                            label = { Text("Age (1-120)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(), isError = ageError, shape = RoundedCornerShape(12.dp)
                        )
                        if (ageError) Text("Age must be between 1 and 120", color = FairColors.DangerRed, fontSize = 12.sp)

                        Spacer(Modifier.height(12.dp))
                        Text("Gender", style = FairTypography.LabelSmall, color = FairColors.TextSecondary)
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            listOf("Male", "Female", "Other").forEach { g ->
                                OutlinedButton(
                                    onClick = { gender = g },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (gender == g) FairColors.InfoBlueBg else FairColors.Surface,
                                        contentColor = if (gender == g) FairColors.InfoBlueText else FairColors.TextSecondary
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (gender == g) FairColors.InfoBlueBorder else FairColors.Border)
                                ) {
                                    Text(g, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Section: Symptoms & pain
                item {
                    StandardCard {
                        SectionCardTitle("Symptoms & pain", Icons.Default.LocalHospital)
                        Text(
                            text = "Select one or more clinical findings",
                            style = FairTypography.BodyMedium,
                            color = FairColors.TextSecondary
                        )
                        Spacer(Modifier.height(10.dp))
                        symptomGroups.forEach { group ->
                            ClinicalOptionSection(
                                title = group.title,
                                options = group.options,
                                selectedOptions = selectedSymptoms,
                                onToggle = { option ->
                                    selectedSymptoms = if (option in selectedSymptoms) {
                                        selectedSymptoms - option
                                    } else {
                                        selectedSymptoms + option
                                    }
                                }
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                        val symptomsError = showErrors && selectedSymptoms.isEmpty()
                        if (symptomsError) {
                            Text("Select at least one symptom or clinical finding", color = FairColors.DangerRed, fontSize = 12.sp)
                        }

                        OutlinedTextField(
                            value = clinicalNote,
                            onValueChange = { clinicalNote = it },
                            label = { Text("Additional clinical note (optional)") },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(Modifier.height(16.dp))
                        val painColor = when (painLevel.toInt()) {
                            in 0..3 -> FairColors.StableFill
                            in 4..6 -> FairColors.UrgentFill
                            else -> FairColors.CriticalFill
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Pain level", style = FairTypography.BodyLarge)
                            Text(painLevel.toInt().toString(), style = FairTypography.ScoreBig, color = painColor)
                        }
                        Slider(
                            value = painLevel, onValueChange = { painLevel = it },
                            valueRange = 0f..10f, steps = 9,
                            colors = SliderDefaults.colors(thumbColor = painColor, activeTrackColor = painColor)
                        )

                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (fever) FairColors.CriticalTint else Color.Transparent, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Patient has fever", style = FairTypography.BodyLarge)
                            Switch(checked = fever, onCheckedChange = { fever = it })
                        }
                    }
                }

                // Section: Vital signs
                item {
                    StandardCard {
                        SectionCardTitle("Vital signs", Icons.Default.Favorite)
                        val hr = heartRateStr.toIntOrNull()
                        val hrError = showErrors && (hr == null || hr !in 30..250)
                        val hrStatus = heartRateStatus(hr)
                        OutlinedTextField(
                            value = heartRateStr, onValueChange = { heartRateStr = it },
                            label = { Text("Heart rate (30-250)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(), isError = hrError, shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (hrError) FairColors.DangerRed else hrStatus.color,
                                unfocusedBorderColor = if (hrError) FairColors.DangerRed else hrStatus.color
                            )
                        )
                        if (hrError) Text("Heart rate must be between 30 and 250", color = FairColors.DangerRed, fontSize = 12.sp)
                        VitalSignalStatusChip(label = "Heart rate", status = hrStatus)

                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val sys = sysBPStr.toIntOrNull()
                            val sysError = showErrors && (sys == null || sys !in 50..250)
                            val dia = diaBPStr.toIntOrNull()
                            val diaError = showErrors && dia == null
                            val sysStatus = systolicStatus(sys)
                            val diaStatus = diastolicStatus(dia)
                            OutlinedTextField(
                                value = sysBPStr, onValueChange = { sysBPStr = it },
                                label = { Text("Systolic BP (50-250)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f), isError = sysError, shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (sysError) FairColors.DangerRed else sysStatus.color,
                                    unfocusedBorderColor = if (sysError) FairColors.DangerRed else sysStatus.color
                                )
                            )

                            OutlinedTextField(
                                value = diaBPStr, onValueChange = { diaBPStr = it },
                                label = { Text("Diastolic BP") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f), isError = diaError, shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (diaError) FairColors.DangerRed else diaStatus.color,
                                    unfocusedBorderColor = if (diaError) FairColors.DangerRed else diaStatus.color
                                )
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            VitalSignalStatusChip(label = "Systolic", status = systolicStatus(sysBPStr.toIntOrNull()), modifier = Modifier.weight(1f))
                            VitalSignalStatusChip(label = "Diastolic", status = diastolicStatus(diaBPStr.toIntOrNull()), modifier = Modifier.weight(1f))
                        }
                        val sys = sysBPStr.toIntOrNull()
                        val dia = diaBPStr.toIntOrNull()
                        if (showErrors && (sys == null || sys !in 50..250)) {
                            Text("Systolic BP must be between 50 and 250", color = FairColors.DangerRed, fontSize = 12.sp)
                        }
                        if (showErrors && dia == null) {
                            Text("Diastolic BP is required", color = FairColors.DangerRed, fontSize = 12.sp)
                        }
                    }
                }

                // Section: Medical history
                item {
                    StandardCard {
                        SectionCardTitle("Medical history", Icons.Default.History)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Has chronic disease", style = FairTypography.BodyLarge)
                            Switch(
                                checked = hasChronic,
                                onCheckedChange = {
                                    hasChronic = it
                                    if (!it) {
                                        selectedChronicConditions = emptySet()
                                        chronicNote = ""
                                    }
                                }
                            )
                        }
                        AnimatedVisibility(visible = hasChronic) {
                            Column(modifier = Modifier.padding(top = 10.dp)) {
                                Text(
                                    text = "Select relevant history flags",
                                    style = FairTypography.BodyMedium,
                                    color = FairColors.TextSecondary
                                )
                                Spacer(Modifier.height(10.dp))
                                chronicGroups.forEachIndexed { index, group ->
                                    ClinicalOptionSection(
                                        title = group.title,
                                        options = group.options,
                                        selectedOptions = selectedChronicConditions,
                                        selectedColor = when (index) {
                                            0 -> FairColors.CriticalFill
                                            1 -> FairColors.UrgentFill
                                            else -> FairColors.StableFill
                                        },
                                        selectedBg = when (index) {
                                            0 -> FairColors.CriticalTint
                                            1 -> FairColors.UrgentTint
                                            else -> FairColors.StableTint
                                        },
                                        onToggle = { option ->
                                            selectedChronicConditions = if (option in selectedChronicConditions) {
                                                selectedChronicConditions - option
                                            } else {
                                                selectedChronicConditions + option
                                            }
                                        }
                                    )
                                    Spacer(Modifier.height(12.dp))
                                }
                                val chronicError = showErrors && hasChronic && selectedChronicConditions.isEmpty() && chronicNote.isBlank()
                                if (chronicError) {
                                    Text("Select at least one history flag or add a note", color = FairColors.DangerRed, fontSize = 12.sp)
                                }
                                OutlinedTextField(
                                    value = chronicNote,
                                    onValueChange = { chronicNote = it },
                                    label = { Text("Additional history note (optional)") },
                                    minLines = 2,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }
                }

                // Section: Image analysis
                item {
                    StandardCard {
                        SectionCardTitle("Image analysis (mock)", Icons.Default.Camera)
                        val mockPercent = (mockImageScore * 100).roundToInt()
                        val imgColor = when {
                            mockPercent < 30 -> FairColors.StableFill
                            mockPercent < 70 -> FairColors.UrgentFill
                            else -> FairColors.CriticalFill
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Mock image analysis score", style = FairTypography.BodyLarge)
                            Text("$mockPercent%", style = FairTypography.ScoreBig, color = imgColor)
                        }
                        Slider(
                            value = mockImageScore, onValueChange = { mockImageScore = it },
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(thumbColor = imgColor, activeTrackColor = imgColor)
                        )
                    }
                }

                item {
                    val isLoading = actionState == SubmitState.Loading
                    Button(
                        onClick = {
                            showErrors = true
                            val symptomText = buildList {
                                addAll(selectedSymptoms)
                                if (clinicalNote.isNotBlank()) add("Additional note: ${clinicalNote.trim()}")
                            }.joinToString("; ")
                            val age = ageStr.toIntOrNull() ?: return@Button
                            if (age !in 1..120) return@Button
                            val hr = heartRateStr.toIntOrNull() ?: return@Button
                            if (hr !in 30..250) return@Button
                            val sys = sysBPStr.toIntOrNull() ?: return@Button
                            if (sys !in 50..250) return@Button
                            val dia = diaBPStr.toIntOrNull() ?: return@Button
                            if (fullName.isBlank()) return@Button
                            if (selectedSymptoms.isEmpty()) return@Button
                            if (hasChronic && selectedChronicConditions.isEmpty() && chronicNote.isBlank()) return@Button
                            val chronicDescription = buildList {
                                addAll(selectedChronicConditions)
                                if (chronicNote.isNotBlank()) add("Additional note: ${chronicNote.trim()}")
                            }.joinToString("; ")
                            
                            val req = CreatePatientRequest(
                                full_name = fullName,
                                age = age,
                                gender = gender,
                                symptoms_description = symptomText,
                                pain_level = painLevel.toInt(),
                                fever = fever,
                                heart_rate = hr,
                                blood_pressure_systolic = sys,
                                blood_pressure_diastolic = dia,
                                has_chronic_disease = hasChronic,
                                chronic_disease_description = if (hasChronic) chronicDescription else null,
                                image_score = mockImageScore.toDouble()
                            )
                            screenModel.submit(req)
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FairColors.NavyDark)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text("Submit & calculate risk", style = FairTypography.BodyLarge.copy(fontWeight = FontWeight.Medium), color = Color.White)
                        }
                    }
                }

                item { DisclaimerText() }
            }
        }
    }

    private data class ClinicalOptionGroup(
        val title: String,
        val options: List<String>
    )

    @Composable
    private fun ClinicalOptionSection(
        title: String,
        options: List<String>,
        selectedOptions: Set<String>,
        selectedColor: Color = FairColors.InfoBlueText,
        selectedBg: Color = FairColors.InfoBlueBg,
        onToggle: (String) -> Unit
    ) {
        Text(
            text = title,
            style = FairTypography.LabelLarge,
            color = FairColors.InfoBlueDark,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.chunked(2).forEach { rowOptions ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    rowOptions.forEach { option ->
                        ClinicalOptionCard(
                            text = option,
                            selected = option in selectedOptions,
                            selectedColor = selectedColor,
                            selectedBg = selectedBg,
                            onClick = { onToggle(option) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowOptions.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }

    @Composable
    private fun ClinicalOptionCard(
        text: String,
        selected: Boolean,
        selectedColor: Color = FairColors.InfoBlueText,
        selectedBg: Color = FairColors.InfoBlueBg,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Row(
            modifier = modifier
                .heightIn(min = 46.dp)
                .clickable(onClick = onClick)
                .background(if (selected) selectedBg else Color.White, RoundedCornerShape(12.dp))
                .border(1.dp, if (selected) selectedColor else FairColors.Border, RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(if (selected) selectedColor else Color.White, RoundedCornerShape(6.dp))
                    .border(1.dp, if (selected) selectedColor else FairColors.Border, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                }
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = text,
                color = if (selected) selectedColor else FairColors.TextPrimary,
                style = FairTypography.BodyMedium.copy(fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal)
            )
        }
    }

    private data class VitalSignalStatus(
        val text: String,
        val color: Color,
        val tint: Color
    )

    private fun heartRateStatus(value: Int?): VitalSignalStatus = when {
        value == null -> VitalSignalStatus("Not entered", FairColors.Border, FairColors.Surface)
        value in 60..100 -> VitalSignalStatus("Normal", FairColors.StableFill, FairColors.StableTint)
        value in 50..59 || value in 101..120 -> VitalSignalStatus("Watch", FairColors.UrgentFill, FairColors.UrgentTint)
        else -> VitalSignalStatus("High concern", FairColors.CriticalFill, FairColors.CriticalTint)
    }

    private fun systolicStatus(value: Int?): VitalSignalStatus = when {
        value == null -> VitalSignalStatus("Not entered", FairColors.Border, FairColors.Surface)
        value in 90..120 -> VitalSignalStatus("Normal", FairColors.StableFill, FairColors.StableTint)
        value in 80..89 || value in 121..159 -> VitalSignalStatus("Watch", FairColors.UrgentFill, FairColors.UrgentTint)
        else -> VitalSignalStatus("High concern", FairColors.CriticalFill, FairColors.CriticalTint)
    }

    private fun diastolicStatus(value: Int?): VitalSignalStatus = when {
        value == null -> VitalSignalStatus("Not entered", FairColors.Border, FairColors.Surface)
        value in 60..80 -> VitalSignalStatus("Normal", FairColors.StableFill, FairColors.StableTint)
        value in 50..59 || value in 81..99 -> VitalSignalStatus("Watch", FairColors.UrgentFill, FairColors.UrgentTint)
        else -> VitalSignalStatus("High concern", FairColors.CriticalFill, FairColors.CriticalTint)
    }

    @Composable
    private fun VitalSignalStatusChip(
        label: String,
        status: VitalSignalStatus,
        modifier: Modifier = Modifier
    ) {
        Row(
            modifier = modifier
                .padding(top = 6.dp)
                .background(status.tint, RoundedCornerShape(10.dp))
                .border(1.dp, status.color.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = FairTypography.LabelSmall, color = FairColors.TextSecondary)
            Text(status.text, style = FairTypography.LabelSmall.copy(fontWeight = FontWeight.Medium), color = status.color)
        }
    }
}
