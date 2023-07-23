package com.joeldev.iptvplayer

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.joeldev.iptvplayer.databinding.ChannelItemBinding
import com.squareup.picasso.Picasso

class ChannelViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val binding = ChannelItemBinding.bind(view)

    fun bind(channel: Video) {
        binding.txtChannel.text = channel.name

        if (channel.img.isNotBlank()) {
            Picasso.get().load(channel.img).into(binding.imgChannel)
        }
    }
}