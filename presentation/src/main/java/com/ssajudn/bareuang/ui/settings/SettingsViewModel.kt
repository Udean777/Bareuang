package com.ssajudn.bareuang.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.bareuang.data.local.BackupRestoreManager
import com.ssajudn.bareuang.data.local.ThemePreferences
import com.ssajudn.bareuang.data.local.TourPreferences
import com.ssajudn.bareuang.data.local.UserSessionManager
import com.ssajudn.bareuang.data.local.LocalDataResetter
import com.ssajudn.bareuang.domain.model.AppThemeDarkMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.common.UiText
import com.ssajudn.bareuang.ui.common.OperationState
import com.ssajudn.bareuang.ui.common.UiEffect
import javax.inject.Inject

data class SettingsUiState(
    val userId: String = "",
    val isLoading: Boolean = false,
    val isSignedOut: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val errorText: UiText? = null,
    val successText: UiText? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val sessionManager: UserSessionManager,
    private val dataResetter: LocalDataResetter,
    private val backupManager: BackupRestoreManager,
    private val themePrefs: ThemePreferences,
    private val tourPrefs: TourPreferences,
    private val widgetPrefs: com.ssajudn.bareuang.data.local.WidgetPreferences,
    private val currencyPrefs: com.ssajudn.bareuang.data.local.CurrencyPreferences
) : ViewModel() {

    val darkMode get() = themePrefs.darkMode
    val widgetHideBalance get() = widgetPrefs.hideBalance
    val currency get() = currencyPrefs.currency

    fun setDarkMode(mode: AppThemeDarkMode) = themePrefs.setDarkMode(mode)

    fun setCurrency(currency: com.ssajudn.bareuang.domain.model.AppCurrency) = currencyPrefs.setCurrency(currency)

    fun setHideBalance(hidden: Boolean) = widgetPrefs.setHideBalance(hidden)

    fun resetTour() = tourPrefs.resetTour()

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
                val ui = UiText.Res(R.string.settings_backup_success_msg)
                _uiState.value = _uiState.value.copy(successMessage = null, successText = ui)
                _operation.value = OperationState.Success()
                _effect.send(UiEffect.ShowSnackbarRes(UiText.Res(R.string.settings_backup_success_snack)))
            } else {
                val ex = result.exceptionOrNull()?.localizedMessage ?: ""
                val ui = UiText.Res(R.string.settings_backup_failed, listOf(ex))
                _uiState.value = _uiState.value.copy(errorMessage = null, errorText = ui)
                _operation.value = OperationState.Error(ex, ui)
                _effect.send(UiEffect.ShowSnackbarRes(ui))
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
                val ui = UiText.Res(R.string.settings_restore_success_msg, listOf(count))
                _uiState.value = _uiState.value.copy(successMessage = null, successText = ui)
                _operation.value = OperationState.Success()
                _effect.send(UiEffect.ShowSnackbarRes(UiText.Res(R.string.settings_restore_success_snack)))
            } else {
                val ex = result.exceptionOrNull()?.localizedMessage ?: ""
                val ui = UiText.Res(R.string.settings_restore_failed, listOf(ex))
                _uiState.value = _uiState.value.copy(errorMessage = null, errorText = ui)
                _operation.value = OperationState.Error(ex, ui)
                _effect.send(UiEffect.ShowSnackbarRes(ui))
            }
        }
    }


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