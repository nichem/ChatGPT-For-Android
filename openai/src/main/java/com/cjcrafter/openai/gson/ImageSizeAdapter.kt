package com.cjcrafter.openai.gson

import com.cjcrafter.openai.image.ImageSize
import com.cjcrafter.openai.image.imageSizeString2Value
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
            imageSizeString2Value(reader?.nextString())
        }
    }

}