package com.example.testapp.data.model

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.ui.viewmodel.SearchUiState
import com.example.testapp.ui.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersBottomSheet(
    uiState: SearchUiState,
    onDismiss: () -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit,
    viewModel: SearchViewModel
) {

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    //.padding(bottom = 100.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Заголовок
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Фильтры",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть"
                        )
                    }
                }

                // Диапазон цен
                Column {
                    Text(
                        text = "Цена за ночь",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Цена: ${uiState.priceRangeStart} ₽ - ${uiState.priceRangeEnd} ₽",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    RangeSlider(
                        value = uiState.priceRangeStart.toFloat()..uiState.priceRangeEnd.toFloat(),
                        onValueChange = { range ->
                            val start = range.start.toInt()
                            val end = range.endInclusive.toInt()

                            // минимальная дистанция 100
                            if (end - start >= 100) {
                                viewModel.setPriceRange(start, end)
                            }
                        },
                        valueRange = uiState.minPrice.toFloat()..uiState.maxPrice.toFloat(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedTextField(
                            value = "${uiState.priceRangeStart}",
                            onValueChange = {
                                it.toIntOrNull()?.let { min ->
                                    viewModel.setPriceRange(min, uiState.priceRangeEnd)
                                }
                            },
                            label = { Text("От") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        OutlinedTextField(
                            value = "${uiState.priceRangeEnd}",
                            onValueChange = {
                                it.toIntOrNull()?.let { max ->
                                    viewModel.setPriceRange(uiState.priceRangeStart, max)
                                }
                            },
                            label = { Text("До") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                HorizontalDivider()

                // Тип жилья
                Column {
                    Text(
                        text = "Тип жилья",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(HotelType.entries.toList()) { type ->
                            FilterChip(
                                selected = type in uiState.selectedHotelTypes,
                                onClick = { viewModel.toggleHotelType(type) },
                                label = { Text(type.displayName) },
                                leadingIcon = if (type in uiState.selectedHotelTypes) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                } else null
                            )
                        }
                    }
                }

                HorizontalDivider()

                // Удобства
                Column {
                    Text(
                        text = "Удобства",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val amenities = uiState.availableAmenities
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        amenities.forEach { amenity ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.toggleAmenity(amenity) }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = amenity,
                                    fontSize = 14.sp
                                )

                                if (amenity in uiState.selectedAmenities) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Выбрано",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()

                // Рейтинг
                Column {
                    Text(
                        text = "Минимальный рейтинг",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(3f, 3.5f, 4f, 4.5f).forEach { rating ->
                            FilterChip(
                                selected = uiState.minRating == rating,
                                onClick = { viewModel.setMinRating(rating) },
                                label = { Text("$rating+") },
                                leadingIcon = if (uiState.minRating == rating) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                } else null
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(100.dp))

            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.BottomCenter)
                    .background(MaterialTheme.colorScheme.onPrimary),
            ) {
                HorizontalDivider()

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { onReset() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Сбросить")
                    }

                    Button(
                        onClick = { onApply() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Применить")
                    }
                }
            }
        }
    }

}