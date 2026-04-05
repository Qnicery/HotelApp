package com.example.testapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.data.model.User
import com.example.testapp.data.model.UserRole
import com.example.testapp.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel для авторизации и регистрации
 * Использует общий AppRepository через синглтон
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository.getInstance(application)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val currentUser: StateFlow<User?> = repository.currentUser

    init {
        // Проверяем, есть ли сохранённый пользователь
        updateCurrentUser()
    }

    private fun updateCurrentUser() {
        val user = repository.getCurrentUser()
        _uiState.value = _uiState.value.copy(
            currentUser = user,
            isLoggedIn = user != null
        )
    }

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email.trim())
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name.trim())
    }

    fun updateConfirmPassword(password: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = password)
    }

    fun login(): Result<Unit> {
        val state = _uiState.value
        val email = state.email
        val password = state.password

        // Валидация
        if (email.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Введите email")
            return Result.failure(Exception("Введите email"))
        }

        if (password.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Введите пароль")
            return Result.failure(Exception("Введите пароль"))
        }

        // Попытка входа
        return repository.login(email, password)
            .map { } // Игнорируем результат, возвращаем Unit
            .onSuccess {
                updateCurrentUser()
                _uiState.value = state.copy(
                    isLoggedIn = true,
                    loginSuccess = true,
                    errorMessage = null,
                    currentUser = currentUser.value
                )
            }
            .onFailure { error ->
                _uiState.value = state.copy(
                    loginSuccess = false,
                    errorMessage = error.message ?: "Ошибка входа"
                )
            }
    }

    fun register(): Result<Unit> {
        val state = _uiState.value
        val name = state.name
        val email = state.email
        val password = state.password
        val confirmPassword = state.confirmPassword

        // Валидация
        if (name.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Введите имя")
            return Result.failure(Exception("Введите имя"))
        }

        if (email.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Введите email")
            return Result.failure(Exception("Введите email"))
        }

        if (password.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Введите пароль")
            return Result.failure(Exception("Введите пароль"))
        }

        if (password.length < 6) {
            _uiState.value = state.copy(errorMessage = "Пароль должен содержать минимум 6 символов")
            return Result.failure(Exception("Пароль слишком короткий"))
        }

        if (password != confirmPassword) {
            _uiState.value = state.copy(errorMessage = "Пароли не совпадают")
            return Result.failure(Exception("Пароли не совпадают"))
        }

        // Попытка регистрации
        return repository.register(name, email, password)
            .map { } // Игнорируем результат, возвращаем Unit
            .onSuccess {
                updateCurrentUser()
                _uiState.value = state.copy(
                    isLoggedIn = true,
                    registerSuccess = true,
                    errorMessage = null,
                    currentUser = currentUser.value
                )
            }
            .onFailure { error ->
                _uiState.value = state.copy(
                    registerSuccess = false,
                    errorMessage = error.message ?: "Ошибка регистрации"
                )
            }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun logout() {
        repository.logout()
        _uiState.value = AuthUiState()
    }

    fun getCurrentUser(): User? = repository.getCurrentUser()

    fun getUserRole(): UserRole? = repository.getCurrentUser()?.role
}

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val name: String = "",
    val isLoggedIn: Boolean = false,
    val loginSuccess: Boolean = false,
    val registerSuccess: Boolean = false,
    val errorMessage: String? = null,
    val currentUser: User? = null
)
