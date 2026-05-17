package com.fairtriage.repository

import com.fairtriage.core.ApiClient
import com.fairtriage.core.BASE_URL
import com.fairtriage.model.CreatePatientRequest
import com.fairtriage.model.OverrideRequest
import com.fairtriage.model.Patient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

interface PatientRepository {
    suspend fun getPatients(): List<Patient>
    suspend fun getPatient(patientId: Int): Patient
    suspend fun createPatient(request: CreatePatientRequest)
    suspend fun completePatient(patientId: Int)
    suspend fun overridePatient(patientId: Int, request: OverrideRequest)
}

class KtorPatientRepository : PatientRepository {
    private val client = ApiClient.httpClient

    override suspend fun getPatients(): List<Patient> = apiCall("Load patients") {
        client.get("$BASE_URL/patients").body()
    }

    override suspend fun getPatient(patientId: Int): Patient = apiCall("Load patient") {
        client.get("$BASE_URL/patients/$patientId").body()
    }

    override suspend fun createPatient(request: CreatePatientRequest): Unit = apiCall("Create patient") {
        client.post("$BASE_URL/patients") {
            setBody(request)
        }
        Unit
    }

    override suspend fun completePatient(patientId: Int): Unit = apiCall("Complete patient") {
        client.post("$BASE_URL/patients/$patientId/complete")
        Unit
    }

    override suspend fun overridePatient(patientId: Int, request: OverrideRequest): Unit = apiCall("Override decision") {
        client.post("$BASE_URL/patients/$patientId/override") {
            setBody(request)
        }
        Unit
    }
}
