package com.cabbage.jarvis.ui

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cabbage.jarvis.App
import com.cabbage.jarvis.data.VITSLoadStatus
import com.cabbage.jarvis.data.safeResume
import com.cabbage.jarvis.utils.LocalModelManager
import com.chatwaifu.vits.utils.SoundGenerateHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

class InitViewModel : ViewModel() {
    enum class ChatStatus {
        DEFAULT,
        FETCH_INPUT,
        SEND_REQUEST,
        TRANSLATE,
        GENERATE_SOUND,
    }

    private val localModelManager: LocalModelManager by lazy {
        LocalModelManager()
    }
    val vitsHelper: SoundGenerateHelper by lazy {
        SoundGenerateHelper(App.application)
    }
    val loadVITSModelLiveData = MutableLiveData<VITSLoadStatus>()
    val chatStatusLiveData = MutableLiveData<ChatStatus>().apply { value = ChatStatus.DEFAULT }

    fun loadVitsModel(basePath: String) {
        val rootFiles = localModelManager.getVITSModelFiles(basePath)
        viewModelScope.launch(Dispatchers.IO) {
            val configResult = suspendCancellableCoroutine<Boolean> {
                vitsHelper.loadConfigs(rootFiles?.find { it.name.endsWith("json") }?.absolutePath) { isSuccess ->
                    it.safeResume(isSuccess)
                }
            }

            val binResult = suspendCancellableCoroutine<Boolean> {
                vitsHelper.loadModel(rootFiles?.find { it.name.endsWith("bin") }?.absolutePath) { isSuccess ->
                    it.safeResume(isSuccess)
                }
            }
            loadVITSModelLiveData.postValue(if (binResult && configResult) VITSLoadStatus.STATE_SUCCESS else VITSLoadStatus.STATE_FAILED)
        }
    }

    private fun generateAndPlaySound(needPlayText: String?) {
        vitsHelper.generateAndPlay(needPlayText) { isSuccess ->
            Log.d("TAG", "generate sound $isSuccess")
            if (chatStatusLiveData.value == ChatStatus.GENERATE_SOUND) {
                chatStatusLiveData.postValue(ChatStatus.DEFAULT)
            }
        }
    }
}