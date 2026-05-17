package com.fairtriage.core

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object ApiClient {
    val httpClient: HttpClient by lazy {
        HttpClient(OkHttp) {
            expectSuccess = true

            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        explicitNulls = false
                    }
                )
            }

            install(Logging) {
                level = LogLevel.INFO
            }

            defaultRequest {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }
        }
    }
}
