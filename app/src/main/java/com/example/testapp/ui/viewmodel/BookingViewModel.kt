package com.example.testapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.testapp.data.model.Booking
import com.example.testapp.data.model.Room
import com.example.testapp.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * ViewModel для экрана бронирования
 * Использует общий AppRepository через синглтон
 */
class BookingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository.getInstance(application)

    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    fun loadRoom(roomId: Int) {
        val room = repository.getRoomById(roomId)
        if (room != null) {
            _uiState.value = _uiState.value.copy(
                room = room,
                isLoading = false
            )
        } else {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Номер не найден"
            )
        }
    }

    fun updateCheckInDate(date: String) {
        val state = _uiState.value
        val checkOutDate = state.checkOutDate

        // Проверка: check-in не может быть в прошлом
        val today = LocalDate.now()
        val checkIn = LocalDate.parse(date)

        if (checkIn.isBefore(today)) {
            _uiState.value = state.copy(error = "Дата заезда не может быть в прошлом")
            return
        }

        // Если check-out раньше check-in, обновляем check-out
        val newCheckOutDate = if (checkOutDate != null) {
            val checkOut = LocalDate.parse(checkOutDate)
            if (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
                checkIn.plusDays(1).toString()
            } else {
                checkOutDate
            }
        } else {
            checkIn.plusDays(1).toString()
        }

        _uiState.value = state.copy(
            checkInDate = date,
            checkOutDate = newCheckOutDate,
            error = null
        )
        calculateTotal()
    }

    fun updateCheckOutDate(date: String) {
        val state = _uiState.value
        val checkInDate = state.checkInDate

        if (checkInDate != null) {
            val checkIn = LocalDate.parse(checkInDate)
            val checkOut = LocalDate.parse(date)

            if (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
                _uiState.value = state.copy(error = "Дата выезда должна быть позже даты заезда")
                return
            }
        }

        _uiState.value = state.copy(checkOutDate = date, error = null)
        calculateTotal()
    }

    fun updateGuests(count: Int) {
        val state = _uiState.value
        val maxGuests = state.room?.maxGuests ?: 1

        if (count < 1 || count > maxGuests) {
            _uiState.value = state.copy(error = "Количество гостей должно быть от 1 до $maxGuests")
            return
        }

        _uiState.value = state.copy(guests = count, error = null)
    }

    private fun calculateTotal() {
        val state = _uiState.value
        val room = state.room ?: return
        val checkInDate = state.checkInDate ?: return
        val checkOutDate = state.checkOutDate ?: return

        try {
            val from = LocalDate.parse(checkInDate)
            val to = LocalDate.parse(checkOutDate)
            val nights = ChronoUnit.DAYS.between(from, to).toInt().coerceAtLeast(1)
            val total = room.pricePerNight * nights

            _uiState.value = state.copy(
                totalNights = nights,
                totalPrice = total
            )
        } catch (e: Exception) {
            _uiState.value = state.copy(error = "Ошибка расчёта стоимости")
        }
    }

    fun createBooking(): Result<Booking> {
        val state = _uiState.value
        val room = state.room ?: return Result.failure(Exception("Номер не выбран"))
        val checkInDate = state.checkInDate ?: return Result.failure(Exception("Дата заезда не выбрана"))
        val checkOutDate = state.checkOutDate ?: return Result.failure(Exception("Дата выезда не выбрана"))
        val guests = state.guests

        val currentUser = repository.getCurrentUser()
            ?: return Result.failure(Exception("Пользователь не авторизован"))

        return repository.createBooking(
            roomId = room.id,
            userId = currentUser.id,
            dateFrom = checkInDate,
            dateTo = checkOutDate,
            guests = guests
        ).onSuccess {
            _uiState.value = state.copy(bookingSuccess = true)
        }.onFailure {
            _uiState.value = state.copy(error = it.message)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun reset() {
        _uiState.value = BookingUiState()
    }
}

data class BookingUiState(
    val room: Room? = null,
    val checkInDate: String? = null,
    val checkOutDate: String? = null,
    val guests: Int = 2,
    val totalNights: Int = 0,
    val totalPrice: Double = 0.0,
    val isLoading: Boolean = true,
    val bookingSuccess: Boolean = false,
    val error: String? = null
)
