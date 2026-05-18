package com.fairtriage.core

import com.fairtriage.model.CreatePatientRequest
import com.fairtriage.model.Patient
import kotlin.math.round

object EdgeTriageEngine {
    private const val URGENT_RISK_FLOOR = 45.0
    private const val CRITICAL_RISK_FLOOR = 75.0

    private val triageRank = mapOf("Critical" to 0, "Urgent" to 1, "Stable" to 2)

    private val highRiskSymptoms = mapOf(
        "unconscious or unresponsive" to 35,
        "reduced consciousness" to 30,
        "airway obstruction" to 35,
        "severe breathing difficulty" to 30,
        "cyanosis" to 30,
        "signs of shock" to 35,
        "severe active bleeding" to 30,
        "cardiac arrest concern" to 40,
        "crushing chest pressure" to 22,
        "radiating chest pain" to 20,
        "syncope after exertion" to 16,
        "severe hypertension symptoms" to 12,
        "cold clammy skin" to 18,
        "leg swelling with breathlessness" to 14,
        "calf pain with swelling" to 10,
        "pacemaker or icd symptom" to 12,
        "chest pain" to 20,
        "abdominal pain" to 6,
        "arm or leg pain" to 4,
        "back pain" to 4,
        "headache" to 4,
        "shortness of breath" to 20,
        "confusion" to 18,
        "seizure" to 18,
        "weakness or numbness" to 15,
        "facial droop or speech difficulty" to 20,
        "severe sudden headache" to 18,
        "bleeding" to 12,
        "blood in vomit or stool" to 15,
        "severe abdominal guarding" to 15,
        "anaphylaxis concern" to 30,
        "poisoning or overdose" to 18,
        "pregnancy with bleeding" to 20,
        "child with lethargy" to 18,
        "trauma" to 12,
        "head injury" to 14,
        "fracture or deformity" to 10,
        "burn injury" to 10,
        "deep wound" to 6,
        "infected wound" to 8,
        "abscess" to 5,
        "cellulitis spreading" to 10,
        "pressure sore" to 5,
        "large burn area" to 16,
        "electrical burn" to 20,
        "frostbite" to 10,
        "severe localized pain" to 10,
        "wheezing or asthma attack" to 12,
        "fever or chills" to 8,
        "cough" to 3,
        "productive cough" to 5,
        "suspected sepsis" to 25,
        "rash or swelling" to 4,
        "dizziness or fainting" to 6,
        "vision changes" to 6,
        "palpitations" to 6,
        "irregular heartbeat" to 8,
        "persistent vomiting" to 8,
        "nausea or vomiting" to 5,
        "diarrhea" to 3,
        "dehydration" to 8,
        "urinary pain" to 3,
        "flank pain" to 5,
        "chemical exposure" to 10,
        "medication reaction" to 6,
        "animal or insect bite" to 6,
        "alcohol or drug intoxication" to 8,
        "pregnancy with abdominal pain" to 12,
        "newborn or infant concern" to 16,
        "child with poor intake" to 8,
        "child with persistent fever" to 10,
        "sudden vision loss" to 16,
        "chemical eye exposure" to 18,
        "severe eye pain" to 10,
        "severe nosebleed" to 8,
        "ear pain" to 3,
        "dental abscess" to 6,
        "facial swelling" to 8,
        "suicidal thoughts" to 18,
        "self-harm injury" to 18,
        "violent or unsafe behavior" to 16,
        "acute psychosis" to 12,
        "panic attack" to 4,
        "severe insomnia with distress" to 3,
        "substance withdrawal" to 12,
        "social safety concern" to 4,
        "urinary retention" to 8,
        "testicular pain" to 12,
        "pelvic pain" to 5,
        "vaginal bleeding" to 8,
        "severe menstrual bleeding" to 10,
        "sexual assault concern" to 10,
        "postpartum bleeding" to 22,
        "reduced fetal movement" to 10,
        "smoke inhalation" to 24,
        "heat stroke concern" to 24,
        "hypothermia concern" to 18,
        "drowning or submersion" to 24,
        "carbon monoxide exposure" to 24,
        "crush injury" to 20,
        "blast injury" to 20,
        "needs decontamination" to 14,
        "fall in older adult" to 10,
        "unable to perform daily activities" to 5,
        "new confusion in older adult" to 18,
        "poor oral intake" to 6,
        "caregiver concern" to 3,
        "unsafe discharge risk" to 6,
        "unable to walk" to 8,
        "severe fatigue" to 4,
        "severe anxiety or agitation" to 5,
        "needs isolation" to 6,
        "other clinical concern" to 4,
        "worsening symptoms" to 10
    )

