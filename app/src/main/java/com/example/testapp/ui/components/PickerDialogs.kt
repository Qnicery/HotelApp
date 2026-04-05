package com.example.testapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale


/**
 * Диалог выбора города
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityPickerDialog(
    cities: List<String>,
    selectedCity: String?,
    onCitySelected: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    var tempSelectedCity by remember { mutableStateOf(selectedCity) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .size(360.dp, 300.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary)
        ) {
            Column {
                // Заголовок
                Text(
                    text = "Выберите город",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(16.dp),
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )

                // Список городов (скроллится)
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(cities) { city ->
                        CityListItem(
                            city = city,
                            isSelected = tempSelectedCity == city,
                            onClick = { tempSelectedCity = city }
                        )
                    }
                }

                // Кнопки (фиксированы)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Отмена")
                    }

                    TextButton(
                        onClick = {
                            onCitySelected(tempSelectedCity)
                            onDismiss()
                        },
                        enabled = tempSelectedCity != null
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

@Composable
private fun CityListItem(
    city: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else Color.Transparent
            )
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = city,
            fontSize = 16.sp,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        )

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Выбрано",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Диалог выбора даты
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RangeDatePickerDialog(
    initialStartDate: String? = null,
    initialEndDate: String? = null,
    onDateRangeSelected: (start: String, end: String) -> Unit,
    onDismiss: () -> Unit
) {
    val zone = java.time.ZoneId.systemDefault()

    val initialStartMillis = initialStartDate?.let {
        java.time.LocalDate.parse(it)
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()
    }

    val initialEndMillis = initialEndDate?.let {
        java.time.LocalDate.parse(it)
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()
    }

    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialStartMillis,
        initialSelectedEndDateMillis = initialEndMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val startMillis = state.selectedStartDateMillis
                    val endMillis = state.selectedEndDateMillis

                    if (startMillis != null && endMillis != null) {
                        val startDate = java.time.LocalDate.ofInstant(
                            java.time.Instant.ofEpochMilli(startMillis), zone
                        )
                        val endDate = java.time.LocalDate.ofInstant(
                            java.time.Instant.ofEpochMilli(endMillis), zone
                        )

                        onDateRangeSelected(
                            startDate.toString(),
                            endDate.toString()
                        )
                        onDismiss()
                    }
                },
                enabled = state.selectedStartDateMillis != null &&
                        state.selectedEndDateMillis != null
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    ) {
        DateRangePicker(
            state = state,
            showModeToggle = false,
            title = null,
            headline = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
        )
    }
}

@Composable
fun SimpleRangeDatePicker(
    selectedStart: LocalDate?,
    selectedEnd: LocalDate?,
    onRangeSelected: (LocalDate, LocalDate) -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var startDate by remember { mutableStateOf(selectedStart) }
    var endDate by remember { mutableStateOf(selectedEnd) }

    val monthName = currentMonth.month.getDisplayName(
        java.time.format.TextStyle.FULL_STANDALONE, Locale("ru")
    )

    val days = remember(currentMonth) {
        val firstDayOfMonth = currentMonth.atDay(1)
        val daysInMonth = currentMonth.lengthOfMonth()

        val startOffset = (firstDayOfMonth.dayOfWeek.value + 6) % 7 // Пн = 0
        val totalDays = startOffset + daysInMonth

        List(totalDays) { index ->
            if (index < startOffset) null
            else currentMonth.atDay(index - startOffset + 1)
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {

        //  Header (месяц + переключение)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
            }

            Text(

                text = monthName
                    .replaceFirstChar { it.uppercase() } + " ${currentMonth.year}",
                style = MaterialTheme.typography.titleMedium
            )

            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        //  Дни недели
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Пн","Вт","Ср","Чт","Пт","Сб","Вс").forEach {
                Text(
                    text = it,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        //  Сетка дней
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(250.dp)
        ) {
            items(days.size) { index ->
                val date = days[index]

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(2.dp)
                        .clickable(enabled = date != null) {
                            if (date == null) return@clickable

                            if (startDate == null || (startDate != null && endDate != null)) {
                                startDate = date
                                endDate = null
                            } else if (date >= startDate!!) {
                                endDate = date
                                onRangeSelected(startDate!!, endDate!!)
                            } else {
                                startDate = date
                                endDate = null
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (date != null) {
                        val isStart = date == startDate
                        val isEnd = date == endDate
                        val inRange = startDate != null && endDate != null &&
                                date > startDate && date < endDate

                        val bgColor = when {
                            isStart || isEnd -> MaterialTheme.colorScheme.primary
                            inRange -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else -> Color.Transparent
                        }

                        val textColor = if (isStart || isEnd)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurface

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(bgColor, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = date.dayOfMonth.toString(),
                                color = textColor
                            )
                        }
                    }
                }
            }
        }
    }
}
