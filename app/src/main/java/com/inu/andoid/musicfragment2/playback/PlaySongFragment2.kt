package com.inu.andoid.musicfragment2.playback

import android.app.ActionBar
import android.app.Activity
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.inu.andoid.musicfragment.utils.MusicProvider
import com.inu.andoid.musicfragment2.R
import com.inu.andoid.musicfragment2.model.Music
import com.inu.andoid.musicfragment2.utils.SongTimer
import com.inu.andoid.musicfragment2.utils.SongsManager
import kotlinx.android.synthetic.main.activity_play_song.*
import kotlinx.android.synthetic.main.fragment_play_song.view.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class PlaySongFragment2 : Fragment(),
    MediaPlayer.OnCompletionListener {

    private val args by navArgs<MusicFragmentArgs>()
    private lateinit var music : Music

    lateinit var mediaPlayer: MediaPlayer
    //    lateinit var songManager: SongsManager
    lateinit var songTimer: SongTimer
    lateinit var songTitle: String
    lateinit var view2: View
    var handler = Handler()
    var seekForwardTime = 5000
    var seekBackwardTime = 5000
    var currentSongIndex = 0
    var isShuffle = false
    var isRepeat = false
    var songList = ArrayList<HashMap<String, String>>()
    private var deviceMusic = mutableListOf<Music>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        view2 = inflater.inflate(R.layout.fragment_play_song, container, false)
        val toolbar = view?.toolbar

        music = args.currentMusic

        val activity = activity as AppCompatActivity?

//        activity!!.setSupportActionBar(toolbar)
//        assert(activity!!.supportActionBar != null)
//        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        activity.supportActionBar?.setDisplayShowTitleEnabled(false)
//        val actionBar: ActionBar = activity!!.setSupportActionBar()

        view2.tvJudulLagu.isSelected = true

        mediaPlayer = MediaPlayer()
        songTimer = SongTimer()

        view2.seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                handler.removeCallbacks(runnable)
                val totalDuration = mediaPlayer.duration
                val currentPosition = songTimer.progressToTimer(view2.seekBar.progress, totalDuration)
                mediaPlayer.seekTo(currentPosition)

                //run seekbar
                updateSeekBar()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                handler.removeCallbacks(runnable)
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
        })
        mediaPlayer.setOnCompletionListener(this)
        deviceMusic.addAll(MusicProvider.getMusicList(requireContext()))
        songList.addAll(SongsManager.getPlayList())
        Log.d("song: ", "${deviceMusic.size}, ${songList.size}")

        Log.d("songIndex: ", "${songList[10]}")

        //get data song
        getPlaySong(11)

        //methods button action
        getButtonSong()
        return view2
    }

    private fun getButtonSong() {
        view2.imagePlay.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                view2.imagePlay.setBackgroundResource(R.drawable.ic_play)
            } else {
                mediaPlayer.start()
                view2.visualizerView.getPathMedia(mediaPlayer)
                view2.imagePlay.setBackgroundResource(R.drawable.ic_pause)
            }
        }

        view2.imageNext.setOnClickListener {
            currentSongIndex = currentSongIndex + 1
            if (currentSongIndex < songList.size) {
                mediaPlayer.stop()
                view2.imagePlay.setBackgroundResource(R.drawable.ic_play)
                getPlaySong(currentSongIndex)
            } else {
                currentSongIndex = currentSongIndex - 1
            }
        }

        view2.imagePrev.setOnClickListener {
            currentSongIndex = currentSongIndex - 1
            if (currentSongIndex >= 0) {
                mediaPlayer.stop()
                view2.imagePlay.setBackgroundResource(R.drawable.ic_play)
                getPlaySong(currentSongIndex)
            } else {
                currentSongIndex = currentSongIndex + 1
            }
        }

        view2.imageForward.setOnClickListener {
            val currentPosition = mediaPlayer.currentPosition
            if (currentPosition + seekForwardTime <= mediaPlayer.duration) {
                mediaPlayer.seekTo(currentPosition + seekForwardTime)
            } else {
                mediaPlayer.seekTo(mediaPlayer.duration)
            }
        }

        view2.imageRewind.setOnClickListener {
            val currentPosition = mediaPlayer.currentPosition
            if (currentPosition - seekBackwardTime >= 0) {
                mediaPlayer.seekTo(currentPosition - seekBackwardTime)
            } else {
                mediaPlayer.seekTo(0)
            }
        }

        view2.imageRepeat.setOnClickListener {
            if (isRepeat) {
                isRepeat = false
                Toast.makeText(requireContext(), "노래 반복", Toast.LENGTH_SHORT).show()
                view2.imageRepeat.setImageResource(R.drawable.btn_repeat)
            } else {
                isRepeat = true
                Toast.makeText(requireContext(), "반복 끄기", Toast.LENGTH_SHORT).show()
                isShuffle = false
                view2.imageRepeat.setImageResource(R.drawable.btn_repeat_focused)
                view2.imageShuffle.setImageResource(R.drawable.btn_shuffle)
            }
        }

        view2.imageShuffle.setOnClickListener {
            if (isShuffle) {
                isShuffle = false
                Toast.makeText(requireContext(), "노래 셔플, 켜기", Toast.LENGTH_SHORT).show()
                view2.imageShuffle.setImageResource(R.drawable.btn_shuffle)
            } else {
                isShuffle = true
                Toast.makeText(requireContext(), "노래 셔플, 끄기", Toast.LENGTH_SHORT).show()
                isRepeat = false
                view2.imageShuffle.setImageResource(R.drawable.btn_shuffle_focused)
                view2.imageRepeat.setImageResource(R.drawable.btn_repeat)
            }
        }
    }

    private fun getPlaySong(songIndex: Int) {
        val uri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, music.id.toString())

        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(songList[songIndex]["songPath"])
            mediaPlayer.prepare()
            Log.d("songIndex2: ", "${songList[songIndex]}")
            songTitle = songList[songIndex]["songTitle"]?.replace("_", " ").toString()
            view2.tvJudulLagu.text = songTitle
            view2.seekBar.progress = 0
            view2.seekBar.max = 100

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun updateSeekBar() {
        handler.postDelayed(runnable, 100)
    }
    private val runnable: Runnable = object : Runnable {
        override fun run() {
            val totalDuration = mediaPlayer.duration.toLong()
            val currentDuration = mediaPlayer.currentPosition.toLong()
            view2.tvTotalDuration.text = "" + songTimer.milliSecondsToTimer(totalDuration)
            view2.tvCurrentDuration.text = "" + songTimer.milliSecondsToTimer(currentDuration)
            val progress = songTimer.getProgressPercentage(currentDuration, totalDuration)
            view2.seekBar.progress = progress
            handler.postDelayed(this, 100)
        }
    }

    override fun onCompletion(p0: MediaPlayer?) {
        if (isRepeat) {
            getPlaySong(currentSongIndex)
        } else if (isShuffle) {
            val rand = Random()
            currentSongIndex = rand.nextInt(songList.size - 1 - 0 + 1) + 0
            getPlaySong(currentSongIndex)
        } else {
            currentSongIndex = if (currentSongIndex < songList.size - 1) {
                getPlaySong(currentSongIndex + 1)
                currentSongIndex + 1
            } else {
                getPlaySong(0)
                0.also { currentSongIndex = it }
            }
        }
    }

    companion object {
        fun setWindowFlag(activity: Activity, bits: Int, on: Boolean) {
            val window = activity.window
            val layoutParams = window.attributes
            if (on) {
                layoutParams.flags = layoutParams.flags or bits
            } else {
                layoutParams.flags = layoutParams.flags and bits.inv()
            }
            window.attributes = layoutParams
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
//            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

}

