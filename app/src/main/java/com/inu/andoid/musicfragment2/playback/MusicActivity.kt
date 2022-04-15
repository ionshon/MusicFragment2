package com.inu.andoid.musicfragment2.playback

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.inu.andoid.musicfragment2.R
import com.inu.andoid.musicfragment2.model.Music

class MusicActivity : AppCompatActivity(), View.OnClickListener {

    private var musicList: ArrayList<Music>? = null
    private var mediaPlayer = MediaPlayer()

    private val title by lazy { findViewById<TextView>(R.id.title) }
    private val album by lazy { findViewById<ImageView>(R.id.album) }
    private val previous by lazy { findViewById<ImageView>(R.id.pre) }
    private val play by lazy { findViewById<ImageView>(R.id.play) }
    private val pause by lazy { findViewById<ImageView>(R.id.pause) }
    private val next by lazy { findViewById<ImageView>(R.id.next) }
    private val seekBar by lazy { findViewById<SeekBar>(R.id.seekbar) }

    private var progressUpdater = Thread(ProcessUpdate())

    private var position:Int = 0
    var isPlaying: Boolean   = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music)

        var intent = intent
        musicList = intent.getSerializableExtra("playlist") as ArrayList<Music>
        position = intent.getIntExtra("position", 0)

        setContentView(R.layout.activity_music)

        previous.setOnClickListener(this)
        play.setOnClickListener(this)
        pause.setOnClickListener(this)
        next.setOnClickListener(this)

        playMusic(musicList!![position])
        progressUpdater.start()

        seekBar!!.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                mediaPlayer.seekTo(seekBar!!.progress)

                if ((seekBar.progress > 0).and(play.visibility == View.GONE)) {
                    mediaPlayer.start()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                mediaPlayer.pause()
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
        })
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.play -> {
                pause.visibility = View.VISIBLE
                play.visibility = View.GONE
                mediaPlayer.seekTo(mediaPlayer.currentPosition)
                mediaPlayer.start()
            }
            R.id.pause -> {
                pause.visibility = View.GONE
                play.visibility = View.VISIBLE
                mediaPlayer.pause()
            }
            R.id.pre -> {
                if (position > 0) {
                    --position
                    playMusic(musicList!![position])
                    seekBar!!.setProgress(0)
                }
            }
            R.id.next -> {
                if (position < musicList!!.size) {
                    ++position
                    playMusic(musicList!![position])
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isPlaying = false
        mediaPlayer?.release()
    }

    private fun playMusic(music: Music) {
        val uri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, music.id.toString())

        var flag = getCoverArtPath(music.albumId!!.toLong(), application)
        var bitmap: Bitmap?

        if (flag != null) {
            bitmap = BitmapFactory.decodeFile(flag)
            album.setImageBitmap(bitmap)
        }
        else {
            Glide.with(this)
                    .load(music.albumUri)
                    .error(R.drawable.ic_all_inclusive_black_24dp)
                    .into(album)
        }

        seekBar!!.progress = 0
        title.text = "${music.artist} - ${music.title}"

        mediaPlayer.reset()
        mediaPlayer.setDataSource(this, uri)
        mediaPlayer.prepare()
        mediaPlayer.start()

        seekBar!!.max = mediaPlayer.duration

        if (mediaPlayer.isPlaying) {
            play.visibility = View.GONE
            pause.visibility = View.VISIBLE
        } else {
            play.visibility = View.VISIBLE
            pause.visibility = View.GONE
        }
    }

    private fun getCoverArtPath(albumId: Long, context: Context): String? {

        val cursor = context.contentResolver.query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Audio.Albums.ALBUM_ART),
                "${MediaStore.Audio.Albums._ID} = ?",
                arrayOf(albumId.toString()),
                null
        )

        var queryResult = cursor?.moveToFirst()
        var res:String? = null

        if (queryResult == true) {
            res = cursor?.getString(0)
        }
        cursor?.close()
        return res
    }

    inner class ProcessUpdate: Runnable {
        override fun run() {
            while (isPlaying) {
                try {
                    Thread.sleep(500)
                    if (mediaPlayer != null) {
                        seekBar.progress = mediaPlayer.currentPosition
                    }
                } catch (e: Exception) {}
            }
        }

    }
}
