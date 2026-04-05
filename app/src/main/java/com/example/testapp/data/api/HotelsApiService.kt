package com.example.testapp.data.api

import com.example.testapp.data.api.model.HotelDTO
import com.example.testapp.data.api.model.RoomDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * API интерфейс для работы с отелями и комнатами
 * Base URL: http://localhost:8080
 * 
 * Документация: API_ROUTES.md - Hotels, Rooms sections
 */
interface HotelsApiService {

    /**
     * Получить все отели
     * GET /hotels
     *
     * Response: 200 OK - List<HotelDTO>
     */
    @GET("hotels")
    suspend fun getAllHotels(): Response<List<HotelDTO>>

    /**
     * Получить отель по ID
     * GET /hotels/{id}
     *
     * Response: 200 OK - HotelDTO
     */
    @GET("hotels/{id}")
    suspend fun getHotelById(
        @Path("id") hotelId: Int
    ): Response<HotelDTO>

    /**
     * Получить отели по городу
     * GET /hotels/city/{city}
     *
     * Response: 200 OK - List<HotelDTO>
     */
    @GET("hotels/city/{city}")
    suspend fun getHotelsByCity(
        @Path("city") city: String
    ): Response<List<HotelDTO>>

    /**
     * Получить комнаты по ID отеля
     * GET /rooms/hotel/{hotelId}
     *
     * Response: 200 OK - List<RoomDTO>
     */
    @GET("rooms/hotel/{hotelId}")
    suspend fun getRoomsByHotelId(
        @Path("hotelId") hotelId: Int
    ): Response<List<RoomDTO>>
}
