package com.fairtriage.model

import kotlinx.serialization.Serializable

@Serializable
data class ProductPolicy(
    val product_name: String,
    val prototype_disclaimer: String,
    val clinical_control_policy: String,
    val scoring_formula: String,
    val safety_rules: List<String>,
    val fairness_policy: String,
    val max_waiting_minutes: Map<String, Int>,
    val audit_log_actions: List<String>,
    val privacy_policy: String,
    val offline_policy: String
)
