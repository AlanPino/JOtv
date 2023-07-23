package com.joeldev.iptvplayer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class MovieAdapter(var movies: MutableList<Video>, private val onMovieSelected: (Video) -> Unit) :
    RecyclerView.Adapter<MovieViewHolder>() {

    lateinit var movieSelected: Video

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return MovieViewHolder(layoutInflater.inflate(R.layout.movie_item, parent, false))
    }

    override fun getItemCount() = movies.size

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(movies[position])

        holder.itemView.setOnClickListener {
            onMovieSelected(movies[position])
        }
    }
}