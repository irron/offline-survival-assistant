package com.doomsday.toolbox.sensor

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class GpsFetchResult(
    val location: Location? = null,
    val status: String
)

class GpsManager(private val context: Context) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
    }

    fun isLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    suspend fun fetchLocation(timeoutMs: Long = 8000L): GpsFetchResult {
        if (!hasPermission()) {
            return GpsFetchResult(status = "未授予定位权限，请允许 GPS 权限后重试。")
        }
        if (!isLocationEnabled()) {
            return GpsFetchResult(status = "系统定位服务未开启，请先打开手机定位开关。")
        }

        val lastKnown = getLastKnownLocation()
        if (lastKnown != null) {
            return GpsFetchResult(
                location = lastKnown,
                status = "已读取最近一次定位结果。若位置不准确，可在室外再次刷新。"
            )
        }

        return suspendCancellableCoroutine { continuation ->
            val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
                .filter(locationManager::isProviderEnabled)

            if (providers.isEmpty()) {
                continuation.resume(GpsFetchResult(status = "当前没有可用的定位提供器。"))
                return@suspendCancellableCoroutine
            }

            val mainHandler = Handler(Looper.getMainLooper())
            var resumed = false
            lateinit var timeoutRunnable: Runnable

            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    if (resumed) return
                    resumed = true
                    mainHandler.removeCallbacks(timeoutRunnable)
                    runCatching { locationManager.removeUpdates(this) }
                    continuation.resume(
                        GpsFetchResult(
                            location = location,
                            status = "已获取新的定位结果。"
                        )
                    )
                }
            }

            timeoutRunnable = Runnable {
                if (resumed) return@Runnable
                resumed = true
                runCatching { locationManager.removeUpdates(listener) }
                continuation.resume(
                    GpsFetchResult(
                        status = "暂时未获取到定位，室内、地下或卫星信号较弱时常会这样，建议到室外空旷处再试。"
                    )
                )
            }

            providers.forEach { provider ->
                runCatching {
                    locationManager.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
                }
            }

            mainHandler.postDelayed(timeoutRunnable, timeoutMs)
            continuation.invokeOnCancellation {
                mainHandler.removeCallbacks(timeoutRunnable)
                runCatching { locationManager.removeUpdates(listener) }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation(): Location? {
        val providers = locationManager.getProviders(true)
        return providers.mapNotNull { provider ->
            runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull()
        }.maxByOrNull { it.time }
    }
}
