package com.fairtriage.repository

import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText

class RepositoryException(message: String) : Exception(message)

suspend fun <T> apiCall(action: String, block: suspend () -> T): T {
    return try {
        block()
    } catch (e: ResponseException) {
        val details = runCatching { e.response.bodyAsText() }.getOrNull()
        val suffix = details?.takeIf { it.isNotBlank() }?.let { ": $it" }.orEmpty()
        throw RepositoryException("$action failed (${e.response.status.value})$suffix")
    } catch (e: Throwable) {
        val message = e.message?.takeIf { it.isNotBlank() } ?: "Unexpected network error"
        throw RepositoryException("$action failed: $message")
    }
}
