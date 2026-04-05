package com.example.testapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.testapp.ui.components.CityPickerDialog
import com.example.testapp.ui.components.RangeDatePickerDialog
import com.example.testapp.ui.components.SimpleRangeDatePicker
import com.example.testapp.ui.viewmodel.SearchViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onNavigateToResults: () -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Состояния диалогов
    var showCityDialog by remember { mutableStateOf(false) }
    var dateRangeText by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    var tempStartDate by remember { mutableStateOf<LocalDate?>(null) }
    var tempEndDate by remember { mutableStateOf<LocalDate?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Поиск отелей",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Заголовок
            Text(
                text = "Найдите свой идеальный отель",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Город - OutlinedTextField с кнопкой
            OutlinedTextField(
                value = uiState.selectedCity ?: "",
                onValueChange = { },
                label = { Text("Город") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Выберите город") },
                trailingIcon = {
                    IconButton(onClick = { showCityDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Выбрать город",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )

            // Даты
            OutlinedTextField(
                value = dateRangeText,
                onValueChange = { },
                readOnly = true,
                label = { Text("Даты") },
                placeholder = { Text("Выберите даты") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Выбрать даты",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )

            // Количество гостей
            OutlinedTextField(
                value = uiState.guests.toString(),
                onValueChange = { value ->
                    value.toIntOrNull()?.let { guests ->
                        viewModel.setGuests(guests)
                    }
                },
                label = { Text("Количество гостей") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Введите число") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Кнопка поиска
            Button(
                onClick = {
                    viewModel.performSearch()
                    onNavigateToResults()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Найти отели",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Ошибка
            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }
        }
    }

    // Диалог выбора города
    if (showCityDialog) {
        CityPickerDialog(
            cities = uiState.availableCities,
            selectedCity = uiState.selectedCity,
            onCitySelected = { viewModel.selectCity(it) },
            onDismiss = { showCityDialog = false }
        )
    }

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Card (
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary)
            ){
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
                                    dateRangeText = formatDateRange(
                                        tempStartDate!!,
                                        tempEndDate!!
                                    )
                                }
                                showDialog = false
                            },
                            enabled = tempStartDate != null && tempEndDate != null
                        ) {
                            Text("Ок")
                        }
                        TextButton(onClick = { showDialog = false }) {
                            Text("Отмена")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Форматирование даты для отображения
 */
private fun formatDateRange(start: LocalDate, end: LocalDate): String {
    val dayFormatter = DateTimeFormatter.ofPattern("dd")
    val monthFormatter = DateTimeFormatter.ofPattern("MMM", Locale("ru"))

    return if (start.month == end.month) {
        // 01-11 апр
        "${start.format(dayFormatter)}-${end.format(dayFormatter)} ${end.format(monthFormatter)}"
    } else {
        // 01 апр - 01 мая
        "${start.format(dayFormatter)} ${start.format(monthFormatter)} - " +
                "${end.format(dayFormatter)} ${end.format(monthFormatter)}"
    }
}
