package com.joeldev.iptvplayer

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.joeldev.iptvplayer.databinding.MovieItemBinding
import com.squareup.picasso.Picasso

class MovieViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val binding = MovieItemBinding.bind(view)

    fun bind(movie: Video) {
        if (movie.img != "") {
            Picasso.get().load(movie.img).into(binding.imgMovie)
        }
        binding.txtMovie.text = movie.name
    }
}