package com.cabbage.jarvis

import android.app.Application
import android.content.Context
import java.io.File

object App {
    @JvmStatic
    lateinit var application: Application
        private set

    @JvmStatic
    var debug: Boolean = false

    @JvmStatic
    lateinit var baseAppDir: String

    @JvmStatic
    fun inject(
        application: Application,
        debug: Boolean = false,
        dir: File = File("${application.filesDir?.parentFile?.path}")
    ) {
        App.application = application
        App.debug = debug
        App.baseAppDir = dir.absolutePath
    }
}