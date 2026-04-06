package com.doomsday.toolbox.ui.tools

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.doomsday.toolbox.databinding.ActivityToolsBinding
import com.doomsday.toolbox.sensor.CompassManager

class ToolsActivity : AppCompatActivity() {
    private val viewModel by viewModels<ToolsViewModel>()
    private lateinit var compassManager: CompassManager

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.refreshLocation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityToolsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonBack.setOnClickListener { finish() }
        compassManager = CompassManager(this) { viewModel.updateAzimuth(it) }
        binding.buttonRefreshGps.setOnClickListener { requestLocationIfNeeded() }

        viewModel.azimuth.observe(this) {
            binding.textAzimuth.text = "$it°"
            binding.compassNeedle.rotation = it.toFloat()
        }

        viewModel.gps.observe(this) {
            binding.textLatitude.text = it.latitude
            binding.textLongitude.text = it.longitude
            binding.textAltitude.text = it.altitude
            binding.textAccuracy.text = it.accuracy
            binding.textGpsStatus.text = it.status
            binding.buttonRefreshGps.isEnabled = !it.loading
        }
    }

    override fun onResume() {
        super.onResume()
        compassManager.start()
        requestLocationIfNeeded()
    }

    override fun onPause() {
        compassManager.stop()
        super.onPause()
    }

    private fun requestLocationIfNeeded() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.refreshLocation()
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}
