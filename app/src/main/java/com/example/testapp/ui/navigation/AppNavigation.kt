package com.example.testapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.testapp.data.model.UserRole
import com.example.testapp.ui.viewmodel.AuthViewModel

/**
 * Основная навигация приложения с поддержкой графов для разных ролей
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val uiState by authViewModel.uiState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val userRole = currentUser?.role

    // Всегда начинаем с Splash
    // На Splash проверяем авторизацию и перенаправляем
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // ==================== Auth Graph ====================
        composable(Screen.Splash.route) {
            // Автоматическая проверка авторизации при входе на Splash
            LaunchedEffect(Unit) {
                authViewModel.checkAuthStatus()
            }

            com.example.testapp.ui.screens.SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                currentUser = currentUser,
                isCheckingAuth = uiState.isCheckingAuth,
                onAutoLoginSuccess = { role ->
                    val startDestination = when (role) {
                        UserRole.HOTEL_ADMIN -> Screen.AdminHotelDashboard.route
                        UserRole.SYSTEM_ADMIN -> Screen.AdminSystemDashboard.route
                        else -> Screen.Home.route
                    }
                    navController.navigate(startDestination) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            com.example.testapp.ui.screens.LoginScreen(
                onLoginSuccess = { role ->
                    val startDestination = when (role) {
                        UserRole.HOTEL_ADMIN -> Screen.AdminHotelDashboard.route
                        UserRole.SYSTEM_ADMIN -> Screen.AdminSystemDashboard.route
                        else -> Screen.Home.route
                    }
                    navController.navigate(startDestination) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            com.example.testapp.ui.screens.RegisterScreen(
                onRegisterSuccess = { role ->
                    val startDestination = when (role) {
                        UserRole.HOTEL_ADMIN -> Screen.AdminHotelDashboard.route
                        UserRole.SYSTEM_ADMIN -> Screen.AdminSystemDashboard.route
                        else -> Screen.Home.route
                    }
                    navController.navigate(startDestination) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        // ==================== Main Graph (User) ====================
        composable(Screen.Home.route) {
            com.example.testapp.ui.screens.HomeScreen(
                onNavigateToSearch = {
                    navController.navigate(Screen.Search.route)
                },
                onNavigateToHotelDetails = { hotelId ->
                    navController.navigate(Screen.HotelDetails.createRoute(hotelId))
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onLogout = {
                    authViewModel.logout()
                }
            )
            
            // Навигация на Splash после завершения logout
            LaunchedEffect(uiState.isLoggedIn) {
                if (!uiState.isLoggedIn && !uiState.isLoggingOut) {
                    // Пользователь вышел, переходим на Splash
                    navController.navigate(Screen.Splash.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }

        composable(Screen.Search.route) {
            com.example.testapp.ui.screens.SearchScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToResults = { city, checkIn, checkOut, guests ->
                    val cityEncoded = city ?: ""
                    navController.navigate("${Screen.SearchResults.route}?city=${cityEncoded}&checkIn=${checkIn ?: ""}&checkOut=${checkOut ?: ""}&guests=${guests ?: ""}")
                }
            )
        }

        composable(
            route = "${Screen.SearchResults.route}?city={city}&checkIn={checkIn}&checkOut={checkOut}&guests={guests}",
            arguments = listOf(
                navArgument("city") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("checkIn") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("checkOut") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("guests") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val city = backStackEntry.arguments?.getString("city")?.takeIf { it.isNotBlank() }
            val checkIn = backStackEntry.arguments?.getString("checkIn")?.takeIf { it.isNotBlank() }
            val checkOut = backStackEntry.arguments?.getString("checkOut")?.takeIf { it.isNotBlank() }
            val guests = backStackEntry.arguments?.getString("guests")?.toIntOrNull()
            com.example.testapp.ui.screens.SearchResultsScreen(
                selectedCity = city,
                checkInDate = checkIn,
                checkOutDate = checkOut,
                guests = guests,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToHotelDetails = { hotelId, ci, co, g ->
                    navController.navigate("${Screen.HotelDetails.createRoute(hotelId)}?checkIn=${ci ?: ""}&checkOut=${co ?: ""}&guests=${g ?: ""}")
                }
            )
        }

        composable(
            route = "${Screen.HotelDetails.route}?checkIn={checkIn}&checkOut={checkOut}&guests={guests}",
            arguments = listOf(
                navArgument("hotelId") { type = NavType.IntType },
                navArgument("checkIn") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("checkOut") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("guests") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val hotelId = backStackEntry.arguments?.getInt("hotelId") ?: return@composable
            val checkIn = backStackEntry.arguments?.getString("checkIn")?.takeIf { it.isNotBlank() }
            val checkOut = backStackEntry.arguments?.getString("checkOut")?.takeIf { it.isNotBlank() }
            val guests = backStackEntry.arguments?.getString("guests")?.toIntOrNull()
            com.example.testapp.ui.screens.HotelDetailsScreen(
                hotelId = hotelId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToReviews = {
                    navController.navigate(Screen.Reviews.createRoute(hotelId))
                },
                onNavigateToRooms = {
                    navController.navigate("${Screen.RoomsList.createRoute(hotelId)}?checkIn=${checkIn ?: ""}&checkOut=${checkOut ?: ""}&guests=${guests ?: ""}")
                },
                onNavigateToBooking = { hotelId, roomId, ci, co, g ->
                    navController.navigate(Screen.Booking.createRoute(hotelId, roomId, ci ?: checkIn, co ?: checkOut, g ?: guests))
                }
            )
        }

        composable(
            route = "${Screen.RoomsList.route}?checkIn={checkIn}&checkOut={checkOut}&guests={guests}",
            arguments = listOf(
                navArgument("hotelId") { type = NavType.IntType },
                navArgument("checkIn") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("checkOut") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("guests") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val hotelId = backStackEntry.arguments?.getInt("hotelId") ?: return@composable
            val checkIn = backStackEntry.arguments?.getString("checkIn")?.takeIf { it.isNotBlank() }
            val checkOut = backStackEntry.arguments?.getString("checkOut")?.takeIf { it.isNotBlank() }
            val guests = backStackEntry.arguments?.getString("guests")?.toIntOrNull()
            com.example.testapp.ui.screens.RoomsListScreen(
                hotelId = hotelId,
                checkInDate = checkIn,
                checkOutDate = checkOut,
                guests = guests,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToBooking = { hId, roomId, ci, co, g ->
                    navController.navigate(Screen.Booking.createRoute(hId, roomId, ci ?: checkIn, co ?: checkOut, g ?: guests))
                }
            )
        }

        composable(
            route = Screen.Booking.route,
            arguments = listOf(
                navArgument("hotelId") { type = NavType.IntType },
                navArgument("roomId") { type = NavType.IntType },
                navArgument("checkIn") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("checkOut") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("guests") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val hotelId = backStackEntry.arguments?.getInt("hotelId") ?: return@composable
            val roomId = backStackEntry.arguments?.getInt("roomId") ?: return@composable
            val checkIn = backStackEntry.arguments?.getString("checkIn")?.takeIf { it.isNotBlank() }
            val checkOut = backStackEntry.arguments?.getString("checkOut")?.takeIf { it.isNotBlank() }
            val guests = backStackEntry.arguments?.getString("guests")?.toIntOrNull()
            com.example.testapp.ui.screens.BookingScreen(
                hotelId = hotelId,
                roomId = roomId,
                checkInDate = checkIn,
                checkOutDate = checkOut,
                guests = guests,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onBookingSuccess = {
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                }
            )
        }

        composable(Screen.Profile.route) {
            com.example.testapp.ui.screens.ProfileScreen(
                onNavigateToBookingHistory = {
                    navController.navigate(Screen.BookingHistory.route)
                },
                onNavigateToAdminRequest = {
                    navController.navigate(Screen.AdminRequest.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToLogin = {
                    authViewModel.logout()
                }
            )
            
            // Навигация на Splash после завершения logout
            LaunchedEffect(uiState.isLoggedIn) {
                if (!uiState.isLoggedIn && !uiState.isLoggingOut) {
                    navController.navigate(Screen.Splash.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }

        composable(Screen.BookingHistory.route) {
            com.example.testapp.ui.screens.BookingHistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCancel = { bookingId ->
                    // Логика отмены бронирования
                },
                onNavigateToReview = { hotelId ->
                    navController.navigate(Screen.Reviews.createRoute(hotelId))
                }
            )
        }

        composable(Screen.AdminRequest.route) {
            val currentUserId = authViewModel.currentUser.value?.id ?: 0
            com.example.testapp.ui.screens.AdminRequestScreen(
                userId = currentUserId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Reviews.route,
            arguments = listOf(
                navArgument("hotelId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val hotelId = backStackEntry.arguments?.getInt("hotelId") ?: return@composable
            com.example.testapp.ui.screens.ReviewsScreen(
                hotelId = hotelId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Notifications.route) {
            com.example.testapp.ui.screens.NotificationsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Settings.route) {
            com.example.testapp.ui.screens.SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ==================== Admin Hotel Graph ====================
        composable(Screen.AdminHotelDashboard.route) {
            com.example.testapp.ui.screens.admin.AdminHotelDashboardScreen(
                onNavigateToEditHotel = { hotelId ->
                    navController.navigate(Screen.AdminHotelEdit.createRoute(hotelId))
                },
                onNavigateToRooms = { hotelId ->
                    navController.navigate(Screen.AdminRoomsList.createRoute(hotelId))
                },
                onNavigateToBookings = {
                    navController.navigate(Screen.AdminHotelBookings.route)
                },
                onNavigateToReviews = {
                    navController.navigate(Screen.AdminHotelReviews.route)
                },
                onLogout = {
                    authViewModel.logout()
                }
            )
            
            // Навигация на Splash после завершения logout
            LaunchedEffect(uiState.isLoggedIn) {
                if (!uiState.isLoggedIn && !uiState.isLoggingOut) {
                    navController.navigate(Screen.Splash.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }

        composable(Screen.AdminHotelBookings.route) {
            com.example.testapp.ui.screens.admin.AdminHotelBookingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.AdminHotelReviews.route) {
            com.example.testapp.ui.screens.admin.AdminHotelReviewsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ==================== Admin System Graph ====================
        composable(Screen.AdminSystemDashboard.route) {
            com.example.testapp.ui.screens.admin.AdminSystemDashboardScreen(
                onLogout = {
                    authViewModel.logout()
                }
            )
            
            // Навигация на Splash после завершения logout
            LaunchedEffect(uiState.isLoggedIn) {
                if (!uiState.isLoggedIn && !uiState.isLoggingOut) {
                    navController.navigate(Screen.Splash.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }

        composable(Screen.AdminUsersList.route) {
            com.example.testapp.ui.screens.admin.AdminUsersListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEditUser = { userId ->
                    navController.navigate(Screen.AdminUserEdit.createRoute(userId))
                }
            )
        }

        composable(Screen.AdminPendingRequests.route) {
            com.example.testapp.ui.screens.admin.AdminPendingRequestsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

/**
 * Определяет стартовый экран на основе роли пользователя
 */
@Composable
private fun determineStartDestination(role: com.example.testapp.data.model.UserRole?): String {
    return when (role) {
        com.example.testapp.data.model.UserRole.HOTEL_ADMIN -> Screen.AdminHotelDashboard.route
        com.example.testapp.data.model.UserRole.SYSTEM_ADMIN -> Screen.AdminSystemDashboard.route
        com.example.testapp.data.model.UserRole.USER -> Screen.Home.route
        null -> Screen.Splash.route
    }
}
