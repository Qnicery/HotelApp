package com.example.testapp.data.api

import com.example.testapp.data.api.model.AmenityDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * API интерфейс для работы с удобствами (amenities)
 * Base URL: http://localhost:8080
 *
 * Документация: API_ROUTES.md - Amenities, Room Amenities sections
 */
interface AmenitiesApiService {

    /**
     * Получить все удобства
     * GET /amenities
     *
     * Response: 200 OK - List<AmenityDTO>
     */
    @GET("amenities")
    suspend fun getAllAmenities(): Response<List<AmenityDTO>>

    /**
     * Получить все удобства отеля (уникальные из всех комнат)
     * GET /room-amenities/hotel/{hotelId}
     *
     * Response: 200 OK - List<AmenityDTO>
     */
    @GET("room-amenities/hotel/{hotelId}")
    suspend fun getAmenitiesByHotelId(
        @Path("hotelId") hotelId: Int
    ): Response<List<AmenityDTO>>

    /**
     * Получить удобства комнаты
     * GET /room-amenities/room/{roomId}
     *
     * Response: 200 OK - List<AmenityDTO>
     */
    @GET("room-amenities/room/{roomId}")
    suspend fun getAmenitiesByRoomId(
        @Path("roomId") roomId: Int
    ): Response<List<AmenityDTO>>
}
