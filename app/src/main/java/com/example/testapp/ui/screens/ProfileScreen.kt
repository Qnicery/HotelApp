package com.example.testapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.testapp.data.model.Notification
import com.example.testapp.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToBookingHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val user = uiState.user

    // Проверка авторизации при загрузке
    LaunchedEffect(user) {
        if (user == null && !uiState.isLoading) {
            // Пользователь не авторизован - редирект на экран входа
            onNavigateToLogin()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
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
            val user = uiState.user
            if (user != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Аватар и имя
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Avatar",
                                    modifier = Modifier.size(56.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = user.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = user.email,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        AssistChip(
                            onClick = { },
                            label = { Text(user.role.displayName) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    }

                    // Меню профиля
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            // История бронирований
                            ListItem(
                                headlineContent = { Text("История бронирований") },
                                supportingContent = { Text("Ваши текущие и прошлые брони") },
                                leadingContent = {
                                    Icon(
                                        imageVector = Icons.Default.BookOnline,
                                        contentDescription = null
                                    )
                                },
                                trailingContent = {
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier
                                    .clickable { onNavigateToBookingHistory() }
                            )

                            HorizontalDivider()

                            // Уведомления
                            ListItem(
                                headlineContent = { 
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Уведомления")
                                        val unreadCount = uiState.notifications.count { !it.isRead }
                                        if (unreadCount > 0) {
                                            Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.error
                                            ) {
                                                Text(
                                                    text = unreadCount.toString(),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onError
                                                )
                                            }
                                        }
                                    }
                                },
                                supportingContent = { Text("Сообщения о бронированиях") },
                                leadingContent = {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = null
                                    )
                                },
                                trailingContent = {
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier
                                    .clickable { onNavigateToNotifications() }
                            )

                            HorizontalDivider()

                            // Настройки
                            ListItem(
                                headlineContent = { Text("Настройки") },
                                supportingContent = { Text("Пароль, уведомления, язык") },
                                leadingContent = {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = null
                                    )
                                },
                                trailingContent = {
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier
                                    .clickable { onNavigateToSettings() }
                            )
                        }
                    }

                    // Последние уведомления
                    if (uiState.notifications.isNotEmpty()) {
                        Text(
                            text = "Последние уведомления",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        uiState.notifications.take(3).forEach { notification ->
                            NotificationItem(
                                notification = notification,
                                onMarkAsRead = {
                                    viewModel.markNotificationAsRead(notification.id)
                                }
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Пользователь не найден")
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: Notification,
    onMarkAsRead: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.primaryContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = notification.title,
                    fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = notification.message,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = notification.createdAt,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!notification.isRead) {
                IconButton(onClick = onMarkAsRead) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = "Отметить как прочитанное"
                    )
                }
            }
        }
    }
}
