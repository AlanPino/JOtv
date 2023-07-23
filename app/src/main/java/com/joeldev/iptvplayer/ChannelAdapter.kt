package com.joeldev.iptvplayer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class ChannelAdapter(
    var channels: MutableList<Video>,
    private val onChannelSelected: (Video) -> Unit
) : RecyclerView.Adapter<ChannelViewHolder>() {

    lateinit var channelSelected: Video

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ChannelViewHolder(layoutInflater.inflate(R.layout.channel_item, parent, false))
    }

    override fun getItemCount() = channels.size

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {

        holder.bind(channels[position])
        val cardView = holder.itemView.findViewById<CardView>(R.id.background)

        if (position == channels.indexOf(channelSelected)) {
            cardView.setCardBackgroundColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.lighten
                )
            )
        } else {
            cardView.setCardBackgroundColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.transparent
                )
            )
        }

        holder.itemView.setOnClickListener {
            onChannelSelected(channels[position])

            channelSelected = channels[position]
            notifyDataSetChanged()
        }
    }

}