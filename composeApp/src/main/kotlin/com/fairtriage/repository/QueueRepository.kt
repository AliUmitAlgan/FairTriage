package com.fairtriage.repository

import com.fairtriage.core.ApiClient
import com.fairtriage.core.BASE_URL
import com.fairtriage.core.LocalTriageCache
import com.fairtriage.model.CreatePatientRequest
import com.fairtriage.model.Patient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

interface QueueRepository {
    suspend fun getQueue(): List<Patient>
}

class KtorQueueRepository : QueueRepository {
    private val client = ApiClient.httpClient

    override suspend fun getQueue(): List<Patient> = apiCall("Load queue") {
        syncPendingActions()
        runCatching {
            client.get("$BASE_URL/queue").body<List<Patient>>()
        }.onSuccess { queue ->
            LocalTriageCache.cacheQueue(queue)
        }.getOrElse { error ->
            val cached = LocalTriageCache.cachedQueue()
            if (cached.isNotEmpty()) cached else throw error
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
