package com.example.testapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var darkThemeEnabled by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Уведомления
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Уведомления",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    SwitchSetting(
                        title = "Push-уведомления",
                        subtitle = "Получать уведомления о бронированиях",
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it },
                        icon = Icons.Default.Notifications
                    )

                    HorizontalDivider()

                    SwitchSetting(
                        title = "Email-уведомления",
                        subtitle = "Получать письма на почту",
                        checked = true,
                        onCheckedChange = { },
                        icon = Icons.Default.Email
                    )
                }
            }

            // Тема
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Внешний вид",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    SwitchSetting(
                        title = "Тёмная тема",
                        subtitle = "Использовать тёмную тему оформления",
                        checked = darkThemeEnabled,
                        onCheckedChange = { darkThemeEnabled = it },
                        icon = Icons.Default.DarkMode
                    )
                }
            }

            // Безопасность
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Безопасность",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    SettingItem(
                        title = "Сменить пароль",
                        subtitle = "Изменить пароль от аккаунта",
                        icon = Icons.Default.Lock,
                        onClick = { }
                    )

                    HorizontalDivider()

                    SettingItem(
                        title = "Двухфакторная аутентификация",
                        subtitle = "Дополнительная защита аккаунта",
                        icon = Icons.Default.Security,
                        onClick = { }
                    )
                }
            }

            // О приложении
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "О приложении",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    SettingItem(
                        title = "Версия",
                        subtitle = "1.0.0",
                        icon = Icons.Default.Info,
                        onClick = { }
                    )

                    HorizontalDivider()

                    SettingItem(
                        title = "Пользовательское соглашение",
                        subtitle = "Правила использования приложения",
                        icon = Icons.Default.Description,
                        onClick = { }
                    )

                    HorizontalDivider()

                    SettingItem(
                        title = "Политика конфиденциальности",
                        subtitle = "Как мы обрабатываем данные",
                        icon = Icons.Default.PrivacyTip,
                        onClick = { }
                    )
                }
            }

            // Выйти
            Button(
                onClick = { /* Выход */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Выйти из аккаунта")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SwitchSetting(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
