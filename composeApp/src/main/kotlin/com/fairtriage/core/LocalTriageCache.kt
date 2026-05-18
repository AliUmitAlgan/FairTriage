package com.fairtriage.core

import android.content.SharedPreferences
import com.fairtriage.model.CreatePatientRequest
import com.fairtriage.model.DecisionLog
import com.fairtriage.model.Patient
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

object LocalTriageCache {
    private const val PREFS = "fairtriage_local_first_cache"
    private const val KEY_PATIENTS = "patients"
    private const val KEY_QUEUE = "queue"
    private const val KEY_LOGS = "logs"
    private const val KEY_PENDING_CREATES = "pending_creates"
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

    fun lastSyncMillis(): Long = prefs.getLong(KEY_LAST_SYNC, 0L)

    fun hasPrivacyConsent(): Boolean = prefs.getBoolean(KEY_PRIVACY_CONSENT, false)

    fun setPrivacyConsent(accepted: Boolean) {
        prefs.edit().putBoolean(KEY_PRIVACY_CONSENT, accepted).apply()
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

    private fun markSynced() {
        prefs.edit().putLong(KEY_LAST_SYNC, System.currentTimeMillis()).apply()
    }

    private fun put(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }
}
