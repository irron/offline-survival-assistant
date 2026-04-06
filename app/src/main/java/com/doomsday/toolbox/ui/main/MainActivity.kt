package com.doomsday.toolbox.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.doomsday.toolbox.common.AppPrefs
import com.doomsday.toolbox.databinding.ActivityMainBinding
import com.doomsday.toolbox.ui.chat.ChatActivity
import com.doomsday.toolbox.ui.knowledge.KnowledgeActivity
import com.doomsday.toolbox.ui.model.ModelManagerActivity
import com.doomsday.toolbox.ui.sos.SosActivity
import com.doomsday.toolbox.ui.tools.ToolsActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!AppPrefs(this).disclaimerAccepted) {
            startActivity(Intent(this, DisclaimerActivity::class.java))
            finish()
            return
        }

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cardAssistant.setOnClickListener { startActivity(Intent(this, ChatActivity::class.java)) }
        binding.cardKnowledge.setOnClickListener { startActivity(Intent(this, KnowledgeActivity::class.java)) }
        binding.cardSos.setOnClickListener { startActivity(Intent(this, SosActivity::class.java)) }
        binding.cardTools.setOnClickListener { startActivity(Intent(this, ToolsActivity::class.java)) }
        binding.cardModel.setOnClickListener { startActivity(Intent(this, ModelManagerActivity::class.java)) }
    }
}
