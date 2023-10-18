package com.example.chatgpt2

import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cjcrafter.openai.OpenAI
import com.cjcrafter.openai.chat.ChatMessage
import com.cjcrafter.openai.chat.ChatMessage.Companion.toAssistantMessage
import com.cjcrafter.openai.chat.ChatMessage.Companion.toSystemMessage
import com.cjcrafter.openai.chat.ChatMessage.Companion.toUserMessage
import com.cjcrafter.openai.chat.ChatModel
import com.cjcrafter.openai.chat.ChatRequest
import com.cjcrafter.openai.chat.ChatUser
import com.example.chatgpt2.databinding.ActivityMainBinding
import com.example.chatgpt2.utils.RepUtils
import com.example.chatgpt2.utils.showAsk
import com.example.chatgpt2.utils.showSetting
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


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
                enableBtnSend(false)
                sendMessage(binding.editSend.text.toString())
                enableBtnSend(true)
            }
        }
        adapter.addChildClickViewIds(R.id.btnRefresh)
        adapter.setOnItemChildClickListener { _, _, pos ->
            lifecycleScope.launch {
                enableBtnSend(false)
                refresh()
                enableBtnSend(true)
            }
        }
        adapter.setOnItemLongClickListener { _, _, pos ->
            showAsk("是否删除词条信息？") {
                adapter.removeAt(pos)
            }
            true
        }

        binding.btnClear.setOnClickListener {
            showAsk("是否清空？") {
                adapter.data.clear()
                RepUtils.messages = adapter.data
                adapter.notifyDataSetChanged()
            }
        }

        binding.btnWeb.setOnClickListener {
            lifecycleScope.launch {
                enableBtnSend(false)
                val r = RepUtils.search(binding.editSend.text.toString())
                binding.editSend.setText("")
                adapter.addData(r.toUserMessage())
                adapter.addData("分析一下上述信息".toUserMessage())
                generateResult(adapter.data)
                enableBtnSend(true)
            }
        }
        binding.rv.itemAnimator = null
        binding.tvModel.text = RepUtils.modelString
    }

    private suspend fun refresh() {
        adapter.removeAt(adapter.itemCount - 1)
        generateResult(adapter.data)
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
                holder.setText(R.id.tvContent, "${item.role.name}:${item.content}")
                holder.getView<ImageButton>(R.id.btnRefresh)?.visibility =
                    if (item.role.name == ChatUser.SYSTEM.name && holder.adapterPosition == itemCount - 1)
                        View.VISIBLE else View.INVISIBLE
            }
        }

    private fun enableBtnSend(isEnable: Boolean) {
        binding.btnSend.isEnabled = isEnable
        binding.btnSend.setImageResource(if (isEnable) R.drawable.baseline_send_24 else R.drawable.baseline_more_horiz_24)
        binding.btnWeb.isEnabled = isEnable
        binding.btnWeb.setImageResource(if (isEnable) R.drawable.baseline_search_24 else R.drawable.baseline_more_horiz_24)
    }

    private suspend fun generateResult(messages: MutableList<ChatMessage>) {
        val request = ChatRequest.builder()
            .messages(messages)
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
}