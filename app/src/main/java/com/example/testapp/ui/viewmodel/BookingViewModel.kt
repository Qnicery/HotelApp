package com.example.testapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.data.api.model.RoomDTO
import com.example.testapp.data.model.Booking
import com.example.testapp.data.model.Room
import com.example.testapp.data.repository.AuthApiRepository
import com.example.testapp.data.repository.BookingRepository
import com.example.testapp.data.repository.HotelsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * ViewModel для экрана бронирования
 * Использует BookingRepository для работы с API
 */
class BookingViewModel(application: Application) : AndroidViewModel(application) {

    private val bookingRepository = BookingRepository.getInstance(application)
    private val hotelsRepository = HotelsRepository.getInstance(application)
    private val authApiRepository = AuthApiRepository.getInstance(application)

    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    fun loadRoom(hotelId: Int, roomId: Int, checkInDate: String? = null, checkOutDate: String? = null, guests: Int? = null) {
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            val result = hotelsRepository.getRoomsByHotelId(hotelId)
            if (result.isSuccess) {
                val roomDTOs = result.getOrNull() ?: emptyList()
                val roomDTO = roomDTOs.find { it.id == roomId }

                if (roomDTO != null) {
                    val room = mapRoomDtoToRoom(roomDTO)
                    val maxGuests = room.maxGuests
                    val initialGuests = guests?.coerceIn(1, maxGuests) ?: 2

                    _uiState.value = _uiState.value.copy(
                        room = room,
                        checkInDate = checkInDate,
                        checkOutDate = checkOutDate,
                        guests = initialGuests,
                        isLoading = false
                    )
                    if (checkInDate != null && checkOutDate != null) {
                        calculateTotal()
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Номер не найден"
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Ошибка загрузки данных номера"
                )
            }
        }
    }

    /**
     * Конвертация RoomDTO (сервер) → Room (приложение)
     */
    private fun mapRoomDtoToRoom(dto: RoomDTO): Room {
        return Room(
            id = dto.id,
            hotelId = dto.hotelId,
            name = dto.roomName,
            description = dto.description ?: "",
            imageUrl = dto.photoUrls?.firstOrNull() ?: "",
            pricePerNight = dto.price,
            maxGuests = dto.maxGuests,
            amenities = emptyList(),
            isAvailable = dto.status.equals("available", ignoreCase = true)
        )
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

    fun createBooking() {
        val state = _uiState.value
        val room = state.room ?: return
        val checkInDate = state.checkInDate ?: return
        val checkOutDate = state.checkOutDate ?: return
        val guests = state.guests
        val totalPrice = state.totalPrice

        val currentUser = authApiRepository.getCurrentUser()
            ?: run {
                _uiState.value = state.copy(error = "Пользователь не авторизован")
                return
            }

        // Форматируем даты в ISO-8601 с временем заезда/выезда
        val dateFrom = "${checkInDate}T14:00:00Z"
        val dateTo = "${checkOutDate}T12:00:00Z"

        _uiState.value = state.copy(isLoading = true)

        viewModelScope.launch {
            // Сначала проверяем доступность комнаты
            val availabilityResult = bookingRepository.checkRoomAvailability(room.id, dateFrom, dateTo)

            if (availabilityResult.isFailure) {
                _uiState.value = state.copy(
                    error = availabilityResult.exceptionOrNull()?.message ?: "Ошибка проверки доступности",
                    isLoading = false
                )
                return@launch
            }

            val availability = availabilityResult.getOrNull()
            if (availability == null) {
                _uiState.value = state.copy(
                    error = "Не удалось проверить доступность комнаты",
                    isLoading = false
                )
                return@launch
            }

            if (!availability.isAvailable) {
                val conflictingIds = availability.conflictingBookings.map { it.bookingId }
                _uiState.value = state.copy(
                    error = "Выбранные даты заняты (бронирования: ${conflictingIds.joinToString(", ")}). Выберите другие даты.",
                    isLoading = false
                )
                return@launch
            }

            // Комната доступна — создаём бронирование
            bookingRepository.createBooking(
                userId = currentUser.id,
                roomId = room.id,
                dateFrom = dateFrom,
                dateTo = dateTo,
                guests = guests,
                totalPrice = totalPrice
            ).onSuccess { booking ->
                _uiState.value = state.copy(
                    bookingSuccess = true,
                    isLoading = false,
                    createdBooking = booking
                )
            }.onFailure { error ->
                _uiState.value = state.copy(
                    error = error.message ?: "Ошибка при создании бронирования",
                    isLoading = false
                )
            }
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
    val createdBooking: Booking? = null,
    val error: String? = null
)
