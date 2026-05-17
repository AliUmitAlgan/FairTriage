package com.fairtriage.model

import kotlinx.serialization.Serializable

@Serializable
data class CreatePatientRequest(
    val full_name: String,
    val age: Int,
    val gender: String,
    val symptoms_description: String,
    val pain_level: Int,
    val fever: Boolean,
    val heart_rate: Int,
    val blood_pressure_systolic: Int,
    val blood_pressure_diastolic: Int,
    val has_chronic_disease: Boolean,
    val chronic_disease_description: String? = null,
    val image_score: Double
)
