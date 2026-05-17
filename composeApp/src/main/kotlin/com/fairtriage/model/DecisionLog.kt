package com.fairtriage.model

import kotlinx.serialization.Serializable

@Serializable
data class DecisionLog(
    val id: Int,
    val patient_id: Int,
    val action_type: String,
    val old_triage_level: String? = null,
    val new_triage_level: String? = null,
    val old_priority_score: Double? = null,
    val new_priority_score: Double? = null,
    val explanation: String,
    val created_at: String
)
