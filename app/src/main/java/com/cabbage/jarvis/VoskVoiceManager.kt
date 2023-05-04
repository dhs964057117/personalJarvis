package com.cabbage.jarvis

import android.text.TextUtils
import android.util.Log
import com.cabbage.jarvis.data.VoiceData
import com.google.gson.Gson
import org.vosk.LibVosk
import org.vosk.LogLevel
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.SpeechStreamService
import org.vosk.android.StorageService
import java.io.IOException
import java.io.InputStream
import java.lang.Exception

object VoskVoiceManager : RecognitionListener {
    const val TAG = "VoskVoiceManager"
    private const val STATE_START = 0
    private const val STATE_READY = 1
    private const val STATE_DONE = 2
    private const val STATE_FILE = 3
    private const val STATE_MIC = 4

    private var model: Model? = null
    private var speechService: SpeechService? = null
    private var speechStreamService: SpeechStreamService? = null
    private var callBack: RecognitionListener? = null
    private var gson = Gson()

    private var stringBuffer: StringBuffer = StringBuffer()

    fun initModel(callBack: RecognitionListener?) {
        this.callBack = callBack
        LibVosk.setLogLevel(LogLevel.INFO)
        StorageService.unpack(App.application, "model-small-cn-0.22", "model",
            { model: Model? ->
                this.model = model
                initRecognizeMicrophone()
            },
            { exception: IOException ->
                Log.e(
                    TAG, "Failed to unpack the model" + exception.message
                )
            })
    }

    fun initRecognizeMicrophone() {
        if (speechService != null) {
            speechService!!.stop()
            speechService = null
        } else {
            try {
                val rec = Recognizer(model, 16000.0f)
                speechService = SpeechService(rec, 16000.0f)
                speechService!!.startListening(this)
            } catch (e: IOException) {
                setErrorState(e.message)
            }
        }
    }

    private fun recognizeFile() {
        if (speechStreamService != null) {
            speechStreamService!!.stop()
            speechStreamService = null
        } else {
            try {
                val rec = Recognizer(
                    model, 16000f, "[\"one zero zero zero one\", " +
                            "\"oh zero one two three four five six seven eight nine\", \"[unk]\"]"
                )
                val ais: InputStream = App.application.assets.open(
                    "10001-90210-01803.wav"
                )
                if (ais.skip(44) != 44L) throw IOException("File too short")
                speechStreamService = SpeechStreamService(rec, ais, 16000f)
                speechStreamService!!.start(this)
            } catch (e: IOException) {
                setErrorState(e.message)
            }
        }
    }

    fun pause(checked: Boolean) {
        if (speechService != null) {
            speechService!!.setPause(checked)
        }
    }

    private fun setErrorState(message: String?) {
        Log.i(TAG, "setErrorState: $message")
    }

    override fun onPartialResult(hypothesis: String?) {
        Log.i(TAG, "onPartialResult: $hypothesis")
        callBack?.onPartialResult(hypothesis)
    }

    override fun onResult(hypothesis: String?) {
        Log.i(TAG, "onResult: $hypothesis")
        if (!TextUtils.isEmpty(hypothesis)) {
            val msg = gson.fromJson(hypothesis, VoiceData::class.java)
            if (!TextUtils.isEmpty(msg.text))
                callBack?.onResult(hypothesis)
        }
    }

    override fun onFinalResult(hypothesis: String?) {
        Log.i(TAG, "onFinalResult: $hypothesis")
        callBack?.onFinalResult(hypothesis)

    }

    override fun onError(exception: Exception?) {
        Log.i(TAG, "onError: $exception")
        callBack?.onError(exception)
    }

    override fun onTimeout() {
        Log.i(TAG, "onTimeout:")
        callBack?.onTimeout()
    }
}