package com.fairtriage.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
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
import com.fairtriage.core.ScreenState
import com.fairtriage.model.CreatePatientRequest
import com.fairtriage.model.Patient
import com.fairtriage.screenmodel.AddPatientScreenModel
import com.fairtriage.screenmodel.SubmitState
import com.fairtriage.ui.components.*
import kotlin.math.roundToInt

data class AddPatientScreen(
    private val patientId: Int? = null,
    private val overrideMode: Boolean = false
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { AddPatientScreenModel() }
        val actionState by screenModel.submitState.collectAsState()
        val patientState by screenModel.patientState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        val editingPatientId = if (overrideMode) patientId else null
        val isOverrideMode = editingPatientId != null

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
        var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
        var showErrors by remember { mutableStateOf(false) }
        val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedImageUri = uri
                val normalized = ((uri.toString().hashCode() and Int.MAX_VALUE) % 86 + 10) / 100f
                mockImageScore = normalized.coerceIn(0.1f, 0.95f)
            }
        }
        val symptomGroups = remember {
            listOf(
                ClinicalOptionGroup(
                    title = "Primary survey: consciousness / ABC",
                    options = listOf(
                        "Unconscious or unresponsive",
                        "Reduced consciousness",
                        "Airway obstruction",
                        "Severe breathing difficulty",
                        "Cyanosis",
                        "Signs of shock",
                        "Severe active bleeding",
                        "Cardiac arrest concern"
                    )
                ),
                ClinicalOptionGroup(
                    title = "Cardiovascular & circulation",
                    options = listOf(
                        "Crushing chest pressure",
                        "Radiating chest pain",
                        "Syncope after exertion",
                        "Severe hypertension symptoms",
                        "Cold clammy skin",
                        "Leg swelling with breathlessness",
                        "Calf pain with swelling",
                        "Pacemaker or ICD symptom"
                    )
                ),
                ClinicalOptionGroup(
                    title = "Pain & trauma",
                    options = listOf(
                        "Chest pain",
                        "Abdominal pain",
                        "Severe localized pain",
                        "Arm or leg pain",
                        "Back pain",
                        "Headache",
                        "Trauma or injury",
                        "Head injury",
                        "Fracture or deformity",
                        "Burn injury"
                    )
                ),
                ClinicalOptionGroup(
                    title = "Wounds / burns / skin integrity",
                    options = listOf(
                        "Deep wound",
                        "Infected wound",
                        "Abscess",
                        "Cellulitis spreading",
                        "Pressure sore",
                        "Large burn area",
                        "Electrical burn",
                        "Frostbite"
                    )
                ),
                ClinicalOptionGroup(
                    title = "Infection & respiratory",
                    options = listOf(
                        "Fever or chills",
                        "Cough",
                        "Shortness of breath",
                        "Wheezing or asthma attack",
                        "Productive cough",
                        "Sore throat",
                        "Flu-like symptoms",
                        "Suspected sepsis",
                        "Rash or swelling"
                    )
                ),
                ClinicalOptionGroup(
                    title = "ENT / eye / dental",
                    options = listOf(
                        "Sudden vision loss",
                        "Eye trauma",
                        "Chemical eye exposure",
                        "Severe eye pain",
                        "Severe nosebleed",
                        "Ear pain",
                        "Dental abscess",
                        "Facial swelling"
                    )
                ),
                ClinicalOptionGroup(
                    title = "Neurologic & cardiac",
                    options = listOf(
                        "Confusion",
                        "Seizure concern",
                        "Weakness or numbness",
                        "Facial droop or speech difficulty",
                        "Severe sudden headache",
                        "Dizziness or fainting",
                        "Vision changes",
                        "Palpitations",
                        "Irregular heartbeat"
                    )
                ),
                ClinicalOptionGroup(
                    title = "Mental health & safety",
                    options = listOf(
                        "Suicidal thoughts",
                        "Self-harm injury",
                        "Violent or unsafe behavior",
                        "Acute psychosis",
                        "Panic attack",
                        "Severe insomnia with distress",
                        "Substance withdrawal",
                        "Social safety concern"
                    )
                ),
                ClinicalOptionGroup(
                    title = "Gastrointestinal / renal",
                    options = listOf(
                        "Nausea or vomiting",
                        "Persistent vomiting",
                        "Diarrhea",
                        "Dehydration",
                        "Blood in vomit or stool",
                        "Severe abdominal guarding",
                        "Urinary pain",
                        "Flank pain"
                    )
                ),
                ClinicalOptionGroup(
                    title = "Urologic / reproductive",
                    options = listOf(
                        "Urinary retention",
                        "Testicular pain",
                        "Pelvic pain",
                        "Vaginal bleeding",
                        "Severe menstrual bleeding",
                        "Sexual assault concern",
                        "Postpartum bleeding",
                        "Reduced fetal movement"
                    )
                ),
                ClinicalOptionGroup(
                    title = "Allergy / toxicology / exposure",
                    options = listOf(
                        "Anaphylaxis concern",
                        "Poisoning or overdose",
                        "Medication reaction",
                        "Chemical exposure",
                        "Animal or insect bite",
                        "Alcohol or drug intoxication"
                    )
                ),
                ClinicalOptionGroup(
                    title = "Environmental / disaster exposure",
                    options = listOf(
                        "Smoke inhalation",
                        "Heat stroke concern",
                        "Hypothermia concern",
                        "Drowning or submersion",
                        "Carbon monoxide exposure",
                        "Crush injury",
                        "Blast injury",
                        "Needs decontamination"
                    )
                ),
                ClinicalOptionGroup(
                    title = "Pregnancy / pediatric risk",
                    options = listOf(
                        "Pregnancy with bleeding",
                        "Pregnancy with abdominal pain",
                        "Newborn or infant concern",
                        "Child with poor intake",
                        "Child with persistent fever",
                        "Child with lethargy"
                    )
                ),
                ClinicalOptionGroup(
                    title = "Functional / frailty concerns",
                    options = listOf(
                        "Fall in older adult",
                        "Unable to perform daily activities",
                        "New confusion in older adult",
                        "Poor oral intake",
                        "Caregiver concern",
                        "Unsafe discharge risk"
                    )
                ),
                ClinicalOptionGroup(
                    title = "General risk signs",
                    options = listOf(
                        "Bleeding",
                        "Severe fatigue",
                        "Worsening symptoms",
                        "Unable to walk",
                        "Severe anxiety or agitation",
                        "Needs isolation",
                        "Other clinical concern"
                    )
                ),
                ClinicalOptionGroup(
                    title = "Mild / stable symptoms",
                    options = listOf(
                        "Mild arm pain",
                        "Mild back pain",
                        "Mild sore throat",
                        "Mild cough",
                        "Mild headache",
                        "Minor cut or abrasion",
                        "Mild skin irritation",
                        "Medication refill concern",
                        "Routine wound check",
                        "Stable chronic complaint"
                    )
                )
            )
        }
        val chronicGroups = remember {
            listOf(
                ClinicalOptionGroup(
                    title = "Cardiac / vascular history",
                    options = listOf(
                        "Heart disease",
                        "Prior heart attack",
                        "Heart failure",
                        "Arrhythmia history",
                        "Pacemaker or ICD",
                        "Valve disease",
                        "Peripheral vascular disease",
                        "History of blood clot"
                    )
                ),
                ClinicalOptionGroup(
                    title = "Neurologic / respiratory history",
                    options = listOf(
                        "Stroke history",
                        "Seizure disorder",
                        "Neurologic disease",
                        "Dementia or cognitive impairment",
                        "Asthma or COPD",
                        "Home oxygen use",
                        "Sleep apnea",
                        "Cystic fibrosis"
                    )
                ),
                ClinicalOptionGroup(
                    title = "Renal / cancer / immune risk",
                    options = listOf(
                        "Kidney disease",
                        "Dialysis patient",
                        "Cancer treatment",
                        "Immunosuppressed",
                        "Organ transplant",
                        "HIV or AIDS",
                        "Long-term steroid use",
                        "Autoimmune disease"
                    )
                ),
                ClinicalOptionGroup(
                    title = "Metabolic / endocrine / liver",
                    options = listOf(
                        "Diabetes",
                        "Insulin-dependent diabetes",
                        "Hypertension",
                        "Liver disease",
                        "Cirrhosis",
                        "Adrenal insufficiency",
                        "Thyroid disease",
                        "Severe obesity",
                        "Malnutrition risk"
                    )
                ),
                ClinicalOptionGroup(
                    title = "Bleeding / medication risk",
                    options = listOf(
                        "Uses blood thinners",
                        "Bleeding disorder",
                        "Sickle cell disease",
                        "Severe anemia history",
                        "Medication allergy",
                        "High-risk medication use",
                        "Recent medication change",
                        "No regular medication access"
                    )
                ),
                ClinicalOptionGroup(
                    title = "Pregnancy / age / functional risk",
                    options = listOf(
                        "Pregnancy risk",
                        "Postpartum under 6 weeks",
                        "Older adult frailty",
                        "Mobility limitation",
                        "Nursing home resident",
                        "Lives alone with limited support",
                        "Pediatric chronic illness",
                        "Developmental disability"
                    )
                ),
                ClinicalOptionGroup(
                    title = "Ongoing care context",
                    options = listOf(
                        "Recent surgery",
                        "Frequent ED visits",
                        "Recent hospitalization",
                        "Indwelling catheter",
                        "Central line or port",
                        "Feeding tube",
                        "Ventricular shunt",
                        "Recent chemotherapy",
                        "Recent trauma admission",
                        "Known infectious exposure",
                        "Homelessness or housing insecurity"
                    )
                )
            )
        }
        val allSymptomOptions = remember(symptomGroups) { symptomGroups.flatMap { it.options }.toSet() }
        val allChronicOptions = remember(chronicGroups) { chronicGroups.flatMap { it.options }.toSet() }
        var populatedPatientId by remember { mutableStateOf<Int?>(null) }

        LaunchedEffect(editingPatientId) {
            editingPatientId?.let { screenModel.loadPatient(it) }
        }

        LaunchedEffect(patientState) {
            val patient = (patientState as? ScreenState.Success<Patient>)?.data ?: return@LaunchedEffect
            if (populatedPatientId == patient.id) return@LaunchedEffect
            fullName = patient.full_name
            ageStr = patient.age.toString()
            gender = patient.gender
            val symptomParse = parseChecklistText(patient.symptoms_description, allSymptomOptions)
            selectedSymptoms = symptomParse.selected
            clinicalNote = symptomParse.note
            painLevel = patient.pain_level.toFloat()
            fever = patient.fever
            heartRateStr = nearestVitalValue(heartRateOptions(), patient.heart_rate).toString()
            sysBPStr = nearestVitalValue(systolicOptions(), patient.blood_pressure_systolic).toString()
            diaBPStr = nearestVitalValue(diastolicOptions(), patient.blood_pressure_diastolic).toString()
            hasChronic = patient.has_chronic_disease
            val chronicParse = parseChecklistText(patient.chronic_disease_description.orEmpty(), allChronicOptions)
            selectedChronicConditions = chronicParse.selected
            chronicNote = chronicParse.note
            mockImageScore = patient.image_score.toFloat().coerceIn(0f, 1f)
            populatedPatientId = patient.id
        }

        LaunchedEffect(actionState) {
            when (val currentAction = actionState) {
                is SubmitState.Success -> {
                    if (isOverrideMode) {
                        navigator.pop()
                    } else {
                        val newPatientId = currentAction.patientId
                        if (newPatientId != null) {
                            navigator.replace(PatientDetailScreen(newPatientId))
                        } else {
                            navigator.replace(DashboardScreen())
                        }
                    }
                }
                is SubmitState.Error -> {
                    snackbarHostState.showSnackbar(currentAction.message)
                    screenModel.clearError()
                }
                else -> Unit
            }
        }

        Scaffold(
            topBar = { FairTriageTopBar(title = if (isOverrideMode) "Override patient data" else "New patient", onBack = { navigator.pop() }) },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = FairColors.ScreenBg
        ) { paddingValues ->
            if (isOverrideMode && patientState is ScreenState.Loading) {
                LoadingState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
                return@Scaffold
            }
            if (isOverrideMode && patientState is ScreenState.Error) {
                val message = (patientState as ScreenState.Error).message
                ErrorState(
                    errorMessage = message,
                    onRetry = { screenModel.loadPatient(editingPatientId) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
                return@Scaffold
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isOverrideMode) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = FairColors.WarningBg),
                            border = androidx.compose.foundation.BorderStroke(1.dp, FairColors.WarningBorder.copy(alpha = 0.45f))
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = FairColors.WarningBorder, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Edit clinical data for override review",
                                        style = FairTypography.BodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = FairColors.WarningText
                                    )
                                    Text(
                                        text = "Existing patient values are pre-selected. Saving will update backend data, recalculate risk, and log the clinician override.",
                                        style = FairTypography.LabelSmall,
                                        color = FairColors.TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

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
                                optionColor = ::clinicalFindingColor,
                                optionBg = ::clinicalFindingTint,
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
                            label = { Text("Clinical note") },
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
                        Text(
                            text = "Select the closest clinical range. The exact prototype value is sent to the backend for scoring.",
                            style = FairTypography.BodyMedium,
                            color = FairColors.TextSecondary
                        )
                        Spacer(Modifier.height(12.dp))
                        VitalOptionSection(
                            title = "Heart rate",
                            options = heartRateOptions(),
                            selectedValue = heartRateStr.toIntOrNull(),
                            onSelect = { option -> heartRateStr = option.value.toString() }
                        )
                        val hr = heartRateStr.toIntOrNull()
                        val hrError = showErrors && (hr == null || hr !in 30..250)
                        if (hrError) Text("Select a heart-rate category", color = FairColors.DangerRed, fontSize = 12.sp)

                        Spacer(Modifier.height(14.dp))
                        VitalOptionSection(
                            title = "Systolic blood pressure",
                            options = systolicOptions(),
                            selectedValue = sysBPStr.toIntOrNull(),
                            onSelect = { option -> sysBPStr = option.value.toString() }
                        )
                        val sys = sysBPStr.toIntOrNull()
                        if (showErrors && (sys == null || sys !in 50..250)) {
                            Text("Select a systolic BP category", color = FairColors.DangerRed, fontSize = 12.sp)
                        }

                        Spacer(Modifier.height(14.dp))
                        VitalOptionSection(
                            title = "Diastolic blood pressure",
                            options = diastolicOptions(),
                            selectedValue = diaBPStr.toIntOrNull(),
                            onSelect = { option -> diaBPStr = option.value.toString() }
                        )
                        val dia = diaBPStr.toIntOrNull()
                        if (showErrors && (dia == null || dia !in 30..180)) {
                            Text("Select a diastolic BP category", color = FairColors.DangerRed, fontSize = 12.sp)
                        }

                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            VitalSignalStatusChip(label = "Heart", status = heartRateStatus(hr), modifier = Modifier.weight(1f))
                            VitalSignalStatusChip(label = "Systolic", status = systolicStatus(sys), modifier = Modifier.weight(1f))
                            VitalSignalStatusChip(label = "Diastolic", status = diastolicStatus(dia), modifier = Modifier.weight(1f))
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
                                        optionColor = {
                                            when (index) {
                                                0 -> FairColors.CriticalFill
                                                1 -> FairColors.UrgentFill
                                                else -> FairColors.StableFill
                                            }
                                        },
                                        optionBg = {
                                            when (index) {
                                                0 -> FairColors.CriticalTint
                                                1 -> FairColors.UrgentTint
                                                else -> FairColors.StableTint
                                            }
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
                                    label = { Text("Additional history note") },
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
                        OutlinedButton(
                            onClick = { imagePicker.launch("image/*") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, FairColors.InfoBlueBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = FairColors.InfoBlueText)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (selectedImageUri == null) "Upload clinical image" else "Image selected - replace")
                        }
                        if (selectedImageUri != null) {
                            Text(
                                text = "Local edge image estimate generated from selected image. Backend receives normalized image score.",
                                style = FairTypography.LabelSmall,
                                color = FairColors.TextSecondary,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        Spacer(Modifier.height(12.dp))
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
                            if (dia !in 30..180) return@Button
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
                            val targetPatientId = editingPatientId
                            if (targetPatientId != null) {
                                screenModel.submitOverride(
                                    patientId = targetPatientId,
                                    request = req,
                                    overrideReasons = buildOverrideReasons(
                                        selectedSymptoms = selectedSymptoms,
                                        painLevel = painLevel.toInt(),
                                        fever = fever,
                                        heartRate = hr,
                                        systolic = sys,
                                        diastolic = dia,
                                        hasChronic = hasChronic,
                                        selectedChronicConditions = selectedChronicConditions
                                    )
                                )
                            } else {
                                screenModel.submit(req)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FairColors.NavyDark)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text(
                                if (isOverrideMode) "Save override & recalculate" else "Submit & calculate risk",
                                style = FairTypography.BodyLarge.copy(fontWeight = FontWeight.Medium),
                                color = Color.White
                            )
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

    private data class ChecklistParse(
        val selected: Set<String>,
        val note: String
    )

    private data class VitalOption(
        val title: String,
        val subtitle: String,
        val value: Int,
        val color: Color,
        val tint: Color
    )

    private fun parseChecklistText(text: String, knownOptions: Set<String>): ChecklistParse {
        val knownByLower = knownOptions.associateBy { it.lowercase() }
        val selected = mutableSetOf<String>()
        val notes = mutableListOf<String>()

        text.split(";")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .forEach { part ->
                val cleaned = part.removePrefix("Additional note:").trim()
                val known = knownByLower[cleaned.lowercase()]
                if (known != null) {
                    selected += known
                } else {
                    notes += cleaned
                }
            }

        return ChecklistParse(selected = selected, note = notes.joinToString("; "))
    }

    private fun nearestVitalValue(options: List<VitalOption>, actualValue: Int): Int {
        return options.minByOrNull { kotlin.math.abs(it.value - actualValue) }?.value ?: actualValue
    }

    private fun buildOverrideReasons(
        selectedSymptoms: Set<String>,
        painLevel: Int,
        fever: Boolean,
        heartRate: Int,
        systolic: Int,
        diastolic: Int,
        hasChronic: Boolean,
        selectedChronicConditions: Set<String>
    ): List<String> {
        val reasons = mutableListOf("Doctor or nurse bedside assessment")
        if (selectedSymptoms.any { clinicalFindingColor(it) == FairColors.CriticalFill }) reasons += "Clinical deterioration observed"
        if (heartRate !in 60..100) reasons += "Abnormal heart rate"
        if (systolic !in 90..120 || diastolic !in 60..80) reasons += "Abnormal blood pressure"
        if (fever || selectedSymptoms.any { it.contains("Fever", ignoreCase = true) || it.contains("Cough", ignoreCase = true) }) reasons += "Persistent fever or infection concern"
        if (painLevel >= 7 || selectedSymptoms.any { it.contains("Severe", ignoreCase = true) }) reasons += "Pain level increased or uncontrolled"
        if (hasChronic && selectedChronicConditions.any { isHighRiskChronicCondition(it) }) reasons += "High-risk chronic disease"
        if (hasChronic && selectedChronicConditions.any { it.contains("Frequent ED", ignoreCase = true) }) reasons += "Requires faster physician review"
        return reasons.distinct().take(12)
    }

    private fun isHighRiskChronicCondition(option: String): Boolean {
        return option in setOf(
            "Heart disease",
            "Prior heart attack",
            "Heart failure",
            "History of blood clot",
            "Kidney disease",
            "Cancer treatment",
            "Immunosuppressed",
            "Pregnancy risk",
            "Stroke history",
            "Seizure disorder",
            "Organ transplant",
            "Dialysis patient",
            "Home oxygen use",
            "HIV or AIDS",
            "Long-term steroid use",
            "Insulin-dependent diabetes",
            "Bleeding disorder",
            "Sickle cell disease",
            "Postpartum under 6 weeks",
            "Central line or port",
            "Ventricular shunt",
            "Recent chemotherapy"
        )
    }

    @Composable
    private fun ClinicalOptionSection(
        title: String,
        options: List<String>,
        selectedOptions: Set<String>,
        selectedColor: Color = FairColors.InfoBlueText,
        selectedBg: Color = FairColors.InfoBlueBg,
        optionColor: (String) -> Color = { selectedColor },
        optionBg: (String) -> Color = { selectedBg },
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
                            indicatorColor = optionColor(option),
                            selectedColor = optionColor(option),
                            selectedBg = optionBg(option),
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
        indicatorColor: Color = FairColors.InfoBlueText,
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
                    .width(4.dp)
                    .height(24.dp)
                    .background(indicatorColor, RoundedCornerShape(12.dp))
            )
            Spacer(Modifier.width(8.dp))
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

    @Composable
    private fun VitalOptionSection(
        title: String,
        options: List<VitalOption>,
        selectedValue: Int?,
        onSelect: (VitalOption) -> Unit
    ) {
        Text(
            text = title,
            style = FairTypography.LabelLarge,
            color = FairColors.InfoBlueDark,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.chunked(2).forEach { rowOptions ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    rowOptions.forEach { option ->
                        VitalOptionCard(
                            option = option,
                            selected = selectedValue == option.value,
                            onClick = { onSelect(option) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowOptions.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }

    @Composable
    private fun VitalOptionCard(
        option: VitalOption,
        selected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Column(
            modifier = modifier
                .heightIn(min = 72.dp)
                .clickable(onClick = onClick)
                .background(if (selected) option.tint else Color.White, RoundedCornerShape(12.dp))
                .border(1.dp, if (selected) option.color else FairColors.Border, RoundedCornerShape(12.dp))
                .padding(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(option.color, RoundedCornerShape(10.dp))
                )
                Spacer(Modifier.width(7.dp))
                Text(
                    text = option.title,
                    color = if (selected) option.color else FairColors.TextPrimary,
                    style = FairTypography.BodyMedium.copy(fontWeight = FontWeight.Medium)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(option.subtitle, style = FairTypography.LabelSmall, color = FairColors.TextSecondary)
            Text("${option.value}", style = FairTypography.LabelSmall.copy(fontWeight = FontWeight.Medium), color = option.color)
        }
    }

    private fun heartRateOptions(): List<VitalOption> = listOf(
        VitalOption("Normal", "60-100 bpm", 82, FairColors.StableFill, FairColors.StableTint),
        VitalOption("Watch high", "101-120 bpm", 112, FairColors.UrgentFill, FairColors.UrgentTint),
        VitalOption("Watch low", "50-59 bpm", 55, FairColors.UrgentFill, FairColors.UrgentTint),
        VitalOption("Critical high", ">130 bpm", 142, FairColors.CriticalFill, FairColors.CriticalTint),
        VitalOption("Critical low", "<50 bpm", 42, FairColors.CriticalFill, FairColors.CriticalTint)
    )

    private fun systolicOptions(): List<VitalOption> = listOf(
        VitalOption("Normal", "90-120 mmHg", 115, FairColors.StableFill, FairColors.StableTint),
        VitalOption("Elevated", "121-159 mmHg", 145, FairColors.UrgentFill, FairColors.UrgentTint),
        VitalOption("Borderline low", "90-99 mmHg", 95, FairColors.UrgentFill, FairColors.UrgentTint),
        VitalOption("Severe high", ">=180 mmHg", 185, FairColors.CriticalFill, FairColors.CriticalTint),
        VitalOption("Hypotension", "<90 mmHg", 85, FairColors.CriticalFill, FairColors.CriticalTint)
    )

    private fun diastolicOptions(): List<VitalOption> = listOf(
        VitalOption("Normal", "60-80 mmHg", 75, FairColors.StableFill, FairColors.StableTint),
        VitalOption("Elevated", "81-99 mmHg", 92, FairColors.UrgentFill, FairColors.UrgentTint),
        VitalOption("Borderline low", "50-59 mmHg", 55, FairColors.UrgentFill, FairColors.UrgentTint),
        VitalOption("Severe high", ">=110 mmHg", 115, FairColors.CriticalFill, FairColors.CriticalTint),
        VitalOption("Very low", "<50 mmHg", 45, FairColors.CriticalFill, FairColors.CriticalTint)
    )

    private fun clinicalFindingColor(option: String): Color {
        val normalized = option.lowercase()
        return when {
            normalized in criticalClinicalFindings() -> FairColors.CriticalFill
            normalized in urgentClinicalFindings() -> FairColors.UrgentFill
            else -> FairColors.StableFill
        }
    }

    private fun criticalClinicalFindings(): Set<String> = setOf(
        "unconscious or unresponsive",
        "reduced consciousness",
        "airway obstruction",
        "severe breathing difficulty",
        "cyanosis",
        "signs of shock",
        "severe active bleeding",
        "cardiac arrest concern",
        "crushing chest pressure",
        "radiating chest pain",
        "cold clammy skin",
        "chest pain",
        "shortness of breath",
        "confusion",
        "seizure concern",
        "weakness or numbness",
        "facial droop or speech difficulty",
        "severe sudden headache",
        "sudden vision loss",
        "chemical eye exposure",
        "bleeding",
        "blood in vomit or stool",
        "severe abdominal guarding",
        "anaphylaxis concern",
        "poisoning or overdose",
        "pregnancy with bleeding",
        "postpartum bleeding",
        "child with lethargy",
        "severe localized pain",
        "suicidal thoughts",
        "self-harm injury",
        "violent or unsafe behavior",
        "smoke inhalation",
        "heat stroke concern",
        "hypothermia concern",
        "drowning or submersion",
        "carbon monoxide exposure",
        "crush injury",
        "blast injury",
        "needs decontamination",
        "worsening symptoms"
    )

    private fun urgentClinicalFindings(): Set<String> = setOf(
        "abdominal pain",
        "headache",
        "trauma or injury",
        "head injury",
        "fracture or deformity",
        "burn injury",
        "deep wound",
        "infected wound",
        "abscess",
        "cellulitis spreading",
        "pressure sore",
        "large burn area",
        "electrical burn",
        "frostbite",
        "arm or leg pain",
        "back pain",
        "syncope after exertion",
        "severe hypertension symptoms",
        "leg swelling with breathlessness",
        "calf pain with swelling",
        "pacemaker or icd symptom",
        "fever or chills",
        "cough",
        "wheezing or asthma attack",
        "productive cough",
        "suspected sepsis",
        "nausea or vomiting",
        "persistent vomiting",
        "diarrhea",
        "rash or swelling",
        "eye trauma",
        "severe eye pain",
        "severe nosebleed",
        "ear pain",
        "dental abscess",
        "facial swelling",
        "dizziness or fainting",
        "vision changes",
        "palpitations",
        "irregular heartbeat",
        "acute psychosis",
        "panic attack",
        "severe insomnia with distress",
        "substance withdrawal",
        "social safety concern",
        "dehydration",
        "flank pain",
        "urinary pain",
        "urinary retention",
        "testicular pain",
        "pelvic pain",
        "vaginal bleeding",
        "severe menstrual bleeding",
        "sexual assault concern",
        "reduced fetal movement",
        "medication reaction",
        "chemical exposure",
        "animal or insect bite",
        "alcohol or drug intoxication",
        "pregnancy with abdominal pain",
        "newborn or infant concern",
        "child with poor intake",
        "child with persistent fever",
        "fall in older adult",
        "unable to perform daily activities",
        "new confusion in older adult",
        "poor oral intake",
        "caregiver concern",
        "unsafe discharge risk",
        "severe fatigue",
        "unable to walk",
        "severe anxiety or agitation",
        "needs isolation",
        "other clinical concern"
    )

    private fun clinicalFindingTint(option: String): Color = when (clinicalFindingColor(option)) {
        FairColors.CriticalFill -> FairColors.CriticalTint
        FairColors.UrgentFill -> FairColors.UrgentTint
        else -> FairColors.StableTint
    }

    private data class VitalSignalStatus(
        val text: String,
        val color: Color,
        val tint: Color
    )

    private fun heartRateStatus(value: Int?): VitalSignalStatus = when (value) {
        null -> VitalSignalStatus("Select one", FairColors.Border, FairColors.Surface)
        in 60..100 -> VitalSignalStatus("Normal", FairColors.StableFill, FairColors.StableTint)
        in 50..59, in 101..120 -> VitalSignalStatus("Watch", FairColors.UrgentFill, FairColors.UrgentTint)
        else -> VitalSignalStatus("High concern", FairColors.CriticalFill, FairColors.CriticalTint)
    }

    private fun systolicStatus(value: Int?): VitalSignalStatus = when (value) {
        null -> VitalSignalStatus("Select one", FairColors.Border, FairColors.Surface)
        in 90..120 -> VitalSignalStatus("Normal", FairColors.StableFill, FairColors.StableTint)
        in 80..89, in 121..159 -> VitalSignalStatus("Watch", FairColors.UrgentFill, FairColors.UrgentTint)
        else -> VitalSignalStatus("High concern", FairColors.CriticalFill, FairColors.CriticalTint)
    }

    private fun diastolicStatus(value: Int?): VitalSignalStatus = when (value) {
        null -> VitalSignalStatus("Select one", FairColors.Border, FairColors.Surface)
        in 60..80 -> VitalSignalStatus("Normal", FairColors.StableFill, FairColors.StableTint)
        in 50..59, in 81..99 -> VitalSignalStatus("Watch", FairColors.UrgentFill, FairColors.UrgentTint)
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
