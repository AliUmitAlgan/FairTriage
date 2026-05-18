package com.fairtriage.model

import kotlinx.serialization.Serializable

@Serializable
data class Patient(
    val id: Int,
    val full_name: String,
    val age: Int,
    val gender: String,
    val arrival_time: String? = null,
    val symptoms_description: String,
    val pain_level: Int,
    val fever: Boolean,
    val heart_rate: Int,
    val blood_pressure_systolic: Int,
    val blood_pressure_diastolic: Int,
    val has_chronic_disease: Boolean,
    val chronic_disease_description: String? = null,
    val image_score: Double,
    val symptom_score: Double? = null,
    val history_score: Double? = null,
    val clinical_risk_score: Double? = null,
    val waiting_time_factor: Double? = null,
    val final_priority_score: Double? = null,
    val triage_level: String? = null,
    val queue_position: Int? = null,
    val decision_rationale: String? = null,
    val status: String,
    val overridden_by_doctor: Boolean = false,
    val doctor_override_reason: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    val waiting_minutes: Double? = null,
    val max_waiting_minutes: Int? = null,
    val max_waiting_exceeded: Boolean = false,
    val queue_policy_summary: String? = null
)
