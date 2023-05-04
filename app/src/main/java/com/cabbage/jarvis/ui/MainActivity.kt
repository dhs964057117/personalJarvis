package com.cabbage.jarvis.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.baidu.location.LocationClient
import com.cabbage.jarvis.BaiduLocation
import com.cabbage.jarvis.BaiduLocation.isLocServiceEnable
import com.cabbage.jarvis.R
import com.cabbage.jarvis.databinding.ActivityMainBinding
import com.cabbage.jarvis.full.MainActivity
import com.cabbage.jarvis.utils.AT
import org.vosk.android.RecognitionListener
import permissions.dispatcher.*
import java.lang.Exception

@RuntimePermissions
class MainActivity : AppCompatActivity(), RecognitionListener {

    private val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Example of a call to a native method
        binding.sampleText.text = stringFromJNI()

//        getWeather()
        binding.startService.setOnClickListener(this@MainActivity::onClick)

    }

    fun onClick(view: View) {
        when (view) {
            binding.startService -> {
                startServiceWithPermissionCheck()
            }
        }
    }

    @NeedsPermission(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.INTERNET,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    fun startService() {
//        val serviceIntent = Intent(this, PorcupineService::class.java)
//        ContextCompat.startForegroundService(this, serviceIntent)
        LocationClient.setAgreePrivacy(true)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    @OnShowRationale(Manifest.permission.RECORD_AUDIO)
    fun onShowRecordAudio(request: PermissionRequest) {
        showRationaleDialog(R.string.need_audio_permission, request)
    }

    @OnNeverAskAgain(Manifest.permission.RECORD_AUDIO)
    fun onRecordAudioNeverAskAgain() {
        AT.toastError(R.string.permission_audio_never_ask_again)
    }

    @OnPermissionDenied(Manifest.permission.RECORD_AUDIO)
    fun onRecordAudioDenied() {
        // NOTE: Deal with a denied permission, e.g. by showing specific UI
        // or disabling certain functionality
        AT.toastWarning(R.string.permission_audio_denied)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // NOTE: delegate the permission handling to generated method
        onRequestPermissionsResult(requestCode, grantResults)
    }

    /**
     * A native method that is implemented by the 'jarvis' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'jarvis' library on application startup.
        init {
            System.loadLibrary("jarvis")
        }
    }

    override fun onPartialResult(hypothesis: String?) {
        Log.i(TAG, "onPartialResult: $hypothesis")
    }

    override fun onResult(hypothesis: String?) {
        Log.i(TAG, "onResult: $hypothesis")
    }

    override fun onFinalResult(hypothesis: String?) {
        Log.i(TAG, "onFinalResult: $hypothesis")
    }

    override fun onError(exception: Exception?) {
        Log.i(TAG, "onError: $exception")
    }

    override fun onTimeout() {
        Log.i(TAG, "onTimeout:")
    }

    private fun showRationaleDialog(@StringRes messageResId: Int, request: PermissionRequest) {
        AlertDialog.Builder(this)
            .setPositiveButton(R.string.button_allow) { _, _ -> request.proceed() }
            .setNegativeButton(R.string.button_deny) { _, _ -> request.cancel() }
            .setCancelable(false)
            .setMessage(messageResId)
    }

    fun initLocation() {}
}