package com.doomsday.toolbox.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.doomsday.toolbox.databinding.ActivityPrivacyPolicyBinding

class PrivacyPolicyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonBack.setOnClickListener { finish() }
        binding.textPolicyContent.text = assets.open("privacy_policy_zh.txt").bufferedReader().use { it.readText() }
    }
}
