package com.cabbage.jarvis.utils

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.annotation.StringRes
import com.cabbage.jarvis.App
import es.dmoral.toasty.Toasty

object AT {

    @JvmStatic
    private val handler: Handler = Handler(Looper.getMainLooper())
    fun toastSuccess(@StringRes msg: Int) {
        toastSuccess(App.application.getString(msg))
    }

    fun toastSuccess(msg: String) {
        toastSuccess(msg, Toast.LENGTH_SHORT)
    }

    fun toastSuccess(msg: String, duration: Int) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toasty.success(App.application, msg, duration).show()
        } else {
            handler.post {
                Toasty.success(App.application, msg, duration).show()
            }
        }
    }

    fun toastInfo(@StringRes msg: Int) {
        toastInfo(App.application.getString(msg), Toast.LENGTH_SHORT)
    }

    fun toastInfo(msg: String) {
        toastInfo(msg, Toast.LENGTH_SHORT)
    }

    fun toastInfo(msg: String, duration: Int) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toasty.info(App.application, msg, duration).show()
        } else {
            handler.post {
                Toasty.info(App.application, msg, duration).show()
            }
        }
    }

    fun toastError(@StringRes msg: Int) {
        toastError(App.application.getString(msg), Toast.LENGTH_SHORT)
    }

    fun toastError(msg: String) {
        toastError(msg, Toast.LENGTH_SHORT)
    }

    fun toastError(msg: String, duration: Int) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toasty.error(App.application, msg, duration).show()
        } else {
            handler.post {
                Toasty.error(App.application, msg, duration).show()
            }
        }
    }

    fun toastNormal(@StringRes msg: Int) {
        toastNormal(App.application.getString(msg), Toast.LENGTH_SHORT)
    }

    fun toastNormal(msg: String) {
        toastNormal(msg, Toast.LENGTH_SHORT)
    }

    fun toastNormal(msg: String, duration: Int) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toasty.normal(App.application, msg, duration).show()
        } else {
            handler.post {
                Toasty.normal(App.application, msg, duration).show()
            }
        }
    }

    fun toastWarning(@StringRes msg: Int) {
        toastWarning(App.application.getString(msg), Toast.LENGTH_SHORT)
    }

    fun toastWarning(msg: String) {
        toastWarning(msg, Toast.LENGTH_SHORT)
    }

    fun toastWarning(msg: String, duration: Int) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toasty.warning(App.application, msg, duration).show()
        } else {
            handler.post {
                Toasty.warning(App.application, msg, duration).show()
            }
        }
    }
}