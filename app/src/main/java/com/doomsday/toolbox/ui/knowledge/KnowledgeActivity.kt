package com.doomsday.toolbox.ui.knowledge

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.doomsday.toolbox.databinding.ActivityKnowledgeBinding

class KnowledgeActivity : AppCompatActivity() {
    private val viewModel by viewModels<KnowledgeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityKnowledgeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonBack.setOnClickListener { finish() }
        val adapter = KnowledgeAdapter { item ->
            startActivity(Intent(this, KnowledgeDetailActivity::class.java).putExtra("id", item.id))
        }
        binding.recyclerKnowledge.layoutManager = LinearLayoutManager(this)
        binding.recyclerKnowledge.adapter = adapter

        viewModel.items.observe(this) { adapter.submitList(it) }
    }
}
