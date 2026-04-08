package com.example.testapp.data.model

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.BookOnline
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Карточка с меню профиля
 */
@Composable
fun ProfileMenuCard(
    onNavigateToBookingHistory: () -> Unit,
    onNavigateToAdminRequest: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
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

            // Заявка на администратора
            ListItem(
                headlineContent = { Text("Заявка на администратора") },
                supportingContent = { Text("Подать заявку на получение прав администратора") },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
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
                    .clickable { onNavigateToAdminRequest() }
            )

            HorizontalDivider()

            // Настройки
            ListItem(
                headlineContent = { Text("Настройки") },
                supportingContent = { Text("Пароль, язык, тема") },
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
}