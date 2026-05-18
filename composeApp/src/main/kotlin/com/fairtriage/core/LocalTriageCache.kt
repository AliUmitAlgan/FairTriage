package com.fairtriage.core

import android.content.SharedPreferences
import com.fairtriage.model.CreatePatientRequest
import com.fairtriage.model.DecisionLog
import com.fairtriage.model.OverrideRequest
import com.fairtriage.model.Patient
import java.time.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import androidx.core.content.edit

@Serializable
data class PendingOverrideAction(
    val patientId: Int,
    val request: OverrideRequest
)

object LocalTriageCache {
    private const val PREFS = "fairtriage_local_first_cache"
    private const val KEY_PATIENTS = "patients"
    private const val KEY_QUEUE = "queue"
    private const val KEY_LOGS = "logs"
    private const val KEY_PENDING_CREATES = "pending_creates"
    private const val KEY_PENDING_OVERRIDES = "pending_overrides"
    private const val KEY_PENDING_COMPLETIONS = "pending_completions"
    private const val KEY_LAST_SYNC = "last_sync"
    private const val KEY_PRIVACY_CONSENT = "privacy_consent"

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private val prefs: SharedPreferences
        get() = AppContext.applicationContext.getSharedPreferences(PREFS, 0)

    fun cachePatients(patients: List<Patient>) {
        put(KEY_PATIENTS, json.encodeToString(ListSerializer(Patient.serializer()), patients))
        markSynced()
    }

    fun cacheQueue(queue: List<Patient>) {
        put(KEY_QUEUE, json.encodeToString(ListSerializer(Patient.serializer()), queue))
        markSynced()
    }

    fun cacheLogs(logs: List<DecisionLog>) {
        put(KEY_LOGS, json.encodeToString(ListSerializer(DecisionLog.serializer()), logs))
        markSynced()
    }

    fun cachedPatients(): List<Patient> = getPatients(KEY_PATIENTS)

    fun cachedQueue(): List<Patient> = getPatients(KEY_QUEUE)

    fun cachedLogs(): List<DecisionLog> {
        val raw = prefs.getString(KEY_LOGS, null) ?: return emptyList()
        return runCatching {
            json.decodeFromString(ListSerializer(DecisionLog.serializer()), raw)
        }.getOrDefault(emptyList())
    }

    fun addPendingCreate(request: CreatePatientRequest) {
        val pending = pendingCreates() + request
        put(KEY_PENDING_CREATES, json.encodeToString(ListSerializer(CreatePatientRequest.serializer()), pending))

        val localPatient = EdgeTriageEngine.createOfflinePatient(
            request = request,
            id = nextOfflinePatientId(),
            queuePosition = cachedQueue().size + 1
        )
        cachePatients((cachedPatients() + localPatient).distinctBy { it.id })
        cacheQueue((cachedQueue() + localPatient).distinctBy { it.id })
        addLocalLog(
            patientId = localPatient.id,
            actionType = "created",
            explanation = "Offline patient record created and queued for backend synchronization."
        )
    }

    fun pendingCreates(): List<CreatePatientRequest> {
        val raw = prefs.getString(KEY_PENDING_CREATES, null) ?: return emptyList()
        return runCatching {
            json.decodeFromString(ListSerializer(CreatePatientRequest.serializer()), raw)
        }.getOrDefault(emptyList())
    }

    fun replacePendingCreates(pending: List<CreatePatientRequest>) {
        put(KEY_PENDING_CREATES, json.encodeToString(ListSerializer(CreatePatientRequest.serializer()), pending))
    }

    fun pendingCreateCount(): Int = pendingCreates().size

