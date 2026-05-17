package com.fairtriage.model

import kotlinx.serialization.Serializable

@Serializable
data class OverrideRequest(
    val new_triage_level: String,
    val override_reason: String
)
