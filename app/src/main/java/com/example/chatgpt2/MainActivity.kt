package com.example.chatgpt2

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.CleanUtils
import com.blankj.utilcode.util.FileUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cjcrafter.openai.OpenAI
import com.cjcrafter.openai.chat.ChatMessage
import com.cjcrafter.openai.chat.ChatMessage.Companion.toAssistantMessage
import com.cjcrafter.openai.chat.ChatMessage.Companion.toSystemMessage
import com.cjcrafter.openai.chat.ChatMessage.Companion.toUserMessage
import com.cjcrafter.openai.chat.ChatRequest
import com.cjcrafter.openai.chat.ChatUser
import com.cjcrafter.openai.image.ImageFormat
import com.cjcrafter.openai.image.ImageGenerateRequest
import com.cjcrafter.openai.image.ImageSize
import com.example.chatgpt2.databinding.ActivityMainBinding
import com.example.chatgpt2.utils.RepUtils
import com.example.chatgpt2.utils.showAsk
import com.example.chatgpt2.utils.showLoading
import com.example.chatgpt2.utils.showSetting
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private var openAI: OpenAI? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        openAI = OpenAI(RepUtils.apiKey, RepUtils.apiHost)

        binding.rv.adapter = adapter
        adapter.setList(RepUtils.messages)
        binding.btnSetting.setOnClickListener {
            showSetting {
                openAI = OpenAI(RepUtils.apiKey, RepUtils.apiHost)
                binding.tvModel.text = RepUtils.modelString
            }
        }

        binding.btnSend.setOnClickListener {
            if (binding.editSend.text.isBlank()) return@setOnClickListener
            lifecycleScope.launch {
                enableSendMessage(false)
                sendMessage(binding.editSend.text.toString())
                enableSendMessage(true)
            }
        }
        adapter.addChildClickViewIds(R.id.btnRefresh)
        adapter.setOnItemChildClickListener { _, _, pos ->
            lifecycleScope.launch {
                enableSendMessage(false)
                refresh()
                enableSendMessage(true)
            }
        }
        adapter.setOnItemLongClickListener { _, _, pos ->
            showAsk("是否删除词条信息？") {
                adapter.removeAt(pos)
            }
            true
        }

        binding.btnClear.setOnClickListener {
            showAsk("是否清空?\n(包含图片缓存${FileUtils.getSize(cacheDir)})") {
                lifecycleScope.launch {
                    val loading = showLoading("清理中...")
                    adapter.data.clear()
                    RepUtils.messages = adapter.data
                    adapter.notifyDataSetChanged()
                    withContext(IO) { CleanUtils.cleanInternalCache() }
                    loading.dismiss()
                }
            }
        }

        binding.btnWeb.setOnClickListener {
            if (binding.editSend.text.isBlank()) return@setOnClickListener
            lifecycleScope.launch {
                enableSendMessage(false)
                val r = RepUtils.search(binding.editSend.text.toString())
                binding.editSend.setText("")
                adapter.addData(r.toUserMessage())
                adapter.addData("分析一下上述信息".toUserMessage())
                generateResult(adapter.data)
                enableSendMessage(true)
            }
        }

        binding.btnImageGenerate.setOnClickListener {
            if (binding.editSend.text.isBlank()) return@setOnClickListener
            lifecycleScope.launch {
                enableSendMessage(false)
                val prompt = binding.editSend.text.toString()
                adapter.addData("$IMAGE_START$prompt".toUserMessage())
                generateImage(prompt)
                enableSendMessage(true)
            }
        }
        binding.rv.itemAnimator = null
        binding.tvModel.text = RepUtils.modelString
    }

    private suspend fun refresh() {
        val lastMessage = adapter.data[adapter.itemCount - 1]
        adapter.removeAt(adapter.itemCount - 1)
        if (lastMessage.isImageGenerationMessage()) {
            val lastMessage2 = adapter.data[adapter.itemCount - 1]
            if (lastMessage2.isImageGenerationMessage()) {
                val prompt = lastMessage2.content.replace(IMAGE_START, "")
                generateImage(prompt)
            }
        } else generateResult(adapter.data)
    }

    private suspend fun sendMessage(content: String) {
        binding.editSend.setText("")
        val message = content.toUserMessage()
        adapter.addData(message)
        generateResult(adapter.data)
    }

    private val adapter =
        object : BaseQuickAdapter<ChatMessage, BaseViewHolder>(R.layout.item_message) {
            override fun convert(holder: BaseViewHolder, item: ChatMessage) {
                holder.setGone(R.id.imageView, true)
                holder.setVisible(R.id.tvContent, true)
                if (item.isImageGenerationMessage()) {
                    when (item.role) {
                        ChatUser.ASSISTANT -> {
                            holder.setVisible(R.id.tvContent, false)
                            holder.setGone(R.id.imageView, false)
                            val filename = item.content.replace(IMAGE_START, "")
                            Glide.with(context)
                                .load(File(cacheDir, filename))
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .into(holder.getView(R.id.imageView))
                        }
                        ChatUser.USER -> {
                            holder.setText(R.id.tvContent, item.content)
                        }
                        else -> {
                            holder.setText(R.id.tvContent, item.content)
                            holder.getView<ImageButton>(R.id.btnRefresh).visibility =
                                if (item.role.name == ChatUser.SYSTEM.name && holder.adapterPosition == itemCount - 1)
                                    View.VISIBLE else View.INVISIBLE
                        }
                    }
                } else {
                    holder.setText(R.id.tvContent, "${item.role.name}:${item.content}")
                    holder.getView<ImageButton>(R.id.btnRefresh).visibility =
                        if (item.role.name == ChatUser.SYSTEM.name && holder.adapterPosition == itemCount - 1)
                            View.VISIBLE else View.INVISIBLE
                }
            }
        }

    private fun enableSendMessage(isEnable: Boolean) {
        binding.btnSend.isEnabled = isEnable
        binding.btnSend.setImageResource(if (isEnable) R.drawable.baseline_send_24 else R.drawable.baseline_more_horiz_24)
        binding.btnWeb.isEnabled = isEnable
        binding.btnWeb.setImageResource(if (isEnable) R.drawable.baseline_search_24 else R.drawable.baseline_more_horiz_24)
        binding.btnImageGenerate.isEnabled = isEnable
        binding.btnImageGenerate.setImageResource(if (isEnable) R.drawable.baseline_image_24 else R.drawable.baseline_more_horiz_24)
    }

    private suspend fun generateResult(messages: MutableList<ChatMessage>) {
        val messages2 = mutableListOf<ChatMessage>().apply { addAll(messages) }
        messages2.removeAll {
            it.isImageGenerationMessage()
        }
        val request = ChatRequest.builder()
            .messages(messages2)
            .model(RepUtils.modelString)
            .build()
        adapter.addData("".toAssistantMessage())
        var isGenerateFinish = false
        openAI?.streamChatCompletionAsync(
            request,
            onResponse = {
                lifecycleScope.launch {
                    isGenerateFinish = it.choices[0].isFinished()
                    if (adapter.data.isNotEmpty()) {
                        val lastIndex = adapter.data.size - 1
                        adapter.data[lastIndex] = it.choices[0].message
                        adapter.notifyItemChanged(lastIndex)
                    }
                }
            },
            onFailure = {
                runOnUiThread {
                    isGenerateFinish = true
                    if (adapter.data.isNotEmpty()) {
                        val lastIndex = adapter.data.size - 1
                        adapter.data[lastIndex] = it.stackTraceToString().toSystemMessage()
                        adapter.notifyItemChanged(lastIndex)
                    }
                }
            }
        )
        while (!isGenerateFinish) delay(100)
    }

    override fun onPause() {
        super.onPause()
        RepUtils.messages = adapter.data
    }

    private fun saveBitmap(bitmap: Bitmap, fileName: String) {
        val root = cacheDir
        val file = File(root, fileName).apply {
            if (exists()) delete()
            createNewFile()
        }
        try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun ChatMessage.isImageGenerationMessage(): Boolean {
        return content.startsWith(IMAGE_START)
    }

    private suspend fun generateImage(prompt: String) {
        val requestBase64 = ImageGenerateRequest.Builder()
            .prompt(prompt)
            .imageFormat(ImageFormat.BASE64)
            .imageSize(ImageSize.SIZE_256)
            .build()
        binding.editSend.setText("")
        withContext(Default) {
            openAI?.imageGeneration(requestBase64, onResponse = {
                val base64 = it.data[0].base64
                Log.d("dl", "base64:$base64")
                val data = Base64.decode(base64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                val filename = "${System.currentTimeMillis()}.png"
                saveBitmap(bitmap, filename)
                runOnUiThread {
                    adapter.addData("$IMAGE_START$filename".toAssistantMessage())
                }
            }, onFailure = {
                runOnUiThread {
                    adapter.addData("$IMAGE_START${it.stackTraceToString()}".toSystemMessage())
                }
            })
        }
    }

    companion object {
        private const val IMAGE_START = "IMAGE:"
    }

}