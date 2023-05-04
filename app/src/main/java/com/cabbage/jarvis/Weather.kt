package com.cabbage.jarvis

import android.util.Log
import com.google.gson.Gson
import com.qweather.sdk.bean.base.Code
import com.qweather.sdk.bean.base.Lang
import com.qweather.sdk.bean.base.Range
import com.qweather.sdk.bean.base.Unit
import com.qweather.sdk.bean.geo.GeoBean
import com.qweather.sdk.bean.weather.WeatherNowBean
import com.qweather.sdk.view.QWeather

object Weather {
    const val TAG = "Weather"

    //根据城市id查询天气
    fun getWeather(locationId: String = "101010300",listener: QWeather.OnResultWeatherNowListener?) {
        /**
         * Real-time weather data
         * @param location location to be queried
         * @param lang (optional) set multi-language, the default is simplified Chinese
         * @param unit (optional) set unit, metric (m) or imperial (i), the default is metric unit
         * @param listener network access result callback
         */
        QWeather.getWeatherNow(
            App.application,
            locationId,
            Lang.ZH_HANS,
            Unit.METRIC,
            object : QWeather.OnResultWeatherNowListener {
                override fun onError(e: Throwable) {
                    Log.i(TAG, "getWeather onError: $e")
                    listener?.onError(e)
                }

                override fun onSuccess(weatherBean: WeatherNowBean) {
                    Log.i(TAG, "getWeather onSuccess: " + Gson().toJson(weatherBean))
                    if (Code.OK.code.equals(weatherBean.code.code, true)) {
                        val now = weatherBean.now
                        listener?.onSuccess(weatherBean)
                    } else {
                        //Check the reason for the failure to return data here
                        val status = weatherBean.code.code
                        val code: Code = Code.toEnum(status)
                        Log.i(TAG, "failed code: $code")
                    }
                }
            })
    }

    //根据定位查询城市id信息
    fun getGeoCity(
        location: String,
        adm: String = "",
        range: Range = Range.CN,
        number: Int = 10,
        lang: Lang = Lang.ZH_HANS,
        listener: QWeather.OnResultGeoListener?
    ) {
        QWeather.getGeoCityLookup(
            App.application,
            location,
            adm,
            range,
            number,
            lang,
            object : QWeather.OnResultGeoListener {
                override fun onError(p0: Throwable?) {
                    Log.i(TAG, "onError:$p0")
                    listener?.onError(p0)
                }

                override fun onSuccess(p0: GeoBean?) {
                    Log.i(TAG, "onSuccess:$p0")
                    listener?.onSuccess(p0)
                }
            })
    }
}