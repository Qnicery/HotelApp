package com.example.testapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.testapp.data.model.Booking
import com.example.testapp.data.model.BookingStatus
import com.example.testapp.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingHistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCancel: (Int) -> Unit,
    onNavigateToReview: (Int) -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Бронирования") },
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
            val bookings = uiState.bookings
            val activeBookings = bookings.filter { it.status in listOf(BookingStatus.CONFIRMED, BookingStatus.ACTIVE) }
            val completedBookings = bookings.filter { it.status == BookingStatus.COMPLETED }
            val cancelledBookings = bookings.filter { it.status == BookingStatus.CANCELLED }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (activeBookings.isNotEmpty()) {
                    item {
                        Text(
                            text = "Активные",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    items(activeBookings) { booking ->
                        BookingCard(
                            booking = booking,
                            onCancelClick = { onNavigateToCancel(booking.id) },
                            showCancelButton = booking.status == BookingStatus.CONFIRMED
                        )
                    }
                }

                if (completedBookings.isNotEmpty()) {
                    item {
                        Text(
                            text = "Завершенные",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    items(completedBookings) { booking ->
                        BookingCard(
                            booking = booking,
                            onReviewClick = { onNavigateToReview(booking.hotelId) },
                            showReviewButton = true
                        )
                    }
                }

                if (cancelledBookings.isNotEmpty()) {
                    item {
                        Text(
                            text = "Отмененные",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    items(cancelledBookings) { booking ->
                        BookingCard(booking = booking)
                    }
                }

                if (bookings.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BookOnline,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "У вас пока нет бронирований",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookingCard(
    booking: Booking,
    onCancelClick: () -> Unit = {},
    onReviewClick: () -> Unit = {},
    showCancelButton: Boolean = false,
    showReviewButton: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Заголовок
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = booking.hotelName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                StatusChip(status = booking.status)
            }

            // Номер
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Bed,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = booking.roomName,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Даты
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${booking.dateFrom} — ${booking.dateTo}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Гости
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${booking.guests} ${guestLabel(booking.guests)}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Цена
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Итого:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${booking.totalPrice.toInt()} ₽",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Кнопки действий
            if (showCancelButton || showReviewButton) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (showCancelButton) {
                        OutlinedButton(
                            onClick = onCancelClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Отменить")
                        }
                    }

                    if (showReviewButton) {
                        Button(
                            onClick = onReviewClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Оценить")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: BookingStatus) {
    AssistChip(
        onClick = { },
        label = { Text(status.displayName) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = when (status) {
                BookingStatus.CONFIRMED -> MaterialTheme.colorScheme.secondaryContainer
                BookingStatus.ACTIVE -> MaterialTheme.colorScheme.primaryContainer
                BookingStatus.COMPLETED -> MaterialTheme.colorScheme.tertiaryContainer
                BookingStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer
                BookingStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    )
}

private fun guestLabel(count: Int): String {
    return when {
        count % 10 == 1 && count % 100 != 11 -> "гость"
        count % 10 in 2..4 && (count % 100 !in 12..14) -> "гостя"
        else -> "гостей"
    }
}
