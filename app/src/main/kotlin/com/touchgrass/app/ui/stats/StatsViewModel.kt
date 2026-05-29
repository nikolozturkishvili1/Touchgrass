package com.touchgrass.app.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.touchgrass.app.data.repository.BlockEventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Stats screen state (spec §3.1.F). Composes today / this-week / all-time counts and a top-N
 * surface breakdown into a single [StatsUiState].
 */
@HiltViewModel
class StatsViewModel
    @Inject
    constructor(
        blockEventRepository: BlockEventRepository,
    ) : ViewModel() {
        val uiState: StateFlow<StatsUiState> =
            combine(
                blockEventRepository.todayCountFlow(),
                blockEventRepository.thisWeekCountFlow(),
                blockEventRepository.allTimeCountFlow(),
                blockEventRepository.topSurfacesThisWeekFlow(),
            ) { today, week, allTime, top ->
                StatsUiState(
                    today = today,
                    thisWeek = week,
                    allTime = allTime,
                    topSurfacesThisWeek = top,
                    loading = false,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STATE_SHARING_TIMEOUT_MS),
                initialValue = StatsUiState(),
            )

        private companion object {
            const val STATE_SHARING_TIMEOUT_MS = 5_000L
        }
    }
