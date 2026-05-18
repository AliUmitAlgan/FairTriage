package com.fairtriage.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.fairtriage.core.ScreenState
import com.fairtriage.model.CreatePatientRequest
import com.fairtriage.model.OverrideRequest
import com.fairtriage.model.Patient
import com.fairtriage.repository.KtorPatientRepository
import com.fairtriage.repository.PatientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SubmitState {
    data object Idle : SubmitState
    data object Loading : SubmitState
    data class Success(val patientId: Int? = null) : SubmitState
    data class Error(val message: String) : SubmitState
}

class AddPatientScreenModel(
    private val patientRepository: PatientRepository = KtorPatientRepository()
) : ScreenModel {
    private val _submitState = MutableStateFlow<SubmitState>(SubmitState.Idle)
    val submitState: StateFlow<SubmitState> = _submitState.asStateFlow()

    private val _patientState = MutableStateFlow<ScreenState<Patient>?>(null)
    val patientState: StateFlow<ScreenState<Patient>?> = _patientState.asStateFlow()

    fun loadPatient(patientId: Int) {
        screenModelScope.launch {
            _patientState.value = ScreenState.Loading
            try {
                _patientState.value = ScreenState.Success(patientRepository.getPatient(patientId))
            } catch (e: Throwable) {
                _patientState.value = ScreenState.Error(e.message ?: "Unable to load patient.")
            }
        }
    }

    fun submit(request: CreatePatientRequest) {
        _submitState.value = SubmitState.Loading
        screenModelScope.launch {
            try {
                val patient = patientRepository.createPatient(request)
                _submitState.value = SubmitState.Success(patient.id)
            } catch (e: Throwable) {
                _submitState.value = SubmitState.Error(e.message ?: "Unable to submit patient.")
            }
        }
    }

    fun submitOverride(patientId: Int, request: CreatePatientRequest, overrideReasons: List<String>) {
        _submitState.value = SubmitState.Loading
        screenModelScope.launch {
            try {
                patientRepository.updatePatient(patientId, request)
                val updated = patientRepository.overridePatient(
                    patientId,
                    OverrideRequest(
                        override_reasons = overrideReasons,
                        override_reason = overrideReasons.joinToString("; ")
                    )
                )
                _submitState.value = SubmitState.Success(updated.id)
            } catch (e: Throwable) {
                _submitState.value = SubmitState.Error(e.message ?: "Unable to save override review.")
            }
        }
    }

    fun clearError() {
        if (_submitState.value is SubmitState.Error) {
            _submitState.value = SubmitState.Idle
        }
    }
}
