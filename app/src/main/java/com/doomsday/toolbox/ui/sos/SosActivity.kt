package com.doomsday.toolbox.ui.sos

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.doomsday.toolbox.databinding.ActivitySosBinding
import com.doomsday.toolbox.sos.SosController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SosActivity : AppCompatActivity() {
    private lateinit var controller: SosController
    private var screenJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        controller = SosController(this)
        binding.buttonBack.setOnClickListener { finish() }
        binding.buttonFlash.setOnClickListener { controller.startFlashSos(lifecycleScope) }
        binding.buttonStopFlash.setOnClickListener { controller.stopFlash() }
        binding.buttonScreen.setOnClickListener {
            screenJob?.cancel()
            screenJob = lifecycleScope.launch {
                while (true) {
                    binding.flashOverlay.visibility = View.VISIBLE
                    delay(250)
                    binding.flashOverlay.visibility = View.INVISIBLE
                    delay(250)
                }
            }
        }
        binding.buttonStopScreen.setOnClickListener {
            screenJob?.cancel()
            binding.flashOverlay.visibility = View.INVISIBLE
        }
    }

    override fun onDestroy() {
        controller.stopFlash()
        screenJob?.cancel()
        super.onDestroy()
    }
}
