package com.cabbage.jarvis.data

import kotlinx.coroutines.CancellableContinuation
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume


/**
 * Description: Constant
 * Author: Voine
 * Date: 2023/2/25
 */
object Constant {
    const val SAVED_CHAT_KEY = "saved_chat_key"
    const val SAVED_TRANSLATE_APP_ID = "saved_translate_id"
    const val SAVED_TRANSLATE_KEY = "saved_translate_key"
    const val SAVED_STORE = "saved_store"
    const val SAVED_CHAT_NAME = "saved_chat_name"
    const val SAVED_FLAG_NEED_COPY_DATA = "need_copy_data"
    const val SAVED_HIYORI_SETTING = "saved_hiyori_setting"
    const val SAVED_AMADEUS_SETTING = "saved_amadeus_setting"
    const val SAVED_ATRI_SETTING = "saved_atri_setting"
    const val SAVED_EXTERNAL_SETTING = "saved_external_setting"
    const val SAVED_USE_TRANSLATE = "saved_use_translate"

    const val LIVE2D_BASE_PATH = "Live2DModels"
    const val VITS_BASE_PATH = "VITSModels"

    const val LOCAL_MODEL_HIYORI = "Hiyori"
    const val LOCAL_MODEL_KURISU = "kurisu"
    const val LOCAL_MODEL_ATRI = "atri"


    val EXTEND_LIVE2D_PATH = "jarvis/live2d/"
    val EXTEND_VITS_PATH = "jarvis/vits/"
}

fun <T> CancellableContinuation<T>.safeResume(value: T) {
    if (this.isActive) {
        (this as? Continuation<T>)?.resume(value)
    }
}