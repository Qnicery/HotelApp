package com.example.testapp.data.api

/**
 * Конфигурация сервера
 * BASE_URL используется для всех запросов к API и для построения URL изображений
 */
object ServerConfig {
    // Для эмулятора Android используйте 10.0.2.2 вместо localhost
    // Для физического устройства используйте IP вашего компьютера
    const val BASE_URL = "http://10.0.2.2:8080/"

    /**
     * Добавить префикс сервера к относительному URL изображения
     * 
     * Примеры:
     * - "uploads/hotels/1/photo.jpg" → "http://10.0.2.2:8080/uploads/hotels/1/photo.jpg"
     * - "/uploads/hotels/1/photo.jpg" → "http://10.0.2.2:8080/uploads/hotels/1/photo.jpg"
     * - "http://..." → "http://..." (полный URL возвращается как есть)
     * - "" или null → "" (пустая строка)
     */
    fun getImageUrl(imagePath: String?): String {
        if (imagePath.isNullOrBlank()) return ""
        
        // Если уже полный URL — возвращаем как есть
        if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
            return imagePath
        }
        
        // Убираем ведущий слеш если есть
        val cleanPath = imagePath.removePrefix("/")
        
        return "$BASE_URL$cleanPath"
    }

    /**
     * Добавить префикс сервера к списку URL изображений
     */
    fun getImageUrls(imagePaths: List<String>?): List<String> {
        if (imagePaths == null) return emptyList()
        return imagePaths.map { getImageUrl(it) }
    }
}
