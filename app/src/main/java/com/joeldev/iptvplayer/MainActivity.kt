package com.joeldev.iptvplayer


import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.recyclerview.widget.LinearLayoutManager
import com.joeldev.iptvplayer.Functions.Companion.dpToPx
import com.joeldev.iptvplayer.Functions.Companion.loadVideosFromAssets
import com.joeldev.iptvplayer.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var channels = mutableListOf<Video>()
    private lateinit var channelAdapter: ChannelAdapter

    private lateinit var categoryAdapter: CategoryAdapter

    private lateinit var libVLC: LibVLC
    private lateinit var mediaPlayer: MediaPlayer

    private var lists = mutableListOf("fuente 1", "fuente 2")

    private lateinit var popupMenu: PopupMenu

    private var isInFullscreen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        channels = loadVideosFromAssets("fuente 1.m3u", this)

        initUI()
        createPlayer()
        initListeners()

        binding.rvCategories.isVisible =
            channels.map { channel -> channel.category }.distinct().size >= 2
    }

    private fun initListeners() {
        binding.btnFullscreen.setOnClickListener {
            inFullscreen()
        }

        binding.btnOutFullscreen.setOnClickListener {
            outFullscreen()
        }

        val handler = Handler()
        var runnable: Runnable? = null

        binding.vdChannel.setOnClickListener {
            if (!isInFullscreen) {
                binding.btnFullscreen.isVisible = binding.btnFullscreen.isVisible != true
            } else {
                binding.btnOutFullscreen.isVisible = binding.btnOutFullscreen.isVisible != true
            }

            if (mediaPlayer.volume == 0) {
                binding.btnVolumeOff.isVisible = binding.btnVolumeOff.isVisible != true
            } else {
                binding.btnVolumeOn.isVisible = binding.btnVolumeOn.isVisible != true
            }

            binding.darken.isVisible = binding.darken.isVisible != true


            runnable?.let { handler.removeCallbacks(it) }

            runnable = Runnable {
                binding.darken.isVisible = false
                binding.btnFullscreen.isVisible = false
                binding.btnOutFullscreen.isVisible = false
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

        binding.btnChangeList.setOnClickListener {
            popupMenu.show()
        }

        popupMenu.setOnMenuItemClickListener { menuItem ->
            val selected = menuItem.title.toString()
            binding.btnChangeList.text = selected
            changeList(selected, this)
            true
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

        binding.movieMode.setOnClickListener {
            intent = Intent(this, MoviesActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
            mediaPlayer.stop()
            mediaPlayer.release()
        }
    }

    private fun outFullscreen() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val layoutParams = binding.vdBox.layoutParams
        layoutParams.height = dpToPx(200, this)

        binding.actionBar.setPadding(
            dpToPx(20, this),
            dpToPx(20, this),
            dpToPx(20, this),
            dpToPx(15, this)
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

        binding.vdBox.setPadding(0)

        binding.btnFullscreen.isVisible = false
        binding.btnOutFullscreen.isVisible = true

        binding.actionBar.isVisible = false

        isInFullscreen = true
    }

    private fun changeList(fileName: String, context: Context) {
        channels.clear()
        channels = loadVideosFromAssets("${fileName}.m3u", context)
        channelAdapter.channels = channels
        categoryAdapter.categories = channels.flatMap { channel -> channel.category }.distinct()
            .toMutableList()
        channelAdapter.notifyDataSetChanged()
        categoryAdapter.notifyDataSetChanged()
    }

    private fun initUI() {
        channelAdapter = ChannelAdapter(channels) { onChannelSelected(it) }
        binding.rvChannels.layoutManager = LinearLayoutManager(this)
        binding.rvChannels.adapter = channelAdapter
        channelAdapter.channelSelected = channels[0]

        categoryAdapter = CategoryAdapter(
            channels.flatMap { channel -> channel.category }.distinct()
                .toMutableList()
        ) { onCategorySelected(it) }
        binding.rvCategories.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvCategories.adapter = categoryAdapter
        categoryAdapter.categorySelected = ""

        popupMenu = PopupMenu(this, binding.btnChangeList)

        for (list in lists) {
            popupMenu.menu.add(list)
        }
    }

    private fun createPlayer() {
        libVLC = LibVLC(this)
        mediaPlayer = MediaPlayer(libVLC)
        mediaPlayer.attachViews(binding.vdChannel, null, false, false)
        val media = Media(libVLC, Uri.parse(channels[0].url))
        mediaPlayer.media = media
        mediaPlayer.play()
    }

    private fun putVideo(channel: Video) {
        val media = Media(libVLC, Uri.parse(channel.url))
        mediaPlayer.media = media
        mediaPlayer.play()
    }

    private fun onChannelSelected(channel: Video) {
        if (channel != channelAdapter.channelSelected) {
            CoroutineScope(Dispatchers.IO).launch {
                putVideo(channel)
            }
            channelAdapter.channelSelected = channel
        }
    }

    private fun onCategorySelected(category: String) {
        channelAdapter.channels =
            channels.filter { channel -> channel.category.contains(category) }.toMutableList()
        channelAdapter.notifyDataSetChanged()
        categoryAdapter.categorySelected = category
    }

    override fun onBackPressed() {
        if (isInFullscreen) {
            outFullscreen()
        } else {
            super.onBackPressed()
        }
    }
}