    private val highRiskHistory = mapOf(
        "heart disease" to 20,
        "prior heart attack" to 20,
        "heart failure" to 18,
        "arrhythmia history" to 12,
        "pacemaker or icd" to 12,
        "valve disease" to 10,
        "peripheral vascular disease" to 10,
        "history of blood clot" to 12,
        "kidney disease" to 18,
        "cancer treatment" to 18,
        "immunosuppressed" to 18,
        "pregnancy risk" to 15,
        "stroke history" to 15,
        "seizure disorder" to 12,
        "neurologic disease" to 8,
        "dementia or cognitive impairment" to 10,
        "organ transplant" to 18,
        "dialysis patient" to 18,
        "home oxygen use" to 15,
        "sleep apnea" to 5,
        "cystic fibrosis" to 15,
        "hiv or aids" to 12,
        "long-term steroid use" to 12,
        "autoimmune disease" to 8,
        "uses blood thinners" to 15,
        "bleeding disorder" to 15,
        "sickle cell disease" to 14,
        "severe anemia history" to 10,
        "high-risk medication use" to 8,
        "recent medication change" to 6,
        "medication allergy" to 4,
        "recent surgery" to 12,
        "diabetes" to 10,
        "hypertension" to 5,
        "asthma or copd" to 10,
        "liver disease" to 10,
        "cirrhosis" to 12,
        "insulin-dependent diabetes" to 12,
        "adrenal insufficiency" to 12,
        "thyroid disease" to 4,
        "severe obesity" to 6,
        "malnutrition risk" to 8,
        "postpartum under 6 weeks" to 12,
        "nursing home resident" to 8,
        "lives alone with limited support" to 5,
        "pediatric chronic illness" to 10,
        "developmental disability" to 6,
        "mobility limitation" to 5,
        "older adult frailty" to 12,
        "recent hospitalization" to 8,
        "indwelling catheter" to 8,
        "central line or port" to 10,
        "feeding tube" to 6,
        "ventricular shunt" to 12,
        "recent chemotherapy" to 14,
        "recent trauma admission" to 8,
        "homelessness or housing insecurity" to 5,
        "known infectious exposure" to 6,
        "frequent ed visits" to 5,
        "no regular medication access" to 5
    )

    private val criticalSymptoms = listOf(
        "unconscious or unresponsive",
        "airway obstruction",
        "severe breathing difficulty",
        "cyanosis",
        "signs of shock",
        "severe active bleeding",
        "cardiac arrest concern",
        "crushing chest pressure",
        "cold clammy skin",
        "anaphylaxis concern",
        "postpartum bleeding",
        "smoke inhalation",
        "heat stroke concern",
        "drowning or submersion",
        "carbon monoxide exposure",
        "crush injury",
        "blast injury"
    )

    private val urgentSymptoms = listOf(
        "reduced consciousness",
        "shortness of breath",
        "confusion",
        "seizure",
        "weakness or numbness",
        "facial droop or speech difficulty",
        "severe sudden headache",
        "blood in vomit or stool",
        "severe abdominal guarding",
        "poisoning or overdose",
        "pregnancy with bleeding",
        "child with lethargy",
        "suspected sepsis",
        "radiating chest pain",
        "syncope after exertion",
        "severe hypertension symptoms",
        "leg swelling with breathlessness",
        "sudden vision loss",
        "chemical eye exposure",
        "suicidal thoughts",
        "self-harm injury",
        "violent or unsafe behavior",
        "acute psychosis",
        "substance withdrawal",
        "testicular pain",
        "sexual assault concern",
        "reduced fetal movement",
        "hypothermia concern",
        "needs decontamination",
        "new confusion in older adult",
        "chest pain",
        "severe localized pain",
        "bleeding",
        "worsening symptoms"
    )

    fun createOfflinePatient(request: CreatePatientRequest, id: Int, queuePosition: Int): Patient {
        val symptomScore = symptomScore(request)
        val historyScore = historyScore(request)
        val weightedRisk = (0.45 * symptomScore) + (0.25 * request.image_score * 100) + (0.30 * historyScore)
        val clinicalRisk = roundScore(maxOf(weightedRisk, safetyRiskFloor(request)))
        val triageLevel = triageLevel(request, clinicalRisk)
        val rationale = buildRationale(request, triageLevel, clinicalRisk)

        return Patient(
            id = id,
            full_name = request.full_name,
            age = request.age,
            gender = request.gender,
            symptoms_description = request.symptoms_description,
            pain_level = request.pain_level,
            fever = request.fever,
            heart_rate = request.heart_rate,
            blood_pressure_systolic = request.blood_pressure_systolic,
            blood_pressure_diastolic = request.blood_pressure_diastolic,
            has_chronic_disease = request.has_chronic_disease,
            chronic_disease_description = request.chronic_disease_description,
            image_score = request.image_score,
            symptom_score = symptomScore,
            history_score = historyScore,
            clinical_risk_score = clinicalRisk,
            waiting_time_factor = 0.0,
            final_priority_score = clinicalRisk,
            triage_level = triageLevel,
            queue_position = queuePosition,
            decision_rationale = rationale,
            status = "waiting",
            queue_policy_summary = "Offline edge estimate pending backend synchronization."
        )
    }

    private fun symptomScore(request: CreatePatientRequest): Double {
        var base = request.pain_level * 6.0
        base += keywordScore(request.symptoms_description, highRiskSymptoms)
        if (request.fever) base += 15

        val heartRate = request.heart_rate
        val systolic = request.blood_pressure_systolic
        val diastolic = request.blood_pressure_diastolic

        if (heartRate < 50) base += 25
        else if (heartRate < 60) base += 8
        if (heartRate > 100) base += 10
        if (heartRate > 120) base += 10

        if (systolic >= 180) base += 20
        else if (systolic >= 160) base += 10
        else if (systolic > 140) base += 5
        if (systolic < 90) base += 25
        else if (systolic < 100) base += 10

        if (diastolic >= 110) base += 20
        else if (diastolic >= 100) base += 10
        else if (diastolic > 80) base += 5
        if (diastolic < 50) base += 20
        else if (diastolic < 60) base += 8

        return roundScore(base.coerceAtMost(100.0))
    }

    private fun historyScore(request: CreatePatientRequest): Double {
        var base = 0.0
        if (request.age > 60) base += 20
        if (request.age > 75) base += 15
        if (request.has_chronic_disease) {
            base += 25
            base += keywordScore(request.chronic_disease_description.orEmpty(), highRiskHistory)
        }
        return roundScore(base.coerceAtMost(100.0))
    }

    private fun safetyRiskFloor(request: CreatePatientRequest): Double {
        var floor = 0.0
        if (request.heart_rate > 130 || request.heart_rate < 50) floor = maxOf(floor, CRITICAL_RISK_FLOOR)
        if (request.blood_pressure_systolic < 90 || request.blood_pressure_systolic >= 180) {
            floor = maxOf(floor, CRITICAL_RISK_FLOOR)
        }
        if (request.blood_pressure_diastolic >= 110 || request.blood_pressure_diastolic < 50) {
            floor = maxOf(floor, CRITICAL_RISK_FLOOR)
        }
        if (containsAny(request.symptoms_description, criticalSymptoms)) floor = maxOf(floor, CRITICAL_RISK_FLOOR)
        if (request.pain_level >= 9 && request.fever) floor = maxOf(floor, URGENT_RISK_FLOOR)
        if (containsAny(request.symptoms_description, urgentSymptoms)) floor = maxOf(floor, URGENT_RISK_FLOOR)
        if (request.symptoms_description.contains("chest pain", ignoreCase = true) && (request.heart_rate > 100 || request.pain_level >= 7)) {
            floor = maxOf(floor, URGENT_RISK_FLOOR)
        }
        return floor
    }

    private fun triageLevel(request: CreatePatientRequest, clinicalRisk: Double): String {
        var level = "Stable"
        fun escalate(target: String) {
            if ((triageRank[target] ?: 2) < (triageRank[level] ?: 2)) level = target
        }

        if (request.heart_rate > 130) escalate("Critical")
        if (request.heart_rate < 50) escalate("Critical")
        if (request.blood_pressure_systolic < 90) escalate("Critical")
        if (request.blood_pressure_systolic >= 180) escalate("Critical")
        if (request.blood_pressure_diastolic >= 110) escalate("Critical")
        if (request.blood_pressure_diastolic < 50) escalate("Critical")
        if (containsAny(request.symptoms_description, criticalSymptoms)) escalate("Critical")

        if (request.pain_level >= 9 && request.fever) escalate("Urgent")
        if (containsAny(request.symptoms_description, urgentSymptoms)) escalate("Urgent")
        if (request.symptoms_description.contains("chest pain", ignoreCase = true) && (request.heart_rate > 100 || request.pain_level >= 7)) {
            escalate("Urgent")
        }

        when {
            clinicalRisk >= 75.0 -> escalate("Critical")
            clinicalRisk >= 72.0 -> escalate("Critical")
            clinicalRisk >= 45.0 -> escalate("Urgent")
            clinicalRisk >= 42.0 -> escalate("Urgent")
        }
        return level
    }

    private fun buildRationale(request: CreatePatientRequest, triageLevel: String, clinicalRisk: Double): String {
        val reasons = mutableListOf<String>()
        if (request.heart_rate > 130) reasons += "heart rate exceeds 130 bpm"
        if (request.heart_rate < 50) reasons += "heart rate is below 50 bpm"
        if (request.blood_pressure_systolic < 90) reasons += "systolic blood pressure is below 90 mmHg"
        if (request.blood_pressure_systolic >= 180) reasons += "systolic blood pressure is 180 mmHg or higher"
        if (request.blood_pressure_diastolic >= 110) reasons += "diastolic blood pressure is 110 mmHg or higher"
        if (request.blood_pressure_diastolic < 50) reasons += "diastolic blood pressure is below 50 mmHg"
        if (request.pain_level >= 9 && request.fever) reasons += "pain level is ${request.pain_level} and fever is present"
        if (containsAny(request.symptoms_description, criticalSymptoms)) reasons += "primary survey red-flag symptom was selected"
        if (containsAny(request.symptoms_description, urgentSymptoms)) reasons += "high-risk symptom selection was detected"
        if (request.has_chronic_disease) reasons += "medical history risk factors were selected"
        reasons += "clinical risk score is ${scoreText(clinicalRisk)}"
        return "Offline edge estimate assigned $triageLevel: ${reasons.joinToString(". ")}."
    }

    private fun keywordScore(text: String, keywords: Map<String, Int>): Double {
        val normalized = text.lowercase()
        return keywords.entries.sumOf { (keyword, points) -> if (keyword in normalized) points else 0 }.toDouble()
    }

    private fun containsAny(text: String, keywords: List<String>): Boolean {
        val normalized = text.lowercase()
        return keywords.any { it in normalized }
    }

    private fun scoreText(value: Double): String = (round(value * 10.0) / 10.0).toString()

    private fun roundScore(value: Double): Double = round(value * 100.0) / 100.0
}
