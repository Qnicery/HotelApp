# HotelAppBack API Routes Documentation

Complete documentation of all API endpoints for the HotelAppBack application.

Base URL: `http://localhost:8080`

---

## Table of Contents

1. [General](#general)
2. [Authentication](#authentication)
3. [Hotels](#hotels)
4. [Rooms](#rooms)
5. [Amenities](#amenities)
6. [Room Amenities](#room-amenities)
7. [Bookings](#bookings)
8. [Reviews](#reviews)
9. [Admin Requests](#admin-requests)

---

## General

### Health Check

```
GET /
```

**Response:** `200 OK`
```
HotelApp API - Running
```

---

## Authentication

### Register User

```
POST /register
```

**Request Body:** `UserCreateRequest`
```json
{
  "email": "string",
  "name": "string",
  "password": "string (min 8 characters)"
}
```

**Responses:**
- `201 Created` - `UserResponse`
- `400 Bad Request` - Validation error
- `500 Internal Server Error` - Server error

### Login

```
POST /login
```

**Request Body:** `UserLoginRequest`
```json
{
  "email": "string",
  "password": "string"
}
```

**Responses:**
- `200 OK` - `UserLoginResponse` with token
- `400 Bad Request` - Missing fields
- `401 Unauthorized` - Invalid credentials
- `500 Internal Server Error` - Server error

### Logout

```
POST /logout
```

**Headers:**
```
Authorization: Bearer <token>
```

**Responses:**
- `200 OK` - Success message
- `400 Bad Request` - Missing token
- `404 Not Found` - Session not found
- `500 Internal Server Error` - Server error

### Get Current User

```
GET /me
```

**Headers:**
```
Authorization: Bearer <token>
```

**Responses:**
- `200 OK` - `UserResponse`
- `401 Unauthorized` - Invalid/expired token
- `404 Not Found` - User not found
- `500 Internal Server Error` - Server error

### Update User Role

```
PUT /users/{id}/role
```

**Path Parameters:**
- `id` (Int) - User ID

**Request Body:** `UserRoleUpdateRequest`
```json
{
  "role": "string (e.g., Admin, User, Moderator)"
}
```

**Responses:**
- `200 OK` - `UserResponse` with updated role
- `400 Bad Request` - Invalid user ID or empty role
- `404 Not Found` - User not found
- `500 Internal Server Error` - Server error

**Example Request:**
```json
PUT /users/1/role
{
  "role": "Admin"
}
```

**Example Response:**
```json
{
  "id": 1,
  "email": "user@example.com",
  "name": "John Doe",
  "role": "Admin"
}
```

---

## Hotels

### Get All Hotels

```
GET /hotels
```

**Responses:**
- `200 OK` - `List<HotelDTO>`
- `500 Internal Server Error` - Server error

### Get Hotel by ID

```
GET /hotels/{id}
```

**Path Parameters:**
- `id` (Int) - Hotel ID

**Responses:**
- `200 OK` - `HotelDTO`
- `400 Bad Request` - Invalid ID
- `404 Not Found` - Hotel not found
- `500 Internal Server Error` - Server error

### Get Hotels by Admin ID

```
GET /hotels/admin/{adminId}
```

**Path Parameters:**
- `adminId` (Int) - Admin user ID

**Responses:**
- `200 OK` - `List<HotelDTO>`
- `400 Bad Request` - Invalid admin ID
- `500 Internal Server Error` - Server error

### Get Hotels by City

```
GET /hotels/city/{city}
```

**Path Parameters:**
- `city` (String) - City name

**Responses:**
- `200 OK` - `List<HotelDTO>`
- `400 Bad Request` - Missing city parameter
- `500 Internal Server Error` - Server error

### Create Hotel

```
POST /hotels
```

**Request Body:** `HotelCreateRequest`
```json
{
  "adminId": 1,
  "name": "string",
  "city": "string",
  "description": "string or null",
  "address": "string",
  "photoUrls": ["https://example.com/photo1.jpg", "https://example.com/photo2.jpg"]
}
```

**Responses:**
- `201 Created` - `HotelDTO`
- `400 Bad Request` - Validation error
- `500 Internal Server Error` - Server error

### Update Hotel

```
PUT /hotels/{id}
```

**Path Parameters:**
- `id` (Int) - Hotel ID

**Request Body:** `HotelUpdateRequest` (all fields optional)
```json
{
  "name": "string or null",
  "city": "string or null",
  "description": "string or null",
  "address": "string or null",
  "photoUrls": ["https://example.com/new-photo1.jpg"]
}
```

**Note:** This endpoint **replaces** the entire photoUrls array. To add/remove individual photos, use the photo management endpoints below.

**Responses:**
- `200 OK` - `HotelDTO`
- `400 Bad Request` - Invalid ID
- `404 Not Found` - Hotel not found
- `500 Internal Server Error` - Server error

### Add Photo to Hotel

```
POST /hotels/{id}/photos
```

**Path Parameters:**
- `id` (Int) - Hotel ID

**Request Body:** `PhotoUrlRequest`
```json
{
  "url": "https://example.com/new-photo.jpg"
}
```

**Note:** This endpoint **appends** the URL to the existing photoUrls array.

**Responses:**
- `200 OK` - `HotelDTO` with updated photoUrls
- `400 Bad Request` - Invalid ID or empty URL
- `404 Not Found` - Hotel not found
- `500 Internal Server Error` - Server error

**Example:**
```json
// Before: ["photo1.jpg"]
// Request: POST /hotels/1/photos {"url": "photo2.jpg"}
// After: ["photo1.jpg", "photo2.jpg"]
```

### Remove Photo from Hotel

```
DELETE /hotels/{id}/photos
```

**Path Parameters:**
- `id` (Int) - Hotel ID

**Request Body:** `PhotoUrlRequest`
```json
{
  "url": "https://example.com/photo-to-remove.jpg"
}
```

**Note:** This endpoint **removes** the specified URL from the photoUrls array.

**Responses:**
- `200 OK` - `HotelDTO` with updated photoUrls
- `400 Bad Request` - Invalid ID or empty URL
- `404 Not Found` - Hotel not found
- `500 Internal Server Error` - Server error

**Example:**
```json
// Before: ["photo1.jpg", "photo2.jpg", "photo3.jpg"]
// Request: DELETE /hotels/1/photos {"url": "photo2.jpg"}
// After: ["photo1.jpg", "photo3.jpg"]
```

### Delete Hotel

```
DELETE /hotels/{id}
```

**Path Parameters:**
- `id` (Int) - Hotel ID

**Responses:**
- `200 OK` - Success message
- `400 Bad Request` - Invalid ID
- `404 Not Found` - Hotel not found
- `500 Internal Server Error` - Server error

---

## Rooms

### Get Room by ID

```
GET /rooms/{id}
```

**Path Parameters:**
- `id` (Int) - Room ID

**Responses:**
- `200 OK` - `RoomDTO`
- `400 Bad Request` - Invalid ID
- `404 Not Found` - Room not found
- `500 Internal Server Error` - Server error

### Get Rooms by Hotel ID

```
GET /rooms/hotel/{hotelId}
```

**Path Parameters:**
- `hotelId` (Int) - Hotel ID

**Responses:**
- `200 OK` - `List<RoomDTO>`
- `400 Bad Request` - Invalid hotel ID
- `500 Internal Server Error` - Server error

### Create Room

```
POST /rooms
```

**Request Body:** `RoomCreateRequest`
```json
{
  "hotelId": 1,
  "roomName": "string",
  "description": "string or null",
  "price": 0.0,
  "maxGuests": 2
}
```

**Responses:**
- `201 Created` - `RoomDTO`
- `400 Bad Request` - Validation error
- `500 Internal Server Error` - Server error

### Update Room

```
PUT /rooms/{id}
```

**Path Parameters:**
- `id` (Int) - Room ID

**Request Body:** `RoomUpdateRequest` (all fields optional)
```json
{
  "roomName": "string or null",
  "description": "string or null",
  "price": 0.0 or null,
  "maxGuests": 2 or null,
  "status": "string or null"
}
```

**Responses:**
- `200 OK` - `RoomDTO`
- `400 Bad Request` - Invalid ID
- `404 Not Found` - Room not found
- `500 Internal Server Error` - Server error

### Add Photo to Room

```
POST /rooms/{id}/photos
```

**Path Parameters:**
- `id` (Int) - Room ID

**Request Body:** `PhotoUrlRequest`
```json
{
  "url": "https://example.com/new-photo.jpg"
}
```

**Note:** This endpoint **appends** the URL to the existing photoUrls array.

**Responses:**
- `200 OK` - `RoomDTO` with updated photoUrls
- `400 Bad Request` - Invalid ID or empty URL
- `404 Not Found` - Room not found
- `500 Internal Server Error` - Server error

**Example:**
```json
// Before: ["photo1.jpg"]
// Request: POST /rooms/1/photos {"url": "photo2.jpg"}
// After: ["photo1.jpg", "photo2.jpg"]
```

### Remove Photo from Room

```
DELETE /rooms/{id}/photos
```

**Path Parameters:**
- `id` (Int) - Room ID

**Request Body:** `PhotoUrlRequest`
```json
{
  "url": "https://example.com/photo-to-remove.jpg"
}
```

**Note:** This endpoint **removes** the specified URL from the photoUrls array.

**Responses:**
- `200 OK` - `RoomDTO` with updated photoUrls
- `400 Bad Request` - Invalid ID or empty URL
- `404 Not Found` - Room not found
- `500 Internal Server Error` - Server error

**Example:**
```json
// Before: ["photo1.jpg", "photo2.jpg", "photo3.jpg"]
// Request: DELETE /rooms/1/photos {"url": "photo2.jpg"}
// After: ["photo1.jpg", "photo3.jpg"]
```

### Delete Room

```
DELETE /rooms/{id}
```

**Path Parameters:**
- `id` (Int) - Room ID

**Responses:**
- `200 OK` - Success message
- `400 Bad Request` - Invalid ID
- `404 Not Found` - Room not found
- `500 Internal Server Error` - Server error

---

## Amenities

### Get All Amenities

```
GET /amenities
```

**Responses:**
- `200 OK` - `List<AmenityDTO>`
- `500 Internal Server Error` - Server error

### Get Amenity by ID

```
GET /amenities/{id}
```

**Path Parameters:**
- `id` (Int) - Amenity ID

**Responses:**
- `200 OK` - `AmenityDTO`
- `400 Bad Request` - Invalid ID
- `404 Not Found` - Amenity not found
- `500 Internal Server Error` - Server error

### Create Amenity

```
POST /amenities
```

**Request Body:** `AmenityCreateRequest`
```json
{
  "name": "string"
}
```

**Responses:**
- `201 Created` - `AmenityDTO`
- `400 Bad Request` - Validation error
- `500 Internal Server Error` - Server error

### Delete Amenity

```
DELETE /amenities/{id}
```

**Path Parameters:**
- `id` (Int) - Amenity ID

**Responses:**
- `200 OK` - Success message
- `400 Bad Request` - Invalid ID
- `404 Not Found` - Amenity not found
- `500 Internal Server Error` - Server error

---

## Room Amenities

### Get Amenities by Room ID

```
GET /room-amenities/room/{roomId}
```

**Path Parameters:**
- `roomId` (Int) - Room ID

**Responses:**
- `200 OK` - `List<AmenityDTO>`
- `400 Bad Request` - Invalid room ID
- `500 Internal Server Error` - Server error

### Assign Amenity to Room

```
POST /room-amenities/assign/{roomId}/{amenityId}
```

**Path Parameters:**
- `roomId` (Int) - Room ID
- `amenityId` (Int) - Amenity ID

**Responses:**
- `200 OK` - Success message
- `400 Bad Request` - Invalid IDs or already assigned
- `500 Internal Server Error` - Server error

### Remove Amenity from Room

```
DELETE /room-amenities/remove/{roomId}/{amenityId}
```

**Path Parameters:**
- `roomId` (Int) - Room ID
- `amenityId` (Int) - Amenity ID

**Responses:**
- `200 OK` - Success message
- `400 Bad Request` - Invalid IDs
- `404 Not Found` - Assignment not found
- `500 Internal Server Error` - Server error

---

## Bookings

### Get Booking by ID

```
GET /bookings/{id}
```

**Path Parameters:**
- `id` (Int) - Booking ID

**Responses:**
- `200 OK` - `BookingDTO`
- `400 Bad Request` - Invalid ID
- `404 Not Found` - Booking not found
- `500 Internal Server Error` - Server error

### Get Bookings by User ID

```
GET /bookings/user/{userId}
```

**Path Parameters:**
- `userId` (Int) - User ID

**Responses:**
- `200 OK` - `List<BookingDTO>`
- `400 Bad Request` - Invalid user ID
- `500 Internal Server Error` - Server error

### Create Booking

```
POST /bookings
```

**Request Body:** `BookingCreateRequest`
```json
{
  "userId": 1,
  "roomId": 1,
  "dateFrom": "2026-04-10T14:00:00Z",
  "dateTo": "2026-04-15T12:00:00Z"
}
```

**Note:** Dates must be in ISO-8601 format. `dateFrom` must be before `dateTo`.

**Responses:**
- `201 Created` - `BookingDTO`
- `400 Bad Request` - Validation error (date_from must be before date_to)
- `500 Internal Server Error` - Server error

### Update Booking Status

```
PUT /bookings/{id}/status
```

**Path Parameters:**
- `id` (Int) - Booking ID

**Request Body:** `BookingStatusUpdateRequest`
```json
{
  "status": "string (e.g., Active, Canceled, Completed)"
}
```

**Responses:**
- `200 OK` - `BookingDTO`
- `400 Bad Request` - Invalid ID or empty status
- `404 Not Found` - Booking not found
- `500 Internal Server Error` - Server error

### Update Expired Bookings

```
POST /bookings/update-expired
```

**Description:** Updates all bookings where `date_to` has passed to "Completed" status. Can be called manually or via scheduled task.

**Responses:**
- `200 OK` - Count message
- `500 Internal Server Error` - Server error

---

## Reviews

### Get Review by ID

```
GET /reviews/{id}
```

**Path Parameters:**
- `id` (Int) - Review ID

**Responses:**
- `200 OK` - `ReviewDTO`
- `400 Bad Request` - Invalid ID
- `404 Not Found` - Review not found
- `500 Internal Server Error` - Server error

### Get Reviews by Hotel ID (with Stats)

```
GET /reviews/hotel/{hotelId}
```

**Path Parameters:**
- `hotelId` (Int) - Hotel ID

**Responses:**
- `200 OK` - `HotelReviewStatsResponse`
- `400 Bad Request` - Invalid hotel ID
- `500 Internal Server Error` - Server error

**Response Example:**
```json
{
  "hotelId": 1,
  "reviewCount": 10,
  "averageRating": 4.5,
  "reviews": [
    {
      "id": 1,
      "bookingId": 5,
      "hotelId": 1,
      "rating": 5,
      "text": "Great hotel!",
      "sentimentScore": 5.0,
      "createdAt": "2026-04-01T10:00:00Z"
    }
  ]
}
```

### Create Review

```
POST /reviews
```

**Request Body:** `ReviewCreateRequest`
```json
{
  "bookingId": 1,
  "hotelId": 1,
  "rating": 5,
  "text": "string or null"
}
```

**Note:** Rating must be between 1 and 5. The `sentiment_score` is automatically calculated (currently returns 5.0 as placeholder).

**Responses:**
- `201 Created` - `ReviewDTO`
- `400 Bad Request` - Validation error (rating out of range)
- `500 Internal Server Error` - Server error

---

## Admin Requests

### Get Admin Request by ID

```
GET /admin-requests/{id}
```

**Path Parameters:**
- `id` (Int) - Admin request ID

**Responses:**
- `200 OK` - `AdminRequestDTO`
- `400 Bad Request` - Invalid ID
- `404 Not Found` - Admin request not found
- `500 Internal Server Error` - Server error

### Get Admin Requests by User ID

```
GET /admin-requests/user/{userId}
```

**Path Parameters:**
- `userId` (Int) - User ID

**Responses:**
- `200 OK` - `List<AdminRequestDTO>`
- `400 Bad Request` - Invalid user ID
- `500 Internal Server Error` - Server error

### Create Admin Request

```
POST /admin-requests
```

**Request Body:** `AdminRequestCreateRequest`
```json
{
  "userId": 1,
  "text": "string"
}
```

**Responses:**
- `201 Created` - `AdminRequestDTO`
- `400 Bad Request` - Validation error (empty text)
- `500 Internal Server Error` - Server error

### Update Admin Request Status

```
PUT /admin-requests/{id}/status
```

**Path Parameters:**
- `id` (Int) - Admin request ID

**Request Body:** `AdminRequestStatusUpdateRequest`
```json
{
  "status": "string (e.g., Pending, Approved, Rejected)"
}
```

**Responses:**
- `200 OK` - `AdminRequestDTO`
- `400 Bad Request` - Invalid ID or empty status
- `404 Not Found` - Admin request not found
- `500 Internal Server Error` - Server error

---

## Data Models

### HotelDTO
```json
{
  "id": 1,
  "adminId": 1,
  "name": "string",
  "city": "string",
  "description": "string or null",
  "address": "string",
  "rating": 0.0
}
```

### RoomDTO
```json
{
  "id": 1,
  "hotelId": 1,
  "roomName": "string",
  "description": "string or null",
  "price": 0.0,
  "maxGuests": 2,
  "status": "string"
}
```

### AmenityDTO
```json
{
  "id": 1,
  "name": "string"
}
```

### BookingDTO
```json
{
  "id": 1,
  "userId": 1,
  "roomId": 1,
  "dateFrom": "ISO-8601 timestamp",
  "dateTo": "ISO-8601 timestamp",
  "status": "string",
  "createdAt": "ISO-8601 timestamp"
}
```

### ReviewDTO
```json
{
  "id": 1,
  "bookingId": 1,
  "hotelId": 1,
  "rating": 5,
  "text": "string or null",
  "sentimentScore": 5.0,
  "createdAt": "ISO-8601 timestamp"
}
```

### AdminRequestDTO
```json
{
  "id": 1,
  "userId": 1,
  "text": "string",
  "status": "string",
  "createdAt": "ISO-8601 timestamp"
}
```

### UserResponse
```json
{
  "id": 1,
  "email": "string",
  "name": "string",
  "role": "string"
}
```

---

## Booking Status Lifecycle

Booking statuses follow this flow:
- `Active` - Initial status when booking is created
- `Canceled` - When user cancels the booking
- `Completed` - Automatically set when `date_to` has passed (via `updateExpiredBookings` endpoint)

---

## Notes

1. All date/time fields use ISO-8601 format (e.g., `2026-04-10T14:00:00Z`)
2. Authentication endpoints return tokens that must be included in the `Authorization` header as `Bearer <token>`
3. Sessions expire after 48 hours
4. The `sentiment_score` field in reviews currently returns 5.0 as a placeholder - to be replaced with actual NLP logic later
5. Hotel ratings are initially set to 0.0 and should be updated based on review averages (manual or automated)
6. All error responses include a descriptive error message
