package com.example.testapp.data.model

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Карточка со статистикой пользователя
 */
@Composable
fun UserStatsCard(
    bookings: List<com.example.testapp.data.model.Booking>,
    modifier: Modifier = Modifier
) {
    val totalBookings = bookings.size
    val activeBookings = bookings.count {
        it.status == BookingStatus.CONFIRMED || it.status == BookingStatus.ACTIVE
    }
    val completedBookings = bookings.count { it.status == BookingStatus.COMPLETED }

    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Статистика",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem(
                    label = "Всего",
                    value = totalBookings.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Активных",
                    value = activeBookings.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Завершённых",
                    value = completedBookings.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Элемент статистики
 */
@Composable
fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}