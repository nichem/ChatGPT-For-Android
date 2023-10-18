package com.cjcrafter.openai.gson

import com.cjcrafter.openai.image.ImageSize
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

class ImageSizeAdapter : TypeAdapter<ImageSize?>() {
    override fun write(writer: JsonWriter?, value: ImageSize?) {
        if (value == null) {
            writer?.nullValue()
        } else {
            writer?.value(value.string)
        }
    }

    override fun read(reader: JsonReader?): ImageSize? {
        return if (reader?.peek() == JsonToken.NULL) {
            reader.nextNull()
            null
        } else {
            when (reader?.nextString()) {
                "256x256" -> ImageSize.SIZE_256
                "512x512" -> ImageSize.SIZE_512
                else -> ImageSize.SIZE_1024
            }
        }
    }

}