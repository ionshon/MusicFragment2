package com.inu.andoid.musicfragment2.playback

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.inu.andoid.musicfragment2.R
import com.inu.andoid.musicfragment2.model.Music
import kotlinx.android.synthetic.main.activity_music.view.*

class MusicFragment : Fragment(), View.OnClickListener {

    private val args by navArgs<MusicFragmentArgs>()

    private lateinit var music : Music
    private var mediaPlayer = MediaPlayer()
    private lateinit var title : TextView
    private lateinit var album : ImageView
    private lateinit var previous : ImageView
    private lateinit var play: ImageView
    private lateinit var pause: ImageView
    private lateinit var next: ImageView
    private lateinit var seekBar: SeekBar

    private var progressUpdater = Thread(ProcessUpdate())

    private var position:Int = 0
    var isPlaying: Boolean   = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_music, container, false)
        previous = view.pre
        play = view.play
        pause = view.pause
        next = view.next
        title = view.title
        seekBar = view.seekbar
        album = view.album

//        var intent = intent
//        musicList = intent.getSerializableExtra("playlist") as ArrayList<Music>
//        position = intent.getIntExtra("position", 0)
        previous.setOnClickListener(this)
        play.setOnClickListener(this)
        pause.setOnClickListener(this)
        next.setOnClickListener(this)

        music = args.currentMusic
        playMusic(music)
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

        // Inflate the layout for this fragment
        return view
    }

    private fun playMusic(music: Music) {
        val uri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, music.id.toString())

        var flag = getCoverArtPath(music.albumId!!.toLong(), requireContext())
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
        mediaPlayer.setDataSource(requireContext(), uri)
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

    override fun onDestroy() {
        super.onDestroy()
        isPlaying = false
        mediaPlayer?.release()
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
        /*    R.id.pre -> {
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
            }*/
        }
    }
}