package com.cjcrafter.openai.gson

import com.cjcrafter.openai.image.ImageFormat
import com.cjcrafter.openai.image.ImageSize
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

class ImageFormatAdapter : TypeAdapter<ImageFormat?>() {
    override fun write(writer: JsonWriter?, value: ImageFormat?) {
        if (value == null) {
            writer?.nullValue()
        } else {
            writer?.value(value.string)
        }
    }

    override fun read(reader: JsonReader?): ImageFormat? {
        return if (reader?.peek() == JsonToken.NULL) {
            reader.nextNull()
            null
        } else {
            when (reader?.nextString()) {
                "url" -> ImageFormat.URL
                else -> ImageFormat.BASE64
            }
        }
    }
}