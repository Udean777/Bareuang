package com.ssajudn.barebudget.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.barebudget.data.local.BackupRestoreManager
import com.ssajudn.barebudget.data.local.UserSessionManager
import com.ssajudn.barebudget.data.local.LocalDataResetter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import com.ssajudn.barebudget.ui.common.OperationState
import com.ssajudn.barebudget.ui.common.UiEffect
import javax.inject.Inject

data class SettingsUiState(
    val userId: String = "",
    val isLoading: Boolean = false,
    val isSignedOut: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val sessionManager: UserSessionManager,
    private val dataResetter: LocalDataResetter,
    private val backupManager: BackupRestoreManager
) : ViewModel() {
    private val _operation = MutableStateFlow<OperationState>(OperationState.Idle)
    val operation: StateFlow<OperationState> = _operation.asStateFlow()
    private val _effect = Channel<UiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        _uiState.value = _uiState.value.copy(userId = sessionManager.userId)
    }

    fun exportBackup(uri: android.net.Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            _operation.value = OperationState.Loading
            val result = backupManager.exportBackupToUri(uri)
            _uiState.value = _uiState.value.copy(isLoading = false)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(successMessage = "Backup berhasil diekspor ke file! Simpan file ini untuk restore kapan saja.")
                _operation.value = OperationState.Success()
                _effect.send(UiEffect.ShowSnackbar("Backup diekspor"))
            } else {
                val msg = "Gagal mengekspor backup: ${result.exceptionOrNull()?.localizedMessage}"
                _uiState.value = _uiState.value.copy(errorMessage = msg)
                _operation.value = OperationState.Error(msg)
                _effect.send(UiEffect.ShowSnackbar(msg))
            }
        }
    }

    fun importBackup(uri: android.net.Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            _operation.value = OperationState.Loading
            val result = backupManager.importBackupFromUri(uri)
            _uiState.value = _uiState.value.copy(isLoading = false)
            if (result.isSuccess) {
                val count = result.getOrNull() ?: 0
                _uiState.value = _uiState.value.copy(successMessage = "Berhasil memulihkan $count data dari file backup!")
                _operation.value = OperationState.Success()
                _effect.send(UiEffect.ShowSnackbar("Restore berhasil"))
            } else {
                val msg = "Gagal memulihkan backup. Pastikan format file benar: ${result.exceptionOrNull()?.localizedMessage}"
                _uiState.value = _uiState.value.copy(errorMessage = msg)
                _operation.value = OperationState.Error(msg)
                _effect.send(UiEffect.ShowSnackbar(msg))
            }
        }
    }

    /**
     * Full-offline app: "sign out" means wiping all local data and returning
     * to onboarding so a fresh start can be made on this device.
     */
    fun signOut() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            _operation.value = OperationState.Loading
            try {
                dataResetter.wipe()
            } catch (_: Exception) {
            }
            sessionManager.clearSession(preserveOnboarding = false)
            _uiState.value = _uiState.value.copy(isLoading = false, isSignedOut = true)
            _operation.value = OperationState.Success()
            _effect.send(UiEffect.Navigate("splash"))
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
