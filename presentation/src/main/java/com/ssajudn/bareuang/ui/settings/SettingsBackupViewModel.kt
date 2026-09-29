package com.ssajudn.bareuang.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.bareuang.domain.error.AppException
import com.ssajudn.bareuang.domain.model.BackupRestorePreview
import com.ssajudn.bareuang.domain.model.BackupRestoreSummary
import com.ssajudn.bareuang.domain.port.BackupRestorePort
import com.ssajudn.bareuang.domain.usecase.ResetLocalDataUseCase
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.common.OperationState
import com.ssajudn.bareuang.ui.common.UiEffect
import com.ssajudn.bareuang.ui.common.UiText
import com.ssajudn.bareuang.ui.common.toUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsBackupUiState(
    val isLocalDataReset: Boolean = false,
    val restorePreview: BackupRestorePreview? = null,
    val restoreSummary: BackupRestoreSummary? = null,
    val restoreError: UiText? = null,
)

@HiltViewModel
class SettingsBackupViewModel @Inject constructor(
    private val backupManager: BackupRestorePort,
    private val resetLocalDataUseCase: ResetLocalDataUseCase,
) : ViewModel() {
    private val _operation = MutableStateFlow<OperationState>(OperationState.Idle)
    val operation: StateFlow<OperationState> = _operation.asStateFlow()
    private val _effect = Channel<UiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val _uiState = MutableStateFlow(SettingsBackupUiState())
    val uiState: StateFlow<SettingsBackupUiState> = _uiState.asStateFlow()

    fun exportBackup(uri: Uri, password: CharArray) {
        viewModelScope.launch {
            setLoading()
            val result = try {
                backupManager.exportBackup(uri.toString(), password)
            } finally {
                password.fill('\u0000')
            }
            if (result.isSuccess) {
                _operation.value = OperationState.Success()
                _effect.send(UiEffect.ShowSnackbarRes(UiText.Res(R.string.settings_backup_success_snack)))
            } else {
                android.util.Log.e("SettingsBackup", "export failed", result.exceptionOrNull())
                emitError(
                    result.exceptionOrNull()?.asUiText() ?: UiText.Res(R.string.settings_error_backup),
                )
            }
        }
    }

    fun previewBackup(uri: Uri, password: CharArray?) {
        viewModelScope.launch {
            setLoading()
            _uiState.value = _uiState.value.copy(restoreError = null)
            val result = try {
                backupManager.previewBackup(uri.toString(), password)
            } finally {
                password?.fill('\u0000')
            }
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    restorePreview = result.getOrNull(),
                    restoreSummary = null,
                    restoreError = null,
                )
                _operation.value = OperationState.Idle
            } else {
                android.util.Log.e("SettingsBackup", "restore preview failed", result.exceptionOrNull())
                val error = result.exceptionOrNull()?.asUiText()
                    ?: UiText.Res(R.string.settings_restore_invalid_password_or_file)
                _uiState.value = _uiState.value.copy(restoreError = error)
                _operation.value = OperationState.Error("", error)
            }
        }
    }

    fun confirmRestore(previewId: String) {
        viewModelScope.launch {
            setLoading()
            _uiState.value = _uiState.value.copy(restoreError = null)
            val result = backupManager.confirmRestore(previewId)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    restorePreview = null,
                    restoreSummary = result.getOrNull(),
                    restoreError = null,
                )
                _operation.value = OperationState.Success()
            } else {
                android.util.Log.e("SettingsBackup", "restore apply failed", result.exceptionOrNull())
                val error = result.exceptionOrNull()?.asUiText()
                    ?: UiText.Res(R.string.settings_restore_apply_failed)
                _uiState.value = _uiState.value.copy(restoreError = error)
                _operation.value = OperationState.Error("", error)
            }
        }
    }

    fun discardRestore(previewId: String) {
        backupManager.discardRestore(previewId)
        _uiState.value = _uiState.value.copy(restorePreview = null, restoreError = null)
        _operation.value = OperationState.Idle
    }

    fun dismissRestoreSummary() {
        _uiState.value = _uiState.value.copy(restoreSummary = null)
        _operation.value = OperationState.Idle
    }

    fun clearRestoreError() {
        _uiState.value = _uiState.value.copy(restoreError = null)
    }

    fun resetLocalData() {
        viewModelScope.launch {
            setLoading()
            val result = resetLocalDataUseCase()
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isLocalDataReset = true)
                _operation.value = OperationState.Success()
            } else {
                val error = result.exceptionOrNull()?.asUiText() ?: UiText.Res(R.string.error_generic)
                emitError(error)
            }
        }
    }

    override fun onCleared() {
        _uiState.value.restorePreview?.id?.let(backupManager::discardRestore)
        super.onCleared()
    }

    private fun setLoading() {
        _operation.value = OperationState.Loading
    }

    private suspend fun emitError(error: UiText) {
        _operation.value = OperationState.Error("", error)
        _effect.send(UiEffect.ShowSnackbarRes(error))
    }

    private fun Throwable.asUiText(): UiText =
        (this as? AppException)?.toUiText() ?: UiText.Res(R.string.error_generic)
}
