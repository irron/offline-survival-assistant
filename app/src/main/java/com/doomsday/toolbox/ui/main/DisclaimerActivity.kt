package com.doomsday.toolbox.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.doomsday.toolbox.common.AppPrefs
import com.doomsday.toolbox.databinding.ActivityDisclaimerBinding

class DisclaimerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityDisclaimerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonContinue.setOnClickListener {
            if (!binding.checkAccept.isChecked) return@setOnClickListener
            AppPrefs(this).disclaimerAccepted = true
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.buttonPrivacyPolicy.setOnClickListener {
            startActivity(Intent(this, PrivacyPolicyActivity::class.java))
        }
    }
}
