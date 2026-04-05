package com.example.testapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.testapp.data.model.Review
import com.example.testapp.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel для экрана отзывов
 * Использует общий AppRepository через синглтон
 */
class ReviewsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository.getInstance(application)

    private val _uiState = MutableStateFlow(ReviewsUiState())
    val uiState: StateFlow<ReviewsUiState> = _uiState.asStateFlow()

    fun loadReviews(hotelId: Int) {
        val reviews = repository.getReviewsByHotel(hotelId)
        _uiState.value = _uiState.value.copy(
            reviews = reviews,
            isLoading = false
        )
    }

    fun addReview(hotelId: Int, rating: Float, text: String): Result<Unit> {
        val currentUser = repository.getCurrentUser()
            ?: return Result.failure(Exception("Необходимо авторизоваться"))

        if (text.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Введите текст отзыва")
            return Result.failure(Exception("Введите текст отзыва"))
        }

        if (rating < 1f || rating > 5f) {
            _uiState.value = _uiState.value.copy(error = "Рейтинг должен быть от 1 до 5")
            return Result.failure(Exception("Некорректный рейтинг"))
        }

        return repository.addReview(hotelId, currentUser.id, rating, text)
            .map { } // Игнорируем результат, возвращаем Unit
            .onSuccess {
                val currentState = _uiState.value
                val newReview = repository.getReviewsByHotel(hotelId).lastOrNull()
                if (newReview != null) {
                    _uiState.value = currentState.copy(
                        reviews = currentState.reviews + newReview,
                        reviewSuccess = true,
                        error = null
                    )
                }
            }
            .onFailure { error ->
                _uiState.value = _uiState.value.copy(error = error.message)
            }
    }

    fun updateRating(rating: Float) {
        _uiState.value = _uiState.value.copy(newRating = rating)
    }

    fun updateReviewText(text: String) {
        _uiState.value = _uiState.value.copy(newReviewText = text)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetReviewForm() {
        val state = _uiState.value
        _uiState.value = state.copy(
            newRating = 0f,
            newReviewText = "",
            reviewSuccess = false
        )
    }
}

data class ReviewsUiState(
    val reviews: List<Review> = emptyList(),
    val newRating: Float = 0f,
    val newReviewText: String = "",
    val isLoading: Boolean = true,
    val reviewSuccess: Boolean = false,
    val error: String? = null
)
