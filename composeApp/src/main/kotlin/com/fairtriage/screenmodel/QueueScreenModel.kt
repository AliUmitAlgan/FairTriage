package com.fairtriage.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.fairtriage.core.ScreenState
import com.fairtriage.model.Patient
import com.fairtriage.repository.KtorQueueRepository
import com.fairtriage.repository.QueueRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QueueScreenModel(
    private val queueRepository: QueueRepository = KtorQueueRepository()
) : ScreenModel {
    private val _state = MutableStateFlow<ScreenState<List<Patient>>>(ScreenState.Loading)
    val state: StateFlow<ScreenState<List<Patient>>> = _state.asStateFlow()

    fun refresh() {
        screenModelScope.launch {
            _state.value = ScreenState.Loading
            try {
                _state.value = ScreenState.Success(queueRepository.getQueue())
            } catch (e: Throwable) {
                _state.value = ScreenState.Error(e.message ?: "Unable to load queue.")
            }
        }
    }
}
