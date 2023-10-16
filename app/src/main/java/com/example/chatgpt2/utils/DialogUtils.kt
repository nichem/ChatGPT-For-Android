package com.example.chatgpt2.utils

import android.content.Context
import com.example.chatgpt2.view.SettingDialog
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView

fun Context.showSetting(onSave: () -> Unit) {
    val dialog = SettingDialog(this, onSave)
    XPopup.Builder(this)
        .autoOpenSoftInput(false)
        .autoFocusEditText(false)
        .isViewMode(true)
        .asCustom(dialog)
        .show()
}

fun Context.showAsk(content: String, onConfirm: () -> Unit) {
    XPopup.Builder(this)
        .asConfirm("", content) {
            onConfirm()
        }
        .show()
}

fun Context.showLoading(msg: String): BasePopupView {
    return XPopup.Builder(this)
        .asLoading(msg)
        .show()
}