package com.example.chatgpt2.view

import android.content.Context
import androidx.lifecycle.lifecycleScope
import com.example.chatgpt2.R
import com.example.chatgpt2.databinding.DialogSettingBinding
import com.example.chatgpt2.utils.RepUtils
import com.lxj.xpopup.core.CenterPopupView
import kotlinx.coroutines.launch

class SettingDialog(context: Context, private val onSave: () -> Unit) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.dialog_setting
    }

    private lateinit var binding: DialogSettingBinding

    override fun onCreate() {
        super.onCreate()
        binding = DialogSettingBinding.bind(contentView)
        initValues()
        binding.btnSave.setOnClickListener {
            saveValues()
            dismiss()
            onSave()
        }
        if (RepUtils.balanceCache < 0f) updateBalance()
        else binding.tvBalance.text = "${RepUtils.balanceCache}"
        binding.btnRefresh.setOnClickListener {
            updateBalance()
        }
    }

    private fun updateBalance() = lifecycleScope.launch {
        binding.tvBalance.text = "获取中..."
        val balance = RepUtils.getBalance()
        binding.tvBalance.text = "$balance"
        RepUtils.balanceCache = balance
    }

    private fun saveValues() {
        RepUtils.apiHost = binding.editHost.text.toString()
        RepUtils.apiKey = binding.editKey.text.toString()
    }

    private fun initValues() {
        binding.editKey.setText(RepUtils.apiKey)
        binding.editHost.setText(RepUtils.apiHost)
    }

}