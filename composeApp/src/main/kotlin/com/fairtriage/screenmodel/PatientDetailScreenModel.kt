package com.fairtriage.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.fairtriage.core.ScreenState
import com.fairtriage.model.Patient
import com.fairtriage.repository.KtorPatientRepository
import com.fairtriage.repository.PatientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PatientActionState {
    data object Idle : PatientActionState
    data object Loading : PatientActionState
    data object Completed : PatientActionState
    data class Error(val message: String) : PatientActionState
}

class PatientDetailScreenModel(
    private val patientRepository: PatientRepository = KtorPatientRepository()
) : ScreenModel {
    private val _state = MutableStateFlow<ScreenState<Patient>>(ScreenState.Loading)
    val state: StateFlow<ScreenState<Patient>> = _state.asStateFlow()

    private val _actionState = MutableStateFlow<PatientActionState>(PatientActionState.Idle)
    val actionState: StateFlow<PatientActionState> = _actionState.asStateFlow()

    fun load(patientId: Int) {
        screenModelScope.launch {
            _state.value = ScreenState.Loading
            try {
                _state.value = ScreenState.Success(patientRepository.getPatient(patientId))
            } catch (e: Throwable) {
                _state.value = ScreenState.Error(e.message ?: "Unable to load patient.")
            }
        }
    }

    fun complete(patientId: Int) {
        _actionState.value = PatientActionState.Loading
        screenModelScope.launch {
            try {
                patientRepository.completePatient(patientId)
                _actionState.value = PatientActionState.Completed
            } catch (e: Throwable) {
                _actionState.value = PatientActionState.Error(e.message ?: "Unable to mark patient completed.")
            }
        }
    }

    fun clearActionError() {
        if (_actionState.value is PatientActionState.Error) {
            _actionState.value = PatientActionState.Idle
        }
    }
}
