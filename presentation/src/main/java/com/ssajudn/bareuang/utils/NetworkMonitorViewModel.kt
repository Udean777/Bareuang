package com.ssajudn.bareuang.utils

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class NetworkMonitorViewModel @Inject constructor(
    private val monitor: NetworkMonitor
) : ViewModel() {
    fun isOnline(): Boolean = monitor.isOnline()
    fun observeIsOnline(): Flow<Boolean> = monitor.observeIsOnline()
}
