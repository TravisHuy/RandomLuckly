package com.nhathuy.randomlucky.presentation.state

import com.nhathuy.randomlucky.domain.model.LotterySession

data class HistoryUiState(
    val sessions: List<LotterySession> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedSession: LotterySession? = null,
    val showSessionDetail :Boolean = false
){
    val filteredSessions : List<LotterySession>
        get() = if(searchQuery.isEmpty()){
            sessions.sortedByDescending { it.startTime }
        }
        else{
            sessions.filter { session->
                session.id.contains(searchQuery, ignoreCase = true)
                session.results.values.any { result ->
                    result.numbers.any {
                        number ->
                        number.contains(searchQuery, ignoreCase = true)
                    }
                }
            }.sortedByDescending { it.startTime }
        }

    val hasResults : Boolean
        get() = sessions.isNotEmpty()

    val isEmpty : Boolean
        get() = sessions.isEmpty() && !isLoading
}
