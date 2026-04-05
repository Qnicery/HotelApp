package com.example.testapp.ui.navigation

import androidx.compose.runtime.Composable
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
    val currentUser by authViewModel.currentUser.collectAsState()
    val userRole = currentUser?.role

    NavHost(
        navController = navController,
        startDestination = determineStartDestination(userRole)
    ) {
        // ==================== Auth Graph ====================
        composable(Screen.Splash.route) {
            com.example.testapp.ui.screens.SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
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
                    navController.navigate(Screen.Splash.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Search.route) {
            com.example.testapp.ui.screens.SearchScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToResults = {
                    navController.navigate(Screen.SearchResults.route)
                }
            )
        }

        composable(Screen.SearchResults.route) {
            com.example.testapp.ui.screens.SearchResultsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToHotelDetails = { hotelId ->
                    navController.navigate(Screen.HotelDetails.createRoute(hotelId))
                }
            )
        }

        composable(
            route = Screen.HotelDetails.route,
            arguments = listOf(
                navArgument("hotelId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val hotelId = backStackEntry.arguments?.getInt("hotelId") ?: return@composable
            com.example.testapp.ui.screens.HotelDetailsScreen(
                hotelId = hotelId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToReviews = {
                    navController.navigate(Screen.Reviews.createRoute(hotelId))
                },
                onNavigateToRooms = {
                    navController.navigate(Screen.RoomsList.createRoute(hotelId))
                },
                onNavigateToBooking = { roomId ->
                    navController.navigate(Screen.Booking.createRoute(roomId))
                }
            )
        }

        composable(
            route = Screen.RoomsList.route,
            arguments = listOf(
                navArgument("hotelId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val hotelId = backStackEntry.arguments?.getInt("hotelId") ?: return@composable
            com.example.testapp.ui.screens.RoomsListScreen(
                hotelId = hotelId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToBooking = { roomId ->
                    navController.navigate(Screen.Booking.createRoute(roomId))
                }
            )
        }

        composable(
            route = Screen.Booking.route,
            arguments = listOf(
                navArgument("roomId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getInt("roomId") ?: return@composable
            com.example.testapp.ui.screens.BookingScreen(
                roomId = roomId,
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
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToNotifications = {
                    navController.navigate(Screen.Notifications.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToLogin = {
                    authViewModel.logout()
                    navController.navigate(Screen.Splash.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
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
                    navController.navigate(Screen.Splash.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
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
                onNavigateToUsersList = {
                    navController.navigate(Screen.AdminUsersList.route)
                },
                onNavigateToPendingRequests = {
                    navController.navigate(Screen.AdminPendingRequests.route)
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Splash.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
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
