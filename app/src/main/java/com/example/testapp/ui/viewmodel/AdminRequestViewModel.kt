package com.example.testapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.data.api.model.AdminRequestDTO
import com.example.testapp.data.repository.AdminSystemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана заявки на администратора
 */
class AdminRequestViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AdminSystemRepository.getInstance(application)

    private val _uiState = MutableStateFlow(AdminRequestUiState())
    val uiState: StateFlow<AdminRequestUiState> = _uiState.asStateFlow()

    /**
     * Подать заявку на администратора
     */
    fun submitAdminRequest(userId: Int, text: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, error = null)

            val result = repository.createAdminRequest(userId, text)

            _uiState.value = _uiState.value.copy(
                isSubmitting = false,
                isSubmitted = result.isSuccess,
                error = if (result.isFailure) result.exceptionOrNull()?.message else null
            )
        }
    }

    /**
     * Загрузить заявки пользователя
     */
    fun loadUserRequests(userId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = repository.getAdminRequestsByUserId(userId)

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                userRequests = if (result.isSuccess) result.getOrNull() ?: emptyList() else emptyList(),
                error = if (result.isFailure) result.exceptionOrNull()?.message else null
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class AdminRequestUiState(
    val userRequests: List<AdminRequestDTO> = emptyList(),
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false,
    val error: String? = null
)
