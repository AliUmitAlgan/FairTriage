package com.fairtriage.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.fairtriage.core.ScreenState
import com.fairtriage.model.DecisionLog
import com.fairtriage.repository.KtorLogRepository
import com.fairtriage.repository.LogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LogsScreenModel(
    private val logRepository: LogRepository = KtorLogRepository()
) : ScreenModel {
    private val _state = MutableStateFlow<ScreenState<List<DecisionLog>>>(ScreenState.Loading)
    val state: StateFlow<ScreenState<List<DecisionLog>>> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        screenModelScope.launch {
            _state.value = ScreenState.Loading
            try {
                val logs = logRepository.getLogs().sortedByDescending { it.created_at }
                _state.value = ScreenState.Success(logs)
            } catch (e: Throwable) {
                _state.value = ScreenState.Error(e.message ?: "Unable to load decision logs.")
            }
        }
    }
}
