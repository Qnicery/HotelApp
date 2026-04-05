package com.example.testapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Строка ввода дат с кнопкой календаря справа
 * При нажатии на кнопку открывается диалог с выбором диапазона дат
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerField(
    checkInDate: String?,
    checkOutDate: String?,
    onDateRangeSelected: (checkIn: String, checkOut: String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Даты"
) {
    var showDialog by remember { mutableStateOf(false) }

    // Отображение дат в TextField
    OutlinedTextField(
        value = if (checkInDate != null && checkOutDate != null) {
            "${formatLocalDate(checkInDate)} - ${formatLocalDate(checkOutDate)}"
        } else {
            ""
        },
        onValueChange = { },
        readOnly = true,
        label = { Text(label) },
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        placeholder = { Text("Выберите даты заезда и выезда") },
        singleLine = true,
        isError = checkInDate != null && checkOutDate == null,
        trailingIcon = {
            IconButton(onClick = { showDialog = true }) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Выбрать даты",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    )

    // Диалог с DateRangePicker
    if (showDialog) {
        DateRangePickerDialog(
            onDateRangeSelected = { checkIn, checkOut ->
                onDateRangeSelected(checkIn, checkOut)
            },
            onDismiss = { showDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerDialog(
    onDateRangeSelected: (checkIn: String, checkOut: String) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberDateRangePickerState()
    var lastSelectedMillis by remember { mutableLongStateOf(0L) }

    // Отслеживаем изменение выбранных дат
    LaunchedEffect(state.selectedStartDateMillis, state.selectedEndDateMillis) {
        val startMillis = state.selectedStartDateMillis
        val endMillis = state.selectedEndDateMillis

        if (startMillis != null && endMillis != null && endMillis != lastSelectedMillis) {
            lastSelectedMillis = endMillis
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Column {
            DateRangePicker(
                state = state,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                title = {
                    Text(
                        text = "Выберите даты",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                },
                headline = {
                    Text(
                        text = "Даты проживания",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                },
                showModeToggle = true  // Переключатель месяц/год
            )

            // Кнопки действий
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Отмена")
                }

                TextButton(
                    onClick = {
                        val startMillis = state.selectedStartDateMillis
                        val endMillis = state.selectedEndDateMillis
                        if (startMillis != null && endMillis != null) {
                            val checkIn = millisToLocalDate(startMillis).toString()
                            val checkOut = millisToLocalDate(endMillis).toString()
                            onDateRangeSelected(checkIn, checkOut)
                        }
                    },
                    enabled = state.selectedStartDateMillis != null && state.selectedEndDateMillis != null
                ) {
                    Text("OK")
                }
            }
        }
    }
}

/**
 * Преобразование millis в LocalDate
 */
private fun millisToLocalDate(millis: Long): LocalDate {
    return Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}

/**
 * Форматирование даты для отображения
 */
fun formatLocalDate(dateString: String): String {
    return try {
        val date = LocalDate.parse(dateString)
        date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    } catch (e: Exception) {
        dateString
    }
}
