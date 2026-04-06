package com.doomsday.toolbox.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.roundToInt

class CompassManager(context: Context, private val onAzimuthChanged: (Int) -> Unit) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private var gravity = FloatArray(3)
    private var geomagnetic = FloatArray(3)

    fun start() {
        accelerometer?.also { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        magnetometer?.also { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> gravity = event.values.clone()
            Sensor.TYPE_MAGNETIC_FIELD -> geomagnetic = event.values.clone()
        }
        val rotation = FloatArray(9)
        val inclination = FloatArray(9)
        if (SensorManager.getRotationMatrix(rotation, inclination, gravity, geomagnetic)) {
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotation, orientation)
            val azimuth = ((Math.toDegrees(orientation[0].toDouble()) + 360) % 360).roundToInt()
            onAzimuthChanged(azimuth)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
