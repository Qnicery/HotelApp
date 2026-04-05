package com.example.testapp.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.testapp.ui.viewmodel.AdminHotelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHotelDashboardScreen(
    onNavigateToEditHotel: (Int) -> Unit,
    onNavigateToRooms: (Int) -> Unit,
    onNavigateToBookings: () -> Unit,
    onNavigateToReviews: () -> Unit,
    onLogout: () -> Unit,
    viewModel: AdminHotelViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Админ-панель отеля") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Выйти",
                            tint = MaterialTheme.colorScheme.onPrimary
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Статистика
                item {
                    Text(
                        text = "Управление",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item {
                    AdminMenuCard(
                        title = "Мои отели",
                        icon = Icons.Default.Hotel,
                        onClick = { /* Переход к списку отелей */ }
                    )
                }

                item {
                    AdminMenuCard(
                        title = "Номера",
                        icon = Icons.Default.Bed,
                        onClick = {
                            if (uiState.hotels.isNotEmpty()) {
                                onNavigateToRooms(uiState.hotels.first().id)
                            }
                        }
                    )
                }

                item {
                    AdminMenuCard(
                        title = "Бронирования",
                        icon = Icons.Default.BookOnline,
                        onClick = onNavigateToBookings
                    )
                }

                item {
                    AdminMenuCard(
                        title = "Отзывы",
                        icon = Icons.Default.Reviews,
                        onClick = onNavigateToReviews
                    )
                }

                // Список отелей
                if (uiState.hotels.isNotEmpty()) {
                    item {
                        Text(
                            text = "Ваши отели",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    items(uiState.hotels) { hotel ->
                        HotelAdminCard(
                            hotel = hotel,
                            onClick = { onNavigateToEditHotel(hotel.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminMenuCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun HotelAdminCard(
    hotel: com.example.testapp.data.model.Hotel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = hotel.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = hotel.city,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Рейтинг: ${String.format("%.1f", hotel.rating)}",
                    fontSize = 14.sp
                )
                Text(
                    text = "Отзывов: ${hotel.reviewsCount}",
                    fontSize = 14.sp
                )
            }
        }
    }
}
