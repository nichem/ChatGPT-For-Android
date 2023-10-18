package com.cjcrafter.openai.image

import com.google.gson.annotations.SerializedName
import java.util.Base64

data class ImageData(
    val url: String,
    @field:SerializedName("b64_json") val base64: String
)
