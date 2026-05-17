package com.fairtriage.repository

import com.fairtriage.core.ApiClient
import com.fairtriage.core.BASE_URL
import com.fairtriage.model.Patient
import io.ktor.client.call.body
import io.ktor.client.request.get

interface QueueRepository {
    suspend fun getQueue(): List<Patient>
}

class KtorQueueRepository : QueueRepository {
    private val client = ApiClient.httpClient

    override suspend fun getQueue(): List<Patient> = apiCall("Load queue") {
        client.get("$BASE_URL/queue").body()
    }
}
