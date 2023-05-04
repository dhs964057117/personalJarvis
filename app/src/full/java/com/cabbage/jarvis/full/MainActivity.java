/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */

package com.cabbage.jarvis.full;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.baidu.location.BDLocation;
import com.cabbage.jarvis.App;
import com.cabbage.jarvis.BaiduLocation;
import com.cabbage.jarvis.VoskVoiceManager;
import com.cabbage.jarvis.Weather;
import com.qweather.sdk.bean.base.Lang;
import com.qweather.sdk.bean.base.Range;
import com.qweather.sdk.bean.geo.GeoBean;
import com.qweather.sdk.bean.grid.GridWeatherNowBean;
import com.qweather.sdk.bean.weather.WeatherNowBean;
import com.qweather.sdk.view.QWeather;

import org.vosk.android.RecognitionListener;

public class MainActivity extends Activity implements RecognitionListener {
    private String TAG = "FullMainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setEGLContextClientVersion(2);       // OpenGL ES 2.0を利用

        glRenderer = new GLRenderer();

        glSurfaceView.setRenderer(glRenderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        setContentView(glSurfaceView);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT
                            ? View.SYSTEM_UI_FLAG_LOW_PROFILE
                            : View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            );
        } else {
            getWindow().getInsetsController().hide(WindowInsets.Type.navigationBars() | WindowInsets.Type.statusBars());

            getWindow().getInsetsController().setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
        VoskVoiceManager.INSTANCE.initModel(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        LAppDelegate.getInstance().onStart(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        glSurfaceView.onResume();

        View decor = this.getWindow().getDecorView();
        decor.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    @Override
    protected void onPause() {
        super.onPause();

        glSurfaceView.onPause();
        LAppDelegate.getInstance().onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LAppDelegate.getInstance().onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LAppDelegate.getInstance().onDestroy();

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float pointX = event.getX();
        float pointY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                LAppDelegate.getInstance().onTouchBegan(pointX, pointY);
                break;
            case MotionEvent.ACTION_UP:
                LAppDelegate.getInstance().onTouchEnd(pointX, pointY);
                break;
            case MotionEvent.ACTION_MOVE:
                LAppDelegate.getInstance().onTouchMoved(pointX, pointY);
                break;
        }
        return super.onTouchEvent(event);
    }

    private GLSurfaceView glSurfaceView;
    private GLRenderer glRenderer;

    @Override
    public void onPartialResult(String hypothesis) {
        Log.i(TAG, "onPartialResult: " + hypothesis);
    }

    @Override
    public void onResult(String hypothesis) {
        Log.i(TAG, "onResult: " + hypothesis);
        resolveMessage(hypothesis);
    }

    private void resolveMessage(String hypothesis) {
        if (hypothesis.contains("天气")) {
            getWeather();
        }
    }

    @Override
    public void onFinalResult(String hypothesis) {
        Log.i(TAG, "onFinalResult: " + hypothesis);

    }

    @Override
    public void onError(Exception exception) {
        Log.i(TAG, "onError: " + exception);
    }

    @Override
    public void onTimeout() {
        Log.i(TAG, "onTimeout: ");
    }


    //先定位 然后获取位置信息，然后获取天气
    private void getWeather() {
        BaiduLocation.INSTANCE.startLoc(new BaiduLocation.MyLocationListener() {
            @Override
            public void onReceiveLocation(@NonNull BDLocation location) {
                super.onReceiveLocation(location);
                String locationId = location.getLocationID();
                Weather.INSTANCE.getGeoCity(locationId, "", Range.CN, 1, Lang.ZH_HANS, new QWeather.OnResultGeoListener() {
                    @Override
                    public void onError(Throwable throwable) {
                        Toast.makeText(App.getApplication(), "获取定位失败", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess(GeoBean geoBean) {
                        GeoBean.LocationBean loc = geoBean.getLocationBean().get(0);
                        Weather.INSTANCE.getWeather(loc.getId(), new QWeather.OnResultWeatherNowListener() {
                            @Override
                            public void onError(Throwable throwable) {
                                Toast.makeText(App.getApplication(), "获取天气失败", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onSuccess(WeatherNowBean WeatherNowBean) {
                                Toast.makeText(App.getApplication(), WeatherNowBean.getNow().getText(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });
    }
}
