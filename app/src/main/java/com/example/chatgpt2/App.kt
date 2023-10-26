package com.example.chatgpt2

import android.app.Application
import com.tencent.mmkv.MMKV

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
        xcrash.XCrash.init(this)
    }
}