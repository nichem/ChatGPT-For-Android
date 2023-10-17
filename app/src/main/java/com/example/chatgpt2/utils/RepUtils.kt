package com.example.chatgpt2.utils

import android.util.Log
import com.cjcrafter.openai.chat.ChatMessage
import com.cjcrafter.openai.chat.ChatModel
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

object RepUtils {
    private const val DEFAULT_API_HOST = "https://api.chatanywhere.com.cn/"
    private val DEFAULT_MODEL = ChatModel.GPT_3_5_TURBO_0613.string
    private val mmkv = MMKV.defaultMMKV()
    private val gson = Gson()
    var apiKey: String
        get() = mmkv.decodeString("apiKey") ?: ""
        set(value) {
            mmkv.encode("apiKey", value)
        }

    var apiHost: String
        get() = mmkv.decodeString("apiHost", DEFAULT_API_HOST) ?: DEFAULT_API_HOST
        set(value) {
            mmkv.encode("apiHost", value)
        }

    var messages: List<ChatMessage>
        get() {
            val string = mmkv.decodeString("messages") ?: "[]"
            return gson.fromJson(string, object : TypeToken<List<ChatMessage>>() {}.type)
        }
        set(value) {
            val string = gson.toJson(value)
            mmkv.encode("messages", string)
        }
    var story: List<List<ChatMessage>>
        get() {
            val string = mmkv.decodeString("story") ?: "[]"
            return gson.fromJson(string, object : TypeToken<List<List<ChatMessage>>>() {}.type)
        }
        set(value) {
            val string = gson.toJson(value)
            mmkv.encode("story", string)
        }

    var balanceCache: Float
        get() {
            return mmkv.decodeFloat("balanceCache", -1f)
        }
        set(value) {
            mmkv.encode("balanceCache", value)
        }

    suspend fun getBalance(): Float {
        val okHttpClient = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.chatanywhere.org/v1/query/balance")
            .addHeader("Authorization", apiKey)
            .post(FormBody.Builder().build())
            .build()
        val res = withContext(Default) { okHttpClient.newCall(request).execute() }
        val data = res.body?.string() ?: "{}"
//        Log.d("dl", data)
        val json = JsonParser.parseString(data).asJsonObject
        val total = try {
            json.get("balanceTotal").asFloat
        } catch (e: Exception) {
            Log.e("dl", e.stackTraceToString())
            0f
        }
        val used = try {
            json.get("balanceUsed").asFloat
        } catch (e: Exception) {
            Log.e("dl", e.stackTraceToString())
            0f
        }
        Log.d("dl", "$total, $used")

        return total - used
    }

    fun getModelList(): List<String> {
        return enumValues<ChatModel>().map {
            it.string
        }
    }

    var modelString: String
        get() {
            return mmkv.decodeString("modelString", DEFAULT_MODEL) ?: DEFAULT_MODEL
        }
        set(value) {
            if (value in getModelList()) {
                mmkv.encode("modelString", value)
            }
        }
}