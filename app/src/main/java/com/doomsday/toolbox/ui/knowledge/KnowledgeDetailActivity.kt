package com.doomsday.toolbox.ui.knowledge

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.doomsday.toolbox.databinding.ActivityKnowledgeDetailBinding

class KnowledgeDetailActivity : AppCompatActivity() {
    private val viewModel by viewModels<KnowledgeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityKnowledgeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonBack.setOnClickListener { finish() }
        val item = viewModel.getItem(intent.getStringExtra("id").orEmpty()) ?: return
        binding.textTitle.text = item.title
        binding.textCategory.text = item.category
        binding.textContent.text = item.content.joinToString("\n\n")
    }
}
