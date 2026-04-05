package com.example.testapp.ui.navigation

import com.example.testapp.data.model.UserRole

/**
 * Экраны приложения с поддержкой ролей
 */
sealed class Screen(val route: String) {
    // ==================== Auth Graph ====================
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")

    // ==================== Main Graph (User) ====================
    object Home : Screen("home")
    object Search : Screen("search")
    object SearchResults : Screen("search_results")
    object HotelDetails : Screen("hotel_details/{hotelId}") {
        fun createRoute(hotelId: Int) = "hotel_details/$hotelId"
    }
    object RoomsList : Screen("rooms_list/{hotelId}") {
        fun createRoute(hotelId: Int) = "rooms_list/$hotelId"
    }
    object Booking : Screen("booking/{roomId}") {
        fun createRoute(roomId: Int) = "booking/$roomId"
    }
    object Profile : Screen("profile")
    object BookingHistory : Screen("booking_history")
    object Reviews : Screen("reviews/{hotelId}") {
        fun createRoute(hotelId: Int) = "reviews/$hotelId"
    }
    object Notifications : Screen("notifications")
    object Settings : Screen("settings")

    // ==================== Admin Hotel Graph ====================
    object AdminHotelDashboard : Screen("admin_hotel_dashboard")
    object AdminHotelEdit : Screen("admin_hotel_edit/{hotelId}") {
        fun createRoute(hotelId: Int) = "admin_hotel_edit/$hotelId"
    }
    object AdminRoomsList : Screen("admin_rooms_list/{hotelId}") {
        fun createRoute(hotelId: Int) = "admin_rooms_list/$hotelId"
    }
    object AdminRoomEdit : Screen("admin_room_edit/{roomId}") {
        fun createRoute(roomId: Int) = "admin_room_edit/$roomId"
    }
    object AdminHotelBookings : Screen("admin_hotel_bookings")
    object AdminHotelReviews : Screen("admin_hotel_reviews")

    // ==================== Admin System Graph ====================
    object AdminSystemDashboard : Screen("admin_system_dashboard")
    object AdminUsersList : Screen("admin_users_list")
    object AdminUserEdit : Screen("admin_user_edit/{userId}") {
        fun createRoute(userId: Int) = "admin_user_edit/$userId"
    }
    object AdminPendingRequests : Screen("admin_pending_requests")

    companion object {
        /**
         * Определяет стартовый экран на основе роли пользователя
         */
        fun getStartDestination(role: UserRole?): String {
            return when (role) {
                UserRole.HOTEL_ADMIN -> AdminHotelDashboard.route
                UserRole.SYSTEM_ADMIN -> AdminSystemDashboard.route
                UserRole.USER -> Home.route
                null -> Splash.route
            }
        }
    }
}

/**
 * Навигационные графы
 */
sealed class NavGraph(val startDestination: String) {
    object Auth : NavGraph("auth") {
        val route = "auth_graph"
        val screens = listOf(Screen.Splash, Screen.Login, Screen.Register)
    }

    object Main : NavGraph(Screen.Home.route) {
        val route = "main_graph"
        val screens = listOf(
            Screen.Home,
            Screen.Search,
            Screen.HotelDetails,
            Screen.RoomsList,
            Screen.Booking,
            Screen.Profile,
            Screen.BookingHistory,
            Screen.Reviews,
            Screen.Notifications,
            Screen.Settings
        )
    }

    object AdminHotel : NavGraph(Screen.AdminHotelDashboard.route) {
        val route = "admin_hotel_graph"
        val screens = listOf(
            Screen.AdminHotelDashboard,
            Screen.AdminHotelEdit,
            Screen.AdminRoomsList,
            Screen.AdminRoomEdit,
            Screen.AdminHotelBookings,
            Screen.AdminHotelReviews
        )
    }

    object AdminSystem : NavGraph(Screen.AdminSystemDashboard.route) {
        val route = "admin_system_graph"
        val screens = listOf(
            Screen.AdminSystemDashboard,
            Screen.AdminUsersList,
            Screen.AdminUserEdit,
            Screen.AdminPendingRequests
        )
    }
}
