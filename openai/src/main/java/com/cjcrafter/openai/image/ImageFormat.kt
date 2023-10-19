package com.cjcrafter.openai.image

enum class ImageFormat(val string: String) {
    URL("url"), BASE64("b64_json")
}

fun imageFormatString2Value(string: String?): ImageFormat {
    enumValues<ImageFormat>().forEach {
        if (it.string == string) return it
    }
    return ImageFormat.BASE64
}