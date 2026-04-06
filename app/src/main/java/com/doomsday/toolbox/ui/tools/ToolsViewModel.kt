package com.doomsday.toolbox.ui.tools

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doomsday.toolbox.sensor.GpsManager
import kotlinx.coroutines.launch
import java.util.Locale

data class GpsUiState(
    val latitude: String = "--",
    val longitude: String = "--",
    val altitude: String = "--",
    val accuracy: String = "--",
    val status: String = "等待定位…",
    val loading: Boolean = false
)

class ToolsViewModel(application: Application) : AndroidViewModel(application) {
    private val gpsManager = GpsManager(application)

    private val _gps = MutableLiveData(GpsUiState())
    val gps: LiveData<GpsUiState> = _gps

    private val _azimuth = MutableLiveData(0)
    val azimuth: LiveData<Int> = _azimuth

    fun updateAzimuth(value: Int) {
        _azimuth.value = value
    }

    fun refreshLocation() {
        viewModelScope.launch {
            _gps.value = _gps.value?.copy(
                loading = true,
                status = "正在搜索定位信号…"
            ) ?: GpsUiState(loading = true, status = "正在搜索定位信号…")

            val result = gpsManager.fetchLocation()
            _gps.value = if (result.location == null) {
                GpsUiState(
                    status = result.status,
                    loading = false
                )
            } else {
                GpsUiState(
                    latitude = formatCoordinate(result.location.latitude),
                    longitude = formatCoordinate(result.location.longitude),
                    altitude = if (result.location.hasAltitude()) {
                        String.format(Locale.US, "%.1f m", result.location.altitude)
                    } else {
                        "未知"
                    },
                    accuracy = String.format(Locale.US, "%.1f m", result.location.accuracy),
                    status = result.status,
                    loading = false
                )
            }
        }
    }

    private fun formatCoordinate(value: Double): String {
        return String.format(Locale.US, "%.6f", value)
    }
}
