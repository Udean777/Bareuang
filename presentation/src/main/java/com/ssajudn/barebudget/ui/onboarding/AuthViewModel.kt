package com.ssajudn.barebudget.ui.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.barebudget.data.auth.AuthManager
import com.ssajudn.barebudget.data.auth.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import com.ssajudn.barebudget.ui.common.OperationState
import com.ssajudn.barebudget.ui.common.UiEffect
import com.ssajudn.barebudget.presentation.R
import javax.inject.Inject

/**
 * Thin wrapper around [AuthManager] for the onboarding/auth screens.
 *
 * Previously [AuthScreen] and [OnboardingScreen] each constructed their own
 * `AuthManager(context)` instance via `LocalContext`. With Hilt, [AuthManager]
 * is a singleton shared with [com.ssajudn.barebudget.ui.settings.SettingsViewModel],
 * so the same Firebase Auth state and CredentialManager is reused everywhere.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val authManager: AuthManager
) : ViewModel() {
    private val _operation = MutableStateFlow<OperationState>(OperationState.Idle)
    val operation: StateFlow<OperationState> = _operation.asStateFlow()
    private val _effect = Channel<UiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()


    fun signInWithGoogle(onResult: (AuthResult) -> Unit) {
        viewModelScope.launch {
            _operation.value = OperationState.Loading
            val r = authManager.signInWithGoogle()
            onResult(r)
            when (r) {
                is AuthResult.Success -> { _operation.value = OperationState.Success(); _effect.send(UiEffect.Navigate("dashboard")) }
                is AuthResult.Error -> { _operation.value = OperationState.Error(r.message); _effect.send(UiEffect.ShowSnackbar(r.message)) }
                is AuthResult.Cancelled -> _operation.value = OperationState.Idle
                AuthResult.Offline -> {
                    val msg = appContext.getString(R.string.auth_offline_message)
                    _operation.value = OperationState.Error(msg)
                    _effect.send(UiEffect.ShowSnackbar(msg))
                }
            }
        }
    }

    fun signInAnonymously(onResult: (AuthResult) -> Unit) {
        viewModelScope.launch {
            _operation.value = OperationState.Loading
            val r = authManager.signInAnonymously()
            onResult(r)
            when (r) {
                is AuthResult.Success -> { _operation.value = OperationState.Success(); _effect.send(UiEffect.Navigate("dashboard")) }
                is AuthResult.Error -> { _operation.value = OperationState.Error(r.message); _effect.send(UiEffect.ShowSnackbar(r.message)) }
                is AuthResult.Cancelled -> _operation.value = OperationState.Idle
                AuthResult.Offline -> {
                    // Guest mode works offline; only the Firebase round-trip failed.
                    val msg = appContext.getString(R.string.auth_guest_offline_message)
                    _operation.value = OperationState.Success()
                    _effect.send(UiEffect.ShowSnackbar(msg))
                }
            }
        }
    }
}