    fun addPendingOverride(patientId: Int, request: OverrideRequest) {
        val pending = pendingOverrides() + PendingOverrideAction(patientId, request)
        put(KEY_PENDING_OVERRIDES, json.encodeToString(ListSerializer(PendingOverrideAction.serializer()), pending))
        updateCachedPatient(patientId) { patient ->
            patient.copy(
                triage_level = request.new_triage_level ?: patient.triage_level,
                overridden_by_doctor = true,
                doctor_override_reason = request.override_reason,
                decision_rationale = "Offline doctor override pending backend synchronization. Backend will derive the final triage level from selected reasons: ${request.override_reason}"
            )
        }
        addLocalLog(
            patientId = patientId,
            actionType = "doctor_override",
            explanation = "Offline override queued: ${request.override_reason}"
        )
    }

    fun pendingOverrides(): List<PendingOverrideAction> {
        val raw = prefs.getString(KEY_PENDING_OVERRIDES, null) ?: return emptyList()
        return runCatching {
            json.decodeFromString(ListSerializer(PendingOverrideAction.serializer()), raw)
        }.getOrDefault(emptyList())
    }

    fun replacePendingOverrides(pending: List<PendingOverrideAction>) {
        put(KEY_PENDING_OVERRIDES, json.encodeToString(ListSerializer(PendingOverrideAction.serializer()), pending))
    }

    fun addPendingCompletion(patientId: Int) {
        val pending = (pendingCompletions() + patientId).distinct()
        put(KEY_PENDING_COMPLETIONS, json.encodeToString(ListSerializer(Int.serializer()), pending))
        updateCachedPatient(patientId) { patient ->
            patient.copy(status = "completed", queue_position = 0)
        }
        cacheQueue(cachedQueue().filterNot { it.id == patientId })
        addLocalLog(
            patientId = patientId,
            actionType = "completed",
            explanation = "Offline completion queued for backend synchronization."
        )
    }

    fun pendingCompletions(): List<Int> {
        val raw = prefs.getString(KEY_PENDING_COMPLETIONS, null) ?: return emptyList()
        return runCatching {
            json.decodeFromString(ListSerializer(Int.serializer()), raw)
        }.getOrDefault(emptyList())
    }

    fun replacePendingCompletions(pending: List<Int>) {
        put(KEY_PENDING_COMPLETIONS, json.encodeToString(ListSerializer(Int.serializer()), pending))
    }

    fun pendingActionCount(): Int = pendingCreateCount() + pendingOverrides().size + pendingCompletions().size

    fun lastSyncMillis(): Long = prefs.getLong(KEY_LAST_SYNC, 0L)

    fun hasPrivacyConsent(): Boolean = prefs.getBoolean(KEY_PRIVACY_CONSENT, false)

    fun setPrivacyConsent(accepted: Boolean) {
        prefs.edit { putBoolean(KEY_PRIVACY_CONSENT, accepted) }
    }

    private fun getPatients(key: String): List<Patient> {
        val raw = prefs.getString(key, null) ?: return emptyList()
        return runCatching {
            json.decodeFromString(ListSerializer(Patient.serializer()), raw)
        }.getOrDefault(emptyList())
    }

    private fun nextOfflinePatientId(): Int {
        val minExistingId = (cachedPatients() + cachedQueue()).minOfOrNull { it.id } ?: 0
        return if (minExistingId < 0) minExistingId - 1 else -1
    }

    private fun updateCachedPatient(patientId: Int, transform: (Patient) -> Patient) {
        cachePatients(cachedPatients().map { if (it.id == patientId) transform(it) else it })
        cacheQueue(cachedQueue().map { if (it.id == patientId) transform(it) else it })
    }

    private fun addLocalLog(patientId: Int, actionType: String, explanation: String) {
        val nextId = (cachedLogs().minOfOrNull { it.id } ?: 0).let { if (it < 0) it - 1 else -1 }
        val log = DecisionLog(
            id = nextId,
            patient_id = patientId,
            action_type = actionType,
            explanation = explanation,
            created_at = Instant.now().toString()
        )
        cacheLogs(listOf(log) + cachedLogs())
    }

    private fun markSynced() {
        prefs.edit { putLong(KEY_LAST_SYNC, System.currentTimeMillis()) }
    }

    private fun put(key: String, value: String) {
        prefs.edit { putString(key, value) }
    }
}
