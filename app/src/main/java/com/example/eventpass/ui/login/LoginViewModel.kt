package com.example.eventpass.ui.login

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * UI state for the login screen, exposed as an immutable snapshot.
 *
 * @property username      current text in the username field
 * @property password      current text in the password field
 * @property error         validation/auth error to show, or null
 * @property isLoading     whether a login attempt is in progress
 * @property loginSuccess  one-shot flag flipped true on a successful login
 */
data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val error: String? = null,
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false
)

/**
 * Handles login form state and (local) authentication.
 *
 * For this college project authentication is entirely local — there is no
 * backend. Valid demo credentials are username "admin" / password "admin123".
 */
class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUsernameChange(value: String) {
        _uiState.update { it.copy(username = value, error = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    /**
     * Validates the form and checks the credentials. Updates [uiState] with
     * either an error message or [LoginUiState.loginSuccess] = true.
     */
    fun login() {
        val state = _uiState.value
        val username = state.username.trim()
        val password = state.password

        // --- Validation ---
        if (username.isEmpty()) {
            _uiState.update { it.copy(error = "Please enter your username") }
            return
        }
        if (password.isEmpty()) {
            _uiState.update { it.copy(error = "Please enter your password") }
            return
        }

        // --- Local credential check ---
        _uiState.update { it.copy(isLoading = true, error = null) }
        if (username.equals(VALID_USERNAME, ignoreCase = true) && password == VALID_PASSWORD) {
            _uiState.update { it.copy(isLoading = false, loginSuccess = true) }
        } else {
            _uiState.update {
                it.copy(isLoading = false, error = "Invalid username or password")
            }
        }
    }

    /** Reset the one-shot success flag after navigation has been handled. */
    fun onLoginHandled() {
        _uiState.update { it.copy(loginSuccess = false) }
    }

    companion object {
        private const val VALID_USERNAME = "admin"
        private const val VALID_PASSWORD = "admin123"
    }
}
