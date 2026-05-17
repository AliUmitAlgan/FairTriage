package com.fairtriage.repository

import com.fairtriage.core.ApiClient
import com.fairtriage.core.BASE_URL
import io.ktor.client.request.delete
import io.ktor.client.request.post

interface SimulationRepository {
    suspend fun seedDemoData()
    suspend fun resetAll()
}

class KtorSimulationRepository : SimulationRepository {
    private val client = ApiClient.httpClient

    override suspend fun seedDemoData(): Unit = apiCall("Seed demo data") {
        client.post("$BASE_URL/simulation/seed-demo-data")
        Unit
    }

    override suspend fun resetAll(): Unit = apiCall("Reset simulation") {
        client.delete("$BASE_URL/simulation/reset")
        Unit
    }
}
