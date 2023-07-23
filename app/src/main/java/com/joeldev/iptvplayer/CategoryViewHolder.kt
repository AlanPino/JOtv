package com.joeldev.iptvplayer

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.joeldev.iptvplayer.databinding.CategoryItemBinding
import com.joeldev.iptvplayer.databinding.ChannelItemBinding

class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val binding = CategoryItemBinding.bind(view)

    fun bind(category: String) {
        binding.txtCategory.text = category
    }
}