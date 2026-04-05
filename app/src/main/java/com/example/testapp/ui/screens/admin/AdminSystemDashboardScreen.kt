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
import com.example.testapp.ui.viewmodel.AdminSystemViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSystemDashboardScreen(
    onNavigateToUsersList: () -> Unit,
    onNavigateToPendingRequests: () -> Unit,
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Управление системой",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                AdminMenuCard(
                    title = "Пользователи",
                    icon = Icons.Default.Person,
                    onClick = onNavigateToUsersList
                )
            }

            item {
                AdminMenuCard(
                    title = "Заявки на администратора",
                    icon = Icons.Default.AdminPanelSettings,
                    onClick = onNavigateToPendingRequests
                )
            }

            item {
                AdminMenuCard(
                    title = "Все отели",
                    icon = Icons.Default.Hotel,
                    onClick = { }
                )
            }

            item {
                AdminMenuCard(
                    title = "Статистика",
                    icon = Icons.Default.BarChart,
                    onClick = { }
                )
            }
        }
    }
}

