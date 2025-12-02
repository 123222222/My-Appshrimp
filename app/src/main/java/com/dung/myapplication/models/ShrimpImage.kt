package com.dung.myapplication.models

import kotlinx.serialization.Serializable

@Serializable
data class ShrimpImage(
    val id: String = "",
    val imageUrl: String,
    val cloudinaryUrl: String,
    val detections: List<ShrimpDetection>,
    val timestamp: Long = System.currentTimeMillis(),
    val capturedFrom: String = ""
)

@Serializable
data class ShrimpDetection(
    val className: String,
    val confidence: Float,
    val bbox: BoundingBox,
    val length: Float = 0f,  // Chiều dài tôm (cm)
    val weight: Float = 0f   // Khối lượng tôm (gram)
)

@Serializable
data class BoundingBox(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
)

// Response từ backend sau khi xử lý YOLO
@Serializable
data class YoloProcessResponse(
    val success: Boolean,
    val imageUrl: String,
    val cloudinaryUrl: String,
    val detections: List<ShrimpDetection>,
    val mongoId: String,
    val message: String = ""
)

