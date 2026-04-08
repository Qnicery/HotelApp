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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.testapp.data.model.HotelCard
import com.example.testapp.data.model.UserRole
import com.example.testapp.ui.viewmodel.AdminSystemViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSystemDashboardScreen(
    onLogout: () -> Unit,
    viewModel: AdminSystemViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Системный администратор") },
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
            TabbedContent(
                uiState = uiState,
                viewModel = viewModel,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun TabbedContent(
    uiState: com.example.testapp.ui.viewmodel.AdminSystemUiState,
    viewModel: AdminSystemViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Пользователи", "Заявки", "Отели", "Статистика")

    Column(modifier = modifier) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> UsersTab(uiState.users)
            1 -> AdminRequestsTab(uiState.adminRequests, uiState.users, viewModel)
            2 -> HotelsTab(uiState.hotels)
            3 -> StatisticsTab(uiState.users, uiState.hotels, uiState.adminRequests)
        }
    }
}

// ==================== Users Tab ====================

@Composable
private fun UsersTab(users: List<com.example.testapp.data.model.User>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (users.isEmpty()) {
            item {
                Text(
                    text = "Нет пользователей",
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            items(users) { user ->
                UserCard(user)
            }
        }
    }
}

@Composable
private fun UserCard(user: com.example.testapp.data.model.User) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = user.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = user.email,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            AssistChip(
                onClick = { },
                label = { Text(user.role.displayName) }
            )
        }
    }
}

// ==================== Admin Requests Tab ====================

@Composable
private fun AdminRequestsTab(
    requests: List<com.example.testapp.data.api.model.AdminRequestDTO>,
    users: List<com.example.testapp.data.model.User>,
    viewModel: AdminSystemViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (requests.isEmpty()) {
            item {
                Text(
                    text = "Нет заявок",
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            items(requests) { request ->
                val user = users.find { it.id == request.userId }
                AdminRequestCard(request, user, viewModel)
            }
        }
    }
}

@Composable
private fun AdminRequestCard(
    request: com.example.testapp.data.api.model.AdminRequestDTO,
    user: com.example.testapp.data.model.User?,
    viewModel: AdminSystemViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Заявка #${request.id}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            // Информация о пользователе
            if (user != null) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Пользователь: ${user.name}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Email: ${user.email}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "Статус: ${request.status}",
                fontSize = 14.sp,
                color = when (request.status) {
                    "Pending" -> MaterialTheme.colorScheme.primary
                    "Approved" -> MaterialTheme.colorScheme.primary
                    "Rejected" -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )

            Text(
                text = "Текст заявки:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = request.text,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (request.status == "Pending") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.approveAdminRequest(request.id, request.userId) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Принять")
                    }
                    OutlinedButton(
                        onClick = { viewModel.rejectAdminRequest(request.id) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Отклонить")
                    }
                }
            }
        }
    }
}

// ==================== Hotels Tab ====================

@Composable
private fun HotelsTab(hotels: List<com.example.testapp.data.model.Hotel>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (hotels.isEmpty()) {
            item {
                EmptyState("Нет отелей")
            }
        } else {
            items(hotels) { hotel ->
                HotelCard(
                    hotel = hotel,
                    onClick = { /* TODO: navigate to hotel details */ }
                )
            }
        }
    }
}

// ==================== Statistics Tab ====================

@Composable
private fun StatisticsTab(
    users: List<com.example.testapp.data.model.User>,
    hotels: List<com.example.testapp.data.model.Hotel>,
    adminRequests: List<com.example.testapp.data.api.model.AdminRequestDTO>
) {
    // Вычисляем статистику
    val totalUsers = users.size
    val usersByRole = users.groupBy { it.role }
    val totalHotels = hotels.size
    val totalRequests = adminRequests.size
    val pendingRequests = adminRequests.count { it.status == "Pending" }
    val approvedRequests = adminRequests.count { it.status == "Approved" }
    val rejectedRequests = adminRequests.count { it.status == "Rejected" }
    val avgHotelRating = if (hotels.isNotEmpty()) {
        hotels.map { it.rating }.average().toFloat()
    } else 0f

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Статистика системы",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Основные метрики
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem(
                    label = "Пользователи",
                    value = totalUsers.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Отели",
                    value = totalHotels.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Заявки",
                    value = totalRequests.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Средний рейтинг отелей
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Средний рейтинг отелей",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f", avgHotelRating),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Пользователи по ролям
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Пользователи по ролям",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    UserRoleStatItem(
                        role = UserRole.USER,
                        count = usersByRole[UserRole.USER]?.size ?: 0
                    )
                    UserRoleStatItem(
                        role = UserRole.HOTEL_ADMIN,
                        count = usersByRole[UserRole.HOTEL_ADMIN]?.size ?: 0
                    )
                    UserRoleStatItem(
                        role = UserRole.SYSTEM_ADMIN,
                        count = usersByRole[UserRole.SYSTEM_ADMIN]?.size ?: 0
                    )
                }
            }
        }

        // Заявки по статусам
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Заявки на администратора",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    RequestStatItem(
                        label = "Ожидает",
                        count = pendingRequests,
                        color = MaterialTheme.colorScheme.primary
                    )
                    RequestStatItem(
                        label = "Одобрено",
                        count = approvedRequests,
                        color = MaterialTheme.colorScheme.primary
                    )
                    RequestStatItem(
                        label = "Отклонено",
                        count = rejectedRequests,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                fontSize = 28.sp,
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
}

@Composable
private fun UserRoleStatItem(role: UserRole, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AssistChip(
            onClick = { },
            label = { Text(role.displayName) }
        )
        Text(
            text = count.toString(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun RequestStatItem(
    label: String,
    count: Int,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = count.toString(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 16.sp
        )
    }
}

