package com.joeldev.iptvplayer

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.joeldev.iptvplayer.databinding.ActivityPlayMovieBinding
import com.squareup.picasso.Picasso
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer

class PlayMovieActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayMovieBinding

    private lateinit var libVLC: LibVLC
    private lateinit var mediaPlayer: MediaPlayer

    private var isSeekBarDragging = false
    private var isInFullscreen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayMovieBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val name = intent.getStringExtra("name")!!
        val url = intent.getStringExtra("url")!!
        val img = intent.getStringExtra("img")!!

        binding.txtMovie.text = name
        Picasso.get().load(img).into(binding.imgMovie)

        createPlayer(url)
        initUI()
        initListeners()
    }

    private fun initUI() {
        val handler = Handler()
        val updateSeekBar = object : Runnable {
            override fun run() {
                if (!isSeekBarDragging) {
                    val currentPosition = mediaPlayer.time
                    val duration = mediaPlayer.length
                    binding.movieProgress.max = duration.toInt()
                    binding.movieProgress.progress = currentPosition.toInt()
                    handler.postDelayed(this, 100)
                }
            }
        }
        handler.postDelayed(updateSeekBar, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.stop()
    }

    private fun initListeners() {
        val handler = Handler()
        var runnable: Runnable? = null

        binding.btnFullscreen.setOnClickListener {
            inFullscreen()
        }

        binding.btnOutFullscreen.setOnClickListener {
            outFullscreen()
        }

        binding.vdMovie.setOnClickListener {
            if (!isInFullscreen) {
                binding.btnFullscreen.isVisible = binding.btnFullscreen.isVisible != true
            } else {
                binding.btnOutFullscreen.isVisible = binding.btnOutFullscreen.isVisible != true
            }

            if (mediaPlayer.isPlaying) {
                binding.btnPause.isVisible = binding.btnPause.isVisible != true
            } else {
                binding.btnPlay.isVisible = binding.btnPlay.isVisible != true
            }

            if (mediaPlayer.volume == 0) {
                binding.btnVolumeOff.isVisible = binding.btnVolumeOff.isVisible != true
            } else {
                binding.btnVolumeOn.isVisible = binding.btnVolumeOn.isVisible != true
            }

            binding.darken.isVisible = binding.darken.isVisible != true
            binding.movieProgress.isVisible = binding.movieProgress.isVisible != true

            runnable?.let { handler.removeCallbacks(it) }

            runnable = Runnable {
                binding.darken.isVisible = false
                binding.btnFullscreen.isVisible = false
                binding.btnOutFullscreen.isVisible = false
                binding.btnPause.isVisible = false
                binding.btnPlay.isVisible = false
                binding.movieProgress.isVisible = false
                binding.btnVolumeOn.isVisible = false
                binding.btnVolumeOff.isVisible = false
            }
            handler.postDelayed(runnable!!, 3000)
        }

        binding.btnVolumeOn.setOnClickListener {
            binding.btnVolumeOff.isVisible = true
            binding.btnVolumeOn.isVisible = false
            mediaPlayer.volume = 0

            runnable?.let { handler.removeCallbacks(it) }
            handler.postDelayed(runnable!!, 3000)
        }

        binding.btnVolumeOff.setOnClickListener {
            binding.btnVolumeOff.isVisible = false
            binding.btnVolumeOn.isVisible = true
            mediaPlayer.volume = 100

            runnable?.let { handler.removeCallbacks(it) }
            handler.postDelayed(runnable!!, 3000)
        }

        mediaPlayer.setEventListener { event ->
            when (event.type) {
                MediaPlayer.Event.Buffering -> {
                    binding.loading.isVisible = true
                }

                else -> {
                    binding.loading.isVisible = false
                }
            }
        }

        binding.btnPause.setOnClickListener {
            binding.btnPlay.isVisible = true
            binding.btnPause.isVisible = false
            mediaPlayer.pause()
            runnable?.let { handler.removeCallbacks(it) }
            handler.postDelayed(runnable!!, 3000)
        }

        binding.btnPlay.setOnClickListener {
            binding.btnPlay.isVisible = false
            binding.btnPause.isVisible = true
            mediaPlayer.play()
            runnable?.let { handler.removeCallbacks(it) }
            handler.postDelayed(runnable!!, 3000)
        }

        binding.movieProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer.time = progress.toLong()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isSeekBarDragging = true
                runnable?.let { handler.removeCallbacks(it) }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isSeekBarDragging = false
                handler.postDelayed(runnable!!, 3000)
            }
        })
    }

    private fun outFullscreen() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val layoutParams = binding.vdBox.layoutParams
        layoutParams.height = Functions.dpToPx(200, this)

        binding.actionBar.setPadding(
            Functions.dpToPx(20, this),
            Functions.dpToPx(20, this),
            Functions.dpToPx(20, this),
            Functions.dpToPx(15, this)
        )

        binding.btnOutFullscreen.isVisible = false
        binding.btnFullscreen.isVisible = true

        binding.actionBar.isVisible = true

        isInFullscreen = false
    }

    private fun inFullscreen() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )

        val layoutParams = binding.vdBox.layoutParams
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        binding.btnFullscreen.isVisible = false
        binding.btnOutFullscreen.isVisible = true

        binding.actionBar.isVisible = false

        isInFullscreen = true
    }

    private fun createPlayer(url: String) {
        libVLC = LibVLC(this)
        mediaPlayer = MediaPlayer(libVLC)
        mediaPlayer.attachViews(binding.vdMovie, null, false, false)
        val media = Media(libVLC, Uri.parse(url))
        mediaPlayer.media = media
        mediaPlayer.play()
    }

    override fun onBackPressed() {
        if (isInFullscreen) {
            outFullscreen()
        } else {
            super.onBackPressed()
        }
    }
}