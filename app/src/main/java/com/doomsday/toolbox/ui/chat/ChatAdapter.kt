package com.doomsday.toolbox.ui.chat

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doomsday.toolbox.ai.ChatMessage
import com.doomsday.toolbox.databinding.ItemChatAiBinding
import com.doomsday.toolbox.databinding.ItemChatUserBinding
import java.io.File

class ChatAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = mutableListOf<ChatMessage>()

    fun submitList(messages: List<ChatMessage>) {
        items.clear()
        items.addAll(messages)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int = if (items[position].isUser) 1 else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 1) {
            UserHolder(ItemChatUserBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        } else {
            AiHolder(ItemChatAiBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is UserHolder -> {
                holder.binding.textMessage.text = item.text
                bindImage(holder.binding.imagePreview, item.imagePath)
            }
            is AiHolder -> {
                holder.binding.textMessage.text = item.text
                bindImage(holder.binding.imagePreview, item.imagePath)
            }
        }
    }

    private fun bindImage(view: android.widget.ImageView, imagePath: String?) {
        if (imagePath.isNullOrBlank()) {
            view.visibility = View.GONE
            view.setImageDrawable(null)
        } else {
            view.visibility = View.VISIBLE
            view.setImageURI(Uri.fromFile(File(imagePath)))
        }
    }

    override fun getItemCount(): Int = items.size

    class UserHolder(val binding: ItemChatUserBinding) : RecyclerView.ViewHolder(binding.root)
    class AiHolder(val binding: ItemChatAiBinding) : RecyclerView.ViewHolder(binding.root)
}
