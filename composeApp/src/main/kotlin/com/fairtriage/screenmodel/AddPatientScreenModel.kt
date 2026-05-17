package com.fairtriage.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.fairtriage.model.CreatePatientRequest
import com.fairtriage.repository.KtorPatientRepository
import com.fairtriage.repository.PatientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SubmitState {
    data object Idle : SubmitState
    data object Loading : SubmitState
    data object Success : SubmitState
    data class Error(val message: String) : SubmitState
}

class AddPatientScreenModel(
    private val patientRepository: PatientRepository = KtorPatientRepository()
) : ScreenModel {
    private val _submitState = MutableStateFlow<SubmitState>(SubmitState.Idle)
    val submitState: StateFlow<SubmitState> = _submitState.asStateFlow()

    fun submit(request: CreatePatientRequest) {
        _submitState.value = SubmitState.Loading
        screenModelScope.launch {
            try {
                patientRepository.createPatient(request)
                _submitState.value = SubmitState.Success
            } catch (e: Throwable) {
                _submitState.value = SubmitState.Error(e.message ?: "Unable to submit patient.")
            }
        }
    }

    fun clearError() {
        if (_submitState.value is SubmitState.Error) {
            _submitState.value = SubmitState.Idle
        }
    }
}
