package com.nhathuy.randomlucky.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhathuy.randomlucky.domain.model.LotterySession
import com.nhathuy.randomlucky.domain.repository.LotteryRepository
import com.nhathuy.randomlucky.presentation.state.HistoryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val lotteryRepository: LotteryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val sessions = lotteryRepository.getAllLotterySessions().first()
                _uiState.update {
                    it.copy(
                        sessions = sessions,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Không thể tải lịch sử: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun searchSessions(query: String) {
        _uiState.update {
            it.copy(searchQuery = query)
        }
    }

    fun showSessionDetail(session: LotterySession) {
        _uiState.update {
            it.copy(
                selectedSession = session,
                showSessionDetail = false
            )
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            try {
                lotteryRepository.deleteLotterySession(sessionId)
                val updatedSessions = _uiState.value.sessions.filter { it.id != sessionId }
                _uiState.update { it.copy(sessions = updatedSessions) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Khong thể xóa phiên: ${e.message}")
                }
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            try {
                lotteryRepository.clearAllSessions()
                _uiState.update { it.copy(sessions = emptyList()) }

                // ✅ Log để debug
                println("DEBUG: All history cleared from HistoryViewModel")
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Không thể xóa lịch sử: ${e.message}")
                }
            }
        }
    }

    fun clearError() {
        _uiState.update {
            it.copy(error = null)
        }
    }

    fun hideSessionDetail() {
        _uiState.update {
            it.copy(
                selectedSession = null,
                showSessionDetail = false
            )
        }
    }
}