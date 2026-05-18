package com.fairtriage.repository

import com.fairtriage.core.ApiClient
import com.fairtriage.core.BASE_URL
import com.fairtriage.core.LocalTriageCache
import com.fairtriage.model.CreatePatientRequest
import com.fairtriage.model.OverrideRequest
import com.fairtriage.model.Patient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody

interface PatientRepository {
    suspend fun getPatients(): List<Patient>
    suspend fun getPatientsFromNetwork(): List<Patient>
    suspend fun getPatient(patientId: Int): Patient
    suspend fun createPatient(request: CreatePatientRequest): Patient
    suspend fun updatePatient(patientId: Int, request: CreatePatientRequest): Patient
    suspend fun completePatient(patientId: Int)
    suspend fun overridePatient(patientId: Int, request: OverrideRequest): Patient
}

class KtorPatientRepository : PatientRepository {
    private val client = ApiClient.httpClient

    override suspend fun getPatients(): List<Patient> = loadPatients(fallbackToCache = true)

    override suspend fun getPatientsFromNetwork(): List<Patient> = loadPatients(fallbackToCache = false)

    private suspend fun loadPatients(fallbackToCache: Boolean): List<Patient> = apiCall("Load patients") {
        syncPendingActions()
        runCatching {
            client.get("$BASE_URL/patients").body<List<Patient>>()
        }.onSuccess { patients ->
            LocalTriageCache.cachePatients(patients)
        }.getOrElse { error ->
            if (!fallbackToCache) throw error
            val cached = LocalTriageCache.cachedPatients()
            cached.ifEmpty { throw error }
        }
    }

    override suspend fun getPatient(patientId: Int): Patient = apiCall("Load patient") {
        runCatching {
            client.get("$BASE_URL/patients/$patientId").body<Patient>()
        }.onSuccess { patient ->
            val updated = (LocalTriageCache.cachedPatients().filterNot { it.id == patient.id } + patient)
            LocalTriageCache.cachePatients(updated)
        }.getOrElse { error ->
            LocalTriageCache.cachedPatients().firstOrNull { it.id == patientId }
                ?: LocalTriageCache.cachedQueue().firstOrNull { it.id == patientId }
                ?: throw error
        }
    }

    override suspend fun createPatient(request: CreatePatientRequest): Patient = apiCall("Create patient") {
        runCatching {
            client.post("$BASE_URL/patients") {
                setBody(request)
            }.body<Patient>()
        }.onSuccess { patient ->
            val updated = (LocalTriageCache.cachedPatients().filterNot { it.id == patient.id } + patient)
            LocalTriageCache.cachePatients(updated)
            LocalTriageCache.cacheQueue((LocalTriageCache.cachedQueue().filterNot { it.id == patient.id } + patient).filter { it.status == "waiting" })
        }.getOrElse {
            LocalTriageCache.addPendingCreate(request)
        }
    }

    override suspend fun updatePatient(patientId: Int, request: CreatePatientRequest): Patient = apiCall("Update patient") {
        client.put("$BASE_URL/patients/$patientId") {
            setBody(request)
        }.body<Patient>().also { patient ->
            val updated = (LocalTriageCache.cachedPatients().filterNot { it.id == patient.id } + patient)
            LocalTriageCache.cachePatients(updated)
            LocalTriageCache.cacheQueue((LocalTriageCache.cachedQueue().filterNot { it.id == patient.id } + patient).filter { it.status == "waiting" })
        }
    }

    override suspend fun completePatient(patientId: Int): Unit = apiCall("Complete patient") {
        runCatching {
            client.post("$BASE_URL/patients/$patientId/complete")
        }.onFailure {
            LocalTriageCache.addPendingCompletion(patientId)
        }
        Unit
    }

    override suspend fun overridePatient(patientId: Int, request: OverrideRequest): Patient = apiCall("Override decision") {
        runCatching {
            client.post("$BASE_URL/patients/$patientId/override") {
                setBody(request)
            }.body<Patient>()
        }.onSuccess { patient ->
            val updated = (LocalTriageCache.cachedPatients().filterNot { it.id == patient.id } + patient)
            LocalTriageCache.cachePatients(updated)
            LocalTriageCache.cacheQueue((LocalTriageCache.cachedQueue().filterNot { it.id == patient.id } + patient).filter { it.status == "waiting" })
        }.onFailure {
            LocalTriageCache.addPendingOverride(patientId, request)
        }.getOrElse { error ->
            LocalTriageCache.cachedPatients().firstOrNull { it.id == patientId }
                ?: LocalTriageCache.cachedQueue().firstOrNull { it.id == patientId }
                ?: throw error
        }
    }

    private suspend fun syncPendingActions() {
        syncPendingCreates()
        syncPendingOverrides()
        syncPendingCompletions()
    }

    private suspend fun syncPendingCreates() {
        val pending = LocalTriageCache.pendingCreates()
        if (pending.isEmpty()) return

        val remaining = mutableListOf<CreatePatientRequest>()
        pending.forEach { request ->
            val synced = runCatching {
                client.post("$BASE_URL/patients") {
                    setBody(request)
                }
            }.isSuccess
            if (!synced) remaining += request
        }
        LocalTriageCache.replacePendingCreates(remaining)
    }

    private suspend fun syncPendingOverrides() {
        val pending = LocalTriageCache.pendingOverrides()
        if (pending.isEmpty()) return

        val remaining = mutableListOf<com.fairtriage.core.PendingOverrideAction>()
        pending.forEach { action ->
            if (action.patientId < 0) {
                remaining += action
                return@forEach
            }
            val synced = runCatching {
                client.post("$BASE_URL/patients/${action.patientId}/override") {
                    setBody(action.request)
                }
            }.isSuccess
            if (!synced) remaining += action
        }
        LocalTriageCache.replacePendingOverrides(remaining)
    }

    private suspend fun syncPendingCompletions() {
        val pending = LocalTriageCache.pendingCompletions()
        if (pending.isEmpty()) return

        val remaining = mutableListOf<Int>()
        pending.forEach { patientId ->
            if (patientId < 0) {
                remaining += patientId
                return@forEach
            }
            val synced = runCatching {
                client.post("$BASE_URL/patients/$patientId/complete")
            }.isSuccess
            if (!synced) remaining += patientId
        }
        LocalTriageCache.replacePendingCompletions(remaining)
    }
}
