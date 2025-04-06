package com.example.sarcopeniamonitor

import androidx.compose.runtime.mutableStateListOf
import org.json.JSONObject

class ImageList(
    inputImagesList: List<MealImage> = emptyList()  // Accept an initial list of MealImage objects
) {
    private val _images = mutableStateListOf<MealImage>().apply {
        addAll(inputImagesList)  // Initialize with the provided list
    }
    val images: List<MealImage> = _images

    fun addImage(mealType: String = "Other") {
        // Add a new image with a null URI and the specified meal type
        _images.add(MealImage(imageUri = null, mealType = mealType))
    }

    fun updateImage(index: Int, imageUri: String?, mealType: String) {
        if (index in _images.indices) {
            // Extract "image_uri" if the imageUri is a JSON string
            val extractedUri = if (imageUri != null && imageUri.startsWith("{") && imageUri.endsWith("}")) {
                try {
                    JSONObject(imageUri).getString("image_uri")
                } catch (e: Exception) {
                    imageUri // Fallback to the original value if parsing fails
                }
            } else {
                imageUri
            }

            // Update the image at the specified index
            _images[index].imageUri = extractedUri
            _images[index].mealType = mealType
        }
    }

    fun removeImage(imageUri: String?) {
        // Remove the image by URI
        _images.removeIf { it.imageUri == imageUri }
    }

    fun clear() {
        _images.clear()
    }

    fun getAllImages(): List<MealImage> {
        return _images.toList()
    }
}
