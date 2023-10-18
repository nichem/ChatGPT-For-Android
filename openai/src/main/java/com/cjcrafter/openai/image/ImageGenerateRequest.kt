package com.cjcrafter.openai.image

import com.google.gson.annotations.SerializedName

data class ImageGenerateRequest @JvmOverloads constructor(
    var prompt: String,
    @field:SerializedName("n") var count: Int,
    @field:SerializedName("response_format") var imageFormat: ImageFormat,
    @field:SerializedName("size") var imageSize: ImageSize
    //response_format
) {
    class Builder() {
        private var prompt: String = ""
        private var count: Int = 1
        private var imageFormat: ImageFormat = ImageFormat.BASE64
        private var imageSize: ImageSize = ImageSize.SIZE_256
        fun prompt(prompt: String): Builder {
            this.prompt = prompt
            return this
        }

        fun count(count: Int): Builder {
            this.count = count
            return this
        }

        fun imageFormat(imageFormat: ImageFormat): Builder {
            this.imageFormat = imageFormat
            return this
        }

        fun imageSize(imageSize: ImageSize): Builder {
            this.imageSize = imageSize
            return this
        }

        fun build(): ImageGenerateRequest {
            return ImageGenerateRequest(prompt, count, imageFormat, imageSize)
        }
    }
}
