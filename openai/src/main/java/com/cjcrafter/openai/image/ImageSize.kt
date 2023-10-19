package com.cjcrafter.openai.image

enum class ImageSize(val string: String) {
    SIZE_256("256x256"), SIZE_512("512x512"), SIZE_1024("1024x1024")
}

fun imageSizeString2Value(string: String?): ImageSize {
    enumValues<ImageSize>().forEach {
        if (it.string == string) return it
    }
    return ImageSize.SIZE_256
}