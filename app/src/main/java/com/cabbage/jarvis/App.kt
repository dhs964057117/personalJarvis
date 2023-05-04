package com.cabbage.jarvis

import android.app.Application
import android.content.Context

object App {
    @JvmStatic
    lateinit var application: Application
        private set

    @JvmStatic
    var debug: Boolean = false

    @JvmStatic
    fun inject(application: Application, debug: Boolean = false) {
        App.application = application
        App.debug = debug
    }
}