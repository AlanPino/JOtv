package com.joeldev.iptvplayer

import android.content.Context
import android.os.AsyncTask
import android.util.TypedValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class Functions {
    companion object {
        private fun loadVideos(reader: BufferedReader): MutableList<Video> {

            var videos = mutableListOf<Video>()
            var line: String? = reader.readLine()

            while (line != null) {
                var name: String
                var category: List<String>
                var img: String
                var url: String

                if (line.startsWith("#EXTINF")) {
                    name = getName(line)
                    category = getCategory(line)
                    img = getImg(line)

                    while (!line!!.startsWith("http")) {
                        line = reader.readLine()
                    }

                    url = line

                    videos.add(Video(name, img, category, url))
                }

                line = reader.readLine()
            }

            reader.close()
            return videos
        }

        fun loadVideosFromAssets(fileName: String, context: Context): MutableList<Video> {
            val inputStream: InputStream = context.assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))

            return loadVideos(reader)
        }

        private fun getName(line: String): String {
            val nombreRegex = Regex("(?<=,).*")
            val nombreMatch = nombreRegex.find(line)
            return nombreMatch?.value?.trim()!!
        }

        private fun getCategory(line: String): List<String> {
            if (line.contains("group-title")) {
                val categoryRegex = Regex("(?<=group-title=\").*?(?=\")")
                val categoryMatch = categoryRegex.find(line)
                val categories = categoryMatch?.value?.trim()!!
                return categories.split(";")
            } else if (line.contains("tvg-group")) {
                val categoryRegex = Regex("(?<=tvg-group=\").*?(?=\")")
                val categoryMatch = categoryRegex.find(line)
                val categories = categoryMatch?.value?.trim()!!
                return categories.split(";")
            } else {
                return (listOf(""))
            }

        }

        private fun getImg(line: String): String {
            if (line.contains("tvg-logo")) {
                val logoRegex = Regex("(?<=tvg-logo=\").*?(?=\")")
                val logoMatch = logoRegex.find(line)
                val url = logoMatch?.value?.trim()!!

                if (url == "null") {
                    return ("")
                }
                return url
            } else {
                return ("")
            }
        }

        fun dpToPx(dp: Int, context: Context): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp.toFloat(),
                context.resources.displayMetrics
            ).toInt()
        }


    }

}