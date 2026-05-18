package com.fairtriage.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.fairtriage.core.ScreenState
import com.fairtriage.model.Patient
import com.fairtriage.repository.KtorPatientRepository
import com.fairtriage.repository.KtorSimulationRepository
import com.fairtriage.repository.PatientRepository
import com.fairtriage.repository.SimulationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardScreenModel(
    private val patientRepository: PatientRepository = KtorPatientRepository(),
    private val simulationRepository: SimulationRepository = KtorSimulationRepository()
) : ScreenModel {
    private val _state = MutableStateFlow<ScreenState<List<Patient>>>(ScreenState.Loading)
    val state: StateFlow<ScreenState<List<Patient>>> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        screenModelScope.launch {
            loadPatients()
        }
    }

    fun seedDemoData() {
        screenModelScope.launch {
            _state.value = ScreenState.Loading
            try {
                simulationRepository.seedDemoData()
                loadPatients()
            } catch (e: Throwable) {
                _state.value = ScreenState.Error(e.message ?: "Unable to seed demo data.")
            }
        }
    }

    fun resetAll() {
        screenModelScope.launch {
            _state.value = ScreenState.Loading
            try {
                simulationRepository.resetAll()
                loadPatients()
            } catch (e: Throwable) {
                _state.value = ScreenState.Error(e.message ?: "Unable to reset simulation.")
            }
        }
    }

    private suspend fun loadPatients() {
        _state.value = ScreenState.Loading
        try {
            _state.value = ScreenState.Success(patientRepository.getPatientsFromNetwork())
        } catch (e: Throwable) {
            _state.value = ScreenState.Error(e.message ?: "Unable to load dashboard.")
        }
    }
}
