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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.testapp.ui.viewmodel.BookingViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    roomId: Int,
    onNavigateBack: () -> Unit,
    onBookingSuccess: () -> Unit,
    viewModel: BookingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCheckInPicker by remember { mutableStateOf(false) }
    var showCheckOutPicker by remember { mutableStateOf(false) }

    LaunchedEffect(roomId) {
        viewModel.loadRoom(roomId)
    }

    LaunchedEffect(uiState.bookingSuccess) {
        if (uiState.bookingSuccess) {
            onBookingSuccess()
        }
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

                    // Дата заезда
                    Text(
                        text = "Дата заезда",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    OutlinedTextField(
                        value = uiState.checkInDate ?: "",
                        onValueChange = {},
                        label = { Text("Выберите дату заезда") },
                        leadingIcon = {
                            Icon(Icons.Default.CalendarToday, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        readOnly = true,
                        placeholder = { Text("ГГГГ-ММ-ДД") },
                        trailingIcon = {
                            IconButton(onClick = { showCheckInPicker = true }) {
                                Icon(Icons.Default.Event, contentDescription = "Выбрать дату")
                            }
                        }
                    )

                    // Дата выезда
                    Text(
                        text = "Дата выезда",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    OutlinedTextField(
                        value = uiState.checkOutDate ?: "",
                        onValueChange = {},
                        label = { Text("Выберите дату выезда") },
                        leadingIcon = {
                            Icon(Icons.Default.CalendarToday, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        readOnly = true,
                        placeholder = { Text("ГГГГ-ММ-ДД") },
                        trailingIcon = {
                            IconButton(onClick = { showCheckOutPicker = true }) {
                                Icon(Icons.Default.Event, contentDescription = "Выбрать дату")
                            }
                        }
                    )

                    // Количество гостей
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
                        onClick = {
                            viewModel.createBooking()
                        },
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

                // DatePicker для заезда
                if (showCheckInPicker) {
                    DatePickerDialog(
                        onDismissRequest = { showCheckInPicker = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    val date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                                    viewModel.updateCheckInDate(date)
                                    showCheckInPicker = false
                                }
                            ) {
                                Text("OK")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showCheckInPicker = false }) {
                                Text("Отмена")
                            }
                        }
                    ) {
                        Text("Выберите дату заезда")
                    }
                }

                // DatePicker для выезда
                if (showCheckOutPicker) {
                    DatePickerDialog(
                        onDismissRequest = { showCheckOutPicker = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    val date = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
                                    viewModel.updateCheckOutDate(date)
                                    showCheckOutPicker = false
                                }
                            ) {
                                Text("OK")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showCheckOutPicker = false }) {
                                Text("Отмена")
                            }
                        }
                    ) {
                        Text("Выберите дату выезда")
                    }
                }
            }
        }
    }
}

private fun guestLabel(count: Int): String {
    return when {
        count % 10 == 1 && count % 100 != 11 -> "гость"
        count % 10 in 2..4 && (count % 100 !in 12..14) -> "гостя"
        else -> "гостей"
    }
}
