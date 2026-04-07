package com.example.testapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.testapp.ui.components.DateRangePickerDialog
import com.example.testapp.ui.components.SimpleRangeDatePicker
import com.example.testapp.ui.viewmodel.BookingViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    hotelId: Int,
    roomId: Int,
    checkInDate: String? = null,
    checkOutDate: String? = null,
    guests: Int? = null,
    onNavigateBack: () -> Unit,
    onBookingSuccess: () -> Unit,
    viewModel: BookingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDateRangePicker by remember { mutableStateOf(false) }

    var tempStartDate by remember { mutableStateOf<LocalDate?>(null) }
    var tempEndDate by remember { mutableStateOf<LocalDate?>(null) }

    LaunchedEffect(hotelId, roomId) {
        viewModel.loadRoom(hotelId, roomId, checkInDate, checkOutDate, guests)
    }

    LaunchedEffect(uiState.bookingSuccess) {
        if (uiState.bookingSuccess) onBookingSuccess()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Бронирование") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val room = uiState.room
            if (room != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Информация о номере
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = room.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = room.description,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Диапазон дат — единое поле как на экране поиска
                    Text(
                        text = "Даты проживания",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    OutlinedTextField(
                        value = if (uiState.checkInDate != null && uiState.checkOutDate != null) {
                            formatDateRangeDisplay(
                                LocalDate.parse(uiState.checkInDate!!),
                                LocalDate.parse(uiState.checkOutDate!!)
                            )
                        } else {
                            ""
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Заезд — Выезд") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Выберите даты заезда и выезда") },
                        leadingIcon = {
                            Icon(Icons.Default.CalendarToday, contentDescription = null)
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        isError = uiState.checkInDate != null && uiState.checkOutDate == null,
                        trailingIcon = {
                            IconButton(onClick = { showDateRangePicker = true }) {
                                Icon(
                                    imageVector = Icons.Default.Event,
                                    contentDescription = "Выбрать даты",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )

                    // Количество гостей — dropdown
                    Text(
                        text = "Количество гостей",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    var guestsDropdownExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = guestsDropdownExpanded,
                        onExpandedChange = { guestsDropdownExpanded = !guestsDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = "${uiState.guests} ${guestLabel(uiState.guests)}",
                            onValueChange = {},
                            label = { Text("Гости") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = guestsDropdownExpanded)
                            }
                        )

                        ExposedDropdownMenu(
                            expanded = guestsDropdownExpanded,
                            onDismissRequest = { guestsDropdownExpanded = false }
                        ) {
                            for (i in 1..room.maxGuests) {
                                DropdownMenuItem(
                                    text = { Text("${i} ${guestLabel(i)}") },
                                    onClick = {
                                        viewModel.updateGuests(i)
                                        guestsDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Итоговая информация
                    if (uiState.checkInDate != null && uiState.checkOutDate != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Период
                                Text(
                                    text = "Период: ${formatDateRangeDisplay(LocalDate.parse(uiState.checkInDate!!), LocalDate.parse(uiState.checkOutDate!!))}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Количество ночей:",
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "${uiState.totalNights}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Цена за ночь:",
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "${room.pricePerNight.toInt()} ₽",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Divider()

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Итого:",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${uiState.totalPrice.toInt()} ₽",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    // Сообщение об ошибке
                    uiState.error?.let { error ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = error,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Кнопка подтверждения
                    Button(
                        onClick = { viewModel.createBooking() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = uiState.checkInDate != null && uiState.checkOutDate != null
                    ) {
                        Text(
                            text = "Подтвердить бронирование",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Material3 DateRangePicker Dialog
                if (showDateRangePicker) {
                    Dialog(onDismissRequest = { showDateRangePicker = false }) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary)
                        ) {
                            Column {
                                SimpleRangeDatePicker(
                                    selectedStart = null,
                                    selectedEnd = null,
                                    onRangeSelected = { start, end ->
                                        tempStartDate = start
                                        tempEndDate = end
                                    }
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = {
                                            if (tempStartDate != null && tempEndDate != null) {
                                                viewModel.updateCheckInDate(tempStartDate!!.toString())
                                                viewModel.updateCheckOutDate(tempEndDate!!.toString())
                                            }
                                            showDateRangePicker = false
                                        },
                                        enabled = tempStartDate != null && tempEndDate != null
                                    ) {
                                        Text("Ок")
                                    }
                                    TextButton(onClick = { showDateRangePicker = false }) {
                                        Text("Отмена")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Форматирование диапазона дат для отображения
 * Примеры: "03 - 11 апр" или "01 апр - 03 мая"
 */
private fun buildDateRangeDisplay(checkIn: String?, checkOut: String?): String {
    if (checkIn == null || checkOut == null) return ""
    return formatDateRangeDisplay( LocalDate.parse(checkIn), LocalDate.parse(checkOut))
}

private fun formatDateRangeDisplay(start: LocalDate, end: LocalDate): String {
    return try {
        val dayFormatter = DateTimeFormatter.ofPattern("dd")
        val monthFormatter = DateTimeFormatter.ofPattern("MMM", Locale("ru"))

        if (start.month == end.month) {
            "${start.format(dayFormatter)} - ${end.format(dayFormatter)} ${end.format(monthFormatter)}"
        } else {
            "${start.format(dayFormatter)} ${start.format(monthFormatter)} - " +
                    "${end.format(dayFormatter)} ${end.format(monthFormatter)}"
        }
    } catch (e: Exception) {
        "$start - $end"
    }
}

private fun guestLabel(count: Int): String {
    return when {
        count % 10 == 1 && count % 100 != 11 -> "гость"
        count % 10 in 2..4 && (count % 100 !in 12..14) -> "гостя"
        else -> "гостей"
    }
}
