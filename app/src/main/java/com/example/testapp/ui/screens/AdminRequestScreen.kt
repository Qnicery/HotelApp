package com.example.testapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.testapp.data.api.model.AdminRequestDTO
import com.example.testapp.ui.viewmodel.AdminRequestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRequestScreen(
    userId: Int,
    onNavigateBack: () -> Unit,
    viewModel: AdminRequestViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Загружаем заявки пользователя при первом запуске
    LaunchedEffect(userId) {
        viewModel.loadUserRequests(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Заявка на администратора") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Если заявок нет - показываем форму создания
                if (uiState.userRequests.isEmpty()) {
                    if (uiState.isSubmitted) {
                        // Показываем сообщение об успешной отправке
                        SuccessMessage(onNavigateBack = onNavigateBack)
                    } else {
                        // Показываем форму создания заявки
                        AdminRequestForm(
                            userId = userId,
                            onSubmit = { text ->
                                viewModel.submitAdminRequest(userId, text)
                            },
                            isSubmitting = uiState.isSubmitting
                        )
                    }
                } else {
                    // Показываем список заявок пользователя
                    UserRequestsList(requests = uiState.userRequests)
                }

                // Показываем ошибку, если есть
                uiState.error?.let { error ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminRequestForm(
    userId: Int,
    onSubmit: (String) -> Unit,
    isSubmitting: Boolean
) {
    var text by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Подать заявку на получение прав администратора",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Опишите причину, по которой вы хотите стать администратором:",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                placeholder = { Text("Введите текст заявки...") },
                maxLines = 5,
                enabled = !isSubmitting
            )

            Button(
                onClick = {
                    if (text.isNotBlank()) {
                        onSubmit(text)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = text.isNotBlank() && !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Отправка...")
                } else {
                    Text("Отправить заявку")
                }
            }
        }
    }
}

@Composable
private fun SuccessMessage(onNavigateBack: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )

            Text(
                text = "Заявка успешно отправлена!",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Ваша заявка на получение прав администратора успешно отправлена. Ожидайте рассмотрения системой администратором.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Вернуться в профиль")
            }
        }
    }
}

@Composable
private fun UserRequestsList(requests: List<AdminRequestDTO>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Ваши заявки",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(requests) { request ->
                AdminRequestCard(request)
            }
        }
    }
}

@Composable
private fun AdminRequestCard(request: AdminRequestDTO) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Заявка",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                // Статус с цветной меткой
                val (statusColor, statusText) = when (request.status) {
                    "Pending" -> MaterialTheme.colorScheme.primary to "На рассмотрении"
                    "Approved" -> MaterialTheme.colorScheme.primary to "Одобрено"
                    "Rejected" -> MaterialTheme.colorScheme.error to "Отклонено"
                    else -> MaterialTheme.colorScheme.onSurface to request.status
                }

                AssistChip(
                    onClick = { },
                    label = { Text(statusText) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = statusColor.copy(alpha = 0.1f),
                        labelColor = statusColor,
                        leadingIconContentColor = statusColor
                    )
                )
            }

            Text(
                text = request.text,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Дата создания: ${request.createdAt}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
