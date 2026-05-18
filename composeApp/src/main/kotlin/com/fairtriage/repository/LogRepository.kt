package com.fairtriage.repository

import com.fairtriage.core.ApiClient
import com.fairtriage.core.BASE_URL
import com.fairtriage.core.LocalTriageCache
import com.fairtriage.model.DecisionLog
import io.ktor.client.call.body
import io.ktor.client.request.get

interface LogRepository {
    suspend fun getLogs(): List<DecisionLog>
}

class KtorLogRepository : LogRepository {
    private val client = ApiClient.httpClient

    override suspend fun getLogs(): List<DecisionLog> = apiCall("Load decision logs") {
        runCatching {
            client.get("$BASE_URL/logs").body<List<DecisionLog>>()
        }.onSuccess { logs ->
            LocalTriageCache.cacheLogs(logs)
        }.getOrElse { error ->
            val cached = LocalTriageCache.cachedLogs()
            if (cached.isNotEmpty()) cached else throw error
        }
    }
}
