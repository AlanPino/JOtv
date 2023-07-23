package com.joeldev.iptvplayer

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.joeldev.iptvplayer.databinding.ActivityMoviesBinding
import kotlin.concurrent.thread

class MoviesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMoviesBinding

    private lateinit var movies: MutableList<Video>
    private lateinit var moviesAdapter: MovieAdapter
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        binding = ActivityMoviesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        movies = Functions.loadVideosFromAssets("co.m3u", this)

        initUI()
        initListeners()
    }

    private fun initListeners() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Aquí puedes realizar alguna acción cuando se envía el texto de búsqueda
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                moviesAdapter.movies =
                    movies.filter { movie -> movie.name.contains(newText, ignoreCase = true) }
                        .toMutableList()
                moviesAdapter.notifyDataSetChanged()
                return true
            }
        })

        binding.tvMode.setOnClickListener {
            intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }
    }

    private fun initUI() {
        moviesAdapter = MovieAdapter(movies) { onMovieSelected(it) }
        binding.rvMovies.layoutManager = GridLayoutManager(this, 4)
        binding.rvMovies.adapter = moviesAdapter
        moviesAdapter.movieSelected = movies[0]

        categoryAdapter = CategoryAdapter(
            movies.flatMap { movie -> movie.category }.distinct()
                .toMutableList()
        ) { onCategorySelected(it) }
        binding.rvCategories.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvCategories.adapter = categoryAdapter

        binding.rvCategories.isVisible =
            movies.map { movie -> movie.category }.distinct().size >= 2
    }

    private fun onCategorySelected(category: String) {
        moviesAdapter.movies =
            movies.filter { movie -> movie.category.contains(category) }.toMutableList()
        moviesAdapter.notifyDataSetChanged()
    }

    private fun onMovieSelected(video: Video) {
        val intent = Intent(this, PlayMovieActivity::class.java)
        intent.putExtra("name", video.name)
        intent.putExtra("url", video.url)
        intent.putExtra("img", video.img)
        startActivity(intent)
    }
}