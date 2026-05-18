package com.fairtriage.core

import com.fairtriage.model.CreatePatientRequest
import com.fairtriage.model.Patient
import kotlin.math.round

object EdgeTriageEngine {
    private val highRiskSymptoms = mapOf(
        "chest pain" to 20,
        "shortness of breath" to 20,
        "confusion" to 18,
        "seizure" to 18,
        "weakness or numbness" to 15,
        "bleeding" to 12,
        "trauma" to 12,
        "severe localized pain" to 10,
        "worsening symptoms" to 10
    )

    private val highRiskHistory = mapOf(
        "heart disease" to 20,
        "kidney disease" to 18,
        "cancer treatment" to 18,
        "immunosuppressed" to 18,
        "pregnancy risk" to 15,
        "uses blood thinners" to 15,
        "recent surgery" to 12,
        "diabetes" to 10,
        "asthma or copd" to 10
    )

    fun createOfflinePatient(request: CreatePatientRequest, id: Int, queuePosition: Int): Patient {
        val symptomScore = symptomScore(request)
        val historyScore = historyScore(request)
        val clinicalRisk = roundScore((0.45 * symptomScore) + (0.25 * request.image_score * 100) + (0.30 * historyScore))
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
        if (request.heart_rate > 100) base += 10
        if (request.heart_rate > 120) base += 10
        if (request.blood_pressure_systolic > 140) base += 5
        if (request.blood_pressure_systolic < 100) base += 10
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

    private fun triageLevel(request: CreatePatientRequest, clinicalRisk: Double): String {
        var level = "Stable"
        fun escalate(target: String) {
            val rank = mapOf("Critical" to 0, "Urgent" to 1, "Stable" to 2)
            if ((rank[target] ?: 2) < (rank[level] ?: 2)) level = target
        }

        if (request.heart_rate > 130) escalate("Critical")
        if (request.blood_pressure_systolic < 90) escalate("Critical")
        if (request.pain_level >= 9 && request.fever) escalate("Urgent")
        if (containsAny(request.symptoms_description, listOf("shortness of breath", "confusion", "seizure", "weakness or numbness"))) escalate("Urgent")
        if (request.symptoms_description.contains("chest pain", ignoreCase = true) && (request.heart_rate > 100 || request.pain_level >= 7)) escalate("Urgent")
        if (clinicalRisk >= 75 || clinicalRisk >= 72) escalate("Critical")
        else if (clinicalRisk >= 45 || clinicalRisk >= 42) escalate("Urgent")
        return level
    }

    private fun buildRationale(request: CreatePatientRequest, triageLevel: String, clinicalRisk: Double): String {
        val reasons = mutableListOf<String>()
        if (request.heart_rate > 130) reasons += "heart rate exceeds 130 bpm"
        if (request.blood_pressure_systolic < 90) reasons += "systolic blood pressure is below 90 mmHg"
        if (request.pain_level >= 9 && request.fever) reasons += "pain level is ${request.pain_level} and fever is present"
        if (containsAny(request.symptoms_description, listOf("shortness of breath", "confusion", "seizure", "weakness or numbness"))) reasons += "high-risk symptom selection was detected"
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
