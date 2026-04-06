package com.doomsday.toolbox.sos

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SosController(context: Context) {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
        cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
    }

    private var flashJob: Job? = null

    fun startFlashSos(scope: CoroutineScope) {
        stopFlash()
        if (cameraId == null) return
        flashJob = scope.launch(Dispatchers.IO) {
            while (true) {
                blink(".")
                blink(".")
                blink(".")
                blink("-")
                blink("-")
                blink("-")
                blink(".")
                blink(".")
                blink(".")
                delay(1200)
            }
        }
    }

    fun stopFlash() {
        flashJob?.cancel()
        flashJob = null
        cameraId?.let { cameraManager.setTorchMode(it, false) }
    }

    private suspend fun blink(symbol: String) {
        val id = cameraId ?: return
        cameraManager.setTorchMode(id, true)
        delay(if (symbol == ".") 250 else 700)
        cameraManager.setTorchMode(id, false)
        delay(250)
    }
}
