package com.doomsday.toolbox.ui.knowledge

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doomsday.toolbox.databinding.ItemKnowledgeBinding
import com.doomsday.toolbox.knowledge.KnowledgeItem

class KnowledgeAdapter(private val onClick: (KnowledgeItem) -> Unit) :
    RecyclerView.Adapter<KnowledgeAdapter.KnowledgeHolder>() {

    private val items = mutableListOf<KnowledgeItem>()

    fun submitList(data: List<KnowledgeItem>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KnowledgeHolder {
        return KnowledgeHolder(ItemKnowledgeBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: KnowledgeHolder, position: Int) {
        holder.bind(items[position], onClick)
    }

    override fun getItemCount(): Int = items.size

    class KnowledgeHolder(private val binding: ItemKnowledgeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: KnowledgeItem, onClick: (KnowledgeItem) -> Unit) {
            binding.textTitle.text = item.title
            binding.textCategory.text = item.category
            binding.textSummary.text = item.summary
            binding.root.setOnClickListener { onClick(item) }
        }
    }
}
