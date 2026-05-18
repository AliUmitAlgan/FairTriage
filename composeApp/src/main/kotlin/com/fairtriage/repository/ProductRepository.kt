package com.fairtriage.repository

import com.fairtriage.core.ApiClient
import com.fairtriage.core.BASE_URL
import com.fairtriage.model.ProductPolicy
import io.ktor.client.call.body
import io.ktor.client.request.get

interface ProductRepository {
    suspend fun getPolicy(): ProductPolicy
}

class KtorProductRepository : ProductRepository {
    private val client = ApiClient.httpClient

    override suspend fun getPolicy(): ProductPolicy = apiCall("Load product policy") {
        client.get("$BASE_URL/product/policy").body()
    }
}
