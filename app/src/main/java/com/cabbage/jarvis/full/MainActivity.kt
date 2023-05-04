/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.cabbage.jarvis.full

import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import com.cabbage.jarvis.VoskVoiceManager.initModel
import com.cabbage.jarvis.BaiduLocation.startLoc
import com.cabbage.jarvis.Weather.getGeoCity
import com.cabbage.jarvis.utils.AT.toastError
import com.cabbage.jarvis.Weather.getWeather
import com.cabbage.jarvis.utils.AT.toastSuccess
import androidx.appcompat.app.AppCompatActivity
import com.cabbage.jarvis.ui.InitViewModel
import com.cabbage.jarvis.utils.LocalModelManager
import com.cabbage.jarvis.App
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.cabbage.jarvis.data.ChannelListBean
import com.qweather.sdk.view.HeContext
import com.cabbage.jarvis.BaiduLocation.MyLocationListener
import com.baidu.location.BDLocation
import com.cabbage.jarvis.databinding.ActivityMainTempBinding
import com.cabbage.jarvis.utils.AT
import com.qweather.sdk.bean.base.Lang
import com.qweather.sdk.view.QWeather.OnResultGeoListener
import com.qweather.sdk.bean.base.Range
import com.qweather.sdk.bean.geo.GeoBean
import com.qweather.sdk.view.QWeather.OnResultWeatherNowListener
import com.qweather.sdk.bean.weather.WeatherNowBean
import com.qweather.sdk.view.HeContext.context
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.vosk.android.RecognitionListener
import java.lang.Exception
import java.util.ArrayList

class MainActivity : AppCompatActivity(), RecognitionListener {
    private val TAG = "FullMainActivity"
    private var binding: ActivityMainTempBinding? = null
    private var viewModel: InitViewModel? = null
    val localModelManager = LocalModelManager()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainTempBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        viewModel = ViewModelProvider(this).get(InitViewModel::class.java)
        val finalModelList = mutableListOf<ChannelListBean>()
        lifecycleScope.launch {
            withContext(this.coroutineContext) {
                localModelManager.initInnerModel(this@MainActivity, finalModelList)
                localModelManager.initExternalModel(this@MainActivity, finalModelList)
                viewModel!!.loadVitsModel(finalModelList[0].characterVitsPath)
            }
        }
        glSurfaceView = binding!!.gf
        glSurfaceView!!.setEGLContextClientVersion(2) // OpenGL ES 2.0を利用
        glRenderer = GLRenderer()
        glSurfaceView!!.setRenderer(glRenderer)
        glSurfaceView!!.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

//        setContentView(glSurfaceView);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) View.SYSTEM_UI_FLAG_LOW_PROFILE else View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        } else {
            window.insetsController!!.hide(WindowInsets.Type.navigationBars() or WindowInsets.Type.statusBars())
            window.insetsController!!.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        initModel(this)
    }

    override fun onStart() {
        super.onStart()
        LAppDelegate.getInstance().onStart(this)
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView!!.onResume()
        val decor = this.window.decorView
        decor.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    override fun onPause() {
        super.onPause()
        glSurfaceView!!.onPause()
        LAppDelegate.getInstance().onPause()
    }

    override fun onStop() {
        super.onStop()
        LAppDelegate.getInstance().onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        LAppDelegate.getInstance().onDestroy()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val pointX = event.x
        val pointY = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> LAppDelegate.getInstance().onTouchBegan(pointX, pointY)
            MotionEvent.ACTION_UP -> LAppDelegate.getInstance().onTouchEnd(pointX, pointY)
            MotionEvent.ACTION_MOVE -> LAppDelegate.getInstance().onTouchMoved(pointX, pointY)
        }
        return super.onTouchEvent(event)
    }

    private var glSurfaceView: GLSurfaceView? = null
    private var glRenderer: GLRenderer? = null
    override fun onPartialResult(hypothesis: String) {
//        Log.i(TAG, "onPartialResult: " + hypothesis);
    }

    override fun onResult(hypothesis: String) {
        Log.i(TAG, "onResult: $hypothesis")
        resolveMessage(hypothesis)
    }

    private fun resolveMessage(hypothesis: String) {
        if (hypothesis.contains("天气")) {
           viewModel!!.vitsHelper.generateAndPlay("こんにちは、私はあなたの助手です") {
                AT.toastInfo(it.toString())
            };
//            weather
        }
    }

    override fun onFinalResult(hypothesis: String) {
//        Log.i(TAG, "onFinalResult: " + hypothesis);
    }

    override fun onError(exception: Exception) {
//        Log.i(TAG, "onError: " + exception);
    }

    override fun onTimeout() {
//        Log.i(TAG, "onTimeout: ");
    }

    //先定位 然后获取位置信息，然后获取天气
    private val weather: Unit
        private get() {
            startLoc(object : MyLocationListener() {
                override fun onReceiveLocation(location: BDLocation) {
                    super.onReceiveLocation(location)
                    Log.d(TAG, "onReceiveLocation: $location")
                    if (location.locType == 61 || location.locType == 161) {
                        getGeoCity(
                            location.city,
                            "",
                            Range.CN,
                            1,
                            Lang.ZH_HANS,
                            object : OnResultGeoListener {
                                override fun onError(throwable: Throwable) {
                                    toastError("获取定位失败")
                                }

                                override fun onSuccess(geoBean: GeoBean) {
                                    Log.d(TAG, "onSuccess: $geoBean")
                                    val loc = geoBean.locationBean[0]
                                    getWeather(loc.id, object : OnResultWeatherNowListener {
                                        override fun onError(throwable: Throwable) {
                                            toastError("获取天气失败")
                                        }

                                        override fun onSuccess(WeatherNowBean: WeatherNowBean) {
                                            toastSuccess(WeatherNowBean.now.text)
                                        }
                                    })
                                }
                            })
                    } else {
                        toastError("获取定位失败 code:" + location.locType)
                        Log.d(TAG, "onReceiveLocation: error code " + location.locType)
                    }
                }
            })
        }
}