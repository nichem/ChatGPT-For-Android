package com.cjcrafter.openai.image

data class ImageResponse(
    val created: Int,
    val data: List<ImageData>
)
