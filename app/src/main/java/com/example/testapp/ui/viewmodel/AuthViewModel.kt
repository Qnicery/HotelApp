package com.example.testapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.data.model.User
import com.example.testapp.data.model.UserRole
import com.example.testapp.data.repository.AppRepository
import com.example.testapp.data.repository.AuthApiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel для авторизации и регистрации
 * Использует AuthApiRepository для работы с сервером
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authApiRepository = AuthApiRepository.getInstance(application)
    private val repository = AppRepository.getInstance(application)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val currentUser: StateFlow<User?> = authApiRepository.currentUser

    init {
        // Проверяем, есть ли сохранённый пользователь
        updateCurrentUser()
    }

    private fun updateCurrentUser() {
        val user = authApiRepository.getCurrentUser()
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

    /**
     * Авторизация пользователя через сервер
     * POST /login
     */
    fun login() {
        val state = _uiState.value
        val email = state.email
        val password = state.password

        // Валидация
        if (email.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Введите email")
            return
        }

        if (password.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Введите пароль")
            return
        }

        // Запрос к серверу
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)

            authApiRepository.login(email, password)
                .onSuccess { user ->
                    _uiState.value = state.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        loginSuccess = true,
                        errorMessage = null,
                        currentUser = user
                    )
                }
                .onFailure { error ->
                    _uiState.value = state.copy(
                        isLoading = false,
                        loginSuccess = false,
                        errorMessage = error.message ?: "Ошибка входа"
                    )
                }
        }
    }

    /**
     * Регистрация пользователя через сервер
     * POST /register
     */
    fun register() {
        val state = _uiState.value
        val name = state.name
        val email = state.email
        val password = state.password
        val confirmPassword = state.confirmPassword

        // Валидация
        if (name.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Введите имя")
            return
        }

        if (email.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Введите email")
            return
        }

        if (password.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Введите пароль")
            return
        }

        if (password.length < 8) {
            _uiState.value = state.copy(errorMessage = "Пароль должен содержать минимум 8 символов")
            return
        }

        if (password != confirmPassword) {
            _uiState.value = state.copy(errorMessage = "Пароли не совпадают")
            return
        }

        // Запрос к серверу
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)

            authApiRepository.register(name, email, password)
                .onSuccess { user ->
                    // После регистрации сразу логинимся
                    loginAfterRegistration(email, password)
                }
                .onFailure { error ->
                    _uiState.value = state.copy(
                        isLoading = false,
                        registerSuccess = false,
                        errorMessage = error.message ?: "Ошибка регистрации"
                    )
                }
        }
    }

    /**
     * Авторизация после успешной регистрации
     */
    private fun loginAfterRegistration(email: String, password: String) {
        viewModelScope.launch {
            authApiRepository.login(email, password)
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        registerSuccess = true,
                        loginSuccess = true,
                        errorMessage = null,
                        currentUser = user
                    )
                }
                .onFailure { error ->
                    // Если логин не удался, всё равно показываем успех регистрации
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        registerSuccess = true,
                        errorMessage = "Регистрация успешна! Теперь войдите в систему."
                    )
                }
        }
    }

    /**
     * Выход из системы
     * POST /logout
     */
    fun logout() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoggingOut = true)
            
            authApiRepository.logout()
            
            // Сбрасываем всё состояние
            _uiState.value = AuthUiState()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun getCurrentUser(): User? = authApiRepository.getCurrentUser()

    fun getUserRole(): UserRole? = authApiRepository.getCurrentUser()?.role

    /**
     * Проверка авторизации при запуске
     * Если есть сохранённый токен, проверяем его валидность
     */
    fun checkAuthStatus() {
        if (authApiRepository.isAuthorized()) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isCheckingAuth = true)
                
                authApiRepository.fetchCurrentUser()
                    .onSuccess { user ->
                        _uiState.value = _uiState.value.copy(
                            isCheckingAuth = false,
                            isLoggedIn = true,
                            currentUser = user
                        )
                    }
                    .onFailure {
                        // Токен невалиден, выходим
                        _uiState.value = _uiState.value.copy(isCheckingAuth = false)
                        logout()
                    }
            }
        } else {
            _uiState.value = _uiState.value.copy(isCheckingAuth = false)
        }
    }
}

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val name: String = "",
    val isLoggedIn: Boolean = false,
    val loginSuccess: Boolean = false,
    val registerSuccess: Boolean = false,
    val isLoading: Boolean = false,
    val isCheckingAuth: Boolean = false,  // Проверка авторизации при запуске
    val isLoggingOut: Boolean = false,    // Процесс выхода
    val errorMessage: String? = null,
    val currentUser: User? = null
)
