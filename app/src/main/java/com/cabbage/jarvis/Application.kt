package com.cabbage.jarvis

import android.app.Application
import android.content.Context
import com.qweather.sdk.view.HeConfig

class Application : Application() {
    override fun onCreate() {
        super.onCreate()
        App.inject(this, true)
        init()
    }

    private fun init() {
        //天气sdk初始化
        HeConfig.init("HE2305031035431032", "77306fc005ce435ebcd36402c6454410");
        //Switch to Free subscription
        HeConfig.switchToDevService();
        //Switch to Standard subscription
//        HeConfig.switchToBizService();
    }
}