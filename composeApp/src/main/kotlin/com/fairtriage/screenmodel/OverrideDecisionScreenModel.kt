package com.fairtriage.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.fairtriage.model.OverrideRequest
import com.fairtriage.repository.KtorPatientRepository
import com.fairtriage.repository.PatientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OverrideDecisionScreenModel(
    private val patientRepository: PatientRepository = KtorPatientRepository()
) : ScreenModel {
    private val _submitState = MutableStateFlow<SubmitState>(SubmitState.Idle)
    val submitState: StateFlow<SubmitState> = _submitState.asStateFlow()

    fun submit(patientId: Int, request: OverrideRequest) {
        _submitState.value = SubmitState.Loading
        screenModelScope.launch {
            try {
                patientRepository.overridePatient(patientId, request)
                _submitState.value = SubmitState.Success(patientId)
            } catch (e: Throwable) {
                _submitState.value = SubmitState.Error(e.message ?: "Unable to apply override.")
            }
        }
    }

    fun clearError() {
        if (_submitState.value is SubmitState.Error) {
            _submitState.value = SubmitState.Idle
        }
    }
}
