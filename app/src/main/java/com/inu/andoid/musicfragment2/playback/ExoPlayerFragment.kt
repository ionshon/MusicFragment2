package com.inu.andoid.musicfragment2.playback

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.util.Util
import com.inu.andoid.musicfragment2.R
import com.inu.andoid.musicfragment2.databinding.ActivityMainBinding
import com.inu.andoid.musicfragment2.model.DeviceMusic
import com.inu.andoid.musicfragment2.model.Music
import com.inu.andoid.musicfragment2.utils.SongTimer
import com.inu.andoid.musicfragment2.utils.SongsManager
import kotlinx.android.synthetic.main.activity_play_song.*
import kotlinx.android.synthetic.main.fragment_exo_player.view.*
import kotlinx.android.synthetic.main.fragment_play_song.view.*
import kotlinx.android.synthetic.main.fragment_play_song.view.imageForward
import kotlinx.android.synthetic.main.fragment_play_song.view.imageNext
import kotlinx.android.synthetic.main.fragment_play_song.view.imagePlay
import kotlinx.android.synthetic.main.fragment_play_song.view.imagePrev
import kotlinx.android.synthetic.main.fragment_play_song.view.imageRepeat
import kotlinx.android.synthetic.main.fragment_play_song.view.imageRewind
import kotlinx.android.synthetic.main.fragment_play_song.view.imageShuffle
import kotlinx.android.synthetic.main.fragment_play_song.view.seekBar
import kotlinx.android.synthetic.main.fragment_play_song.view.tvCurrentDuration
import kotlinx.android.synthetic.main.fragment_play_song.view.tvJudulLagu
import kotlinx.android.synthetic.main.fragment_play_song.view.tvTotalDuration
import kotlinx.android.synthetic.main.fragment_play_song.view.visualizerView
import java.io.IOException
import java.util.*

class ExoPlayerFragment : Fragment(),
    MediaPlayer.OnCompletionListener {

    private val args by navArgs<MusicFragmentArgs>()
    private lateinit var music : Music

    // exo
    var player: ExoPlayer? = null
    val viewBinding by lazy(LazyThreadSafetyMode.NONE){
        ActivityMainBinding.inflate(layoutInflater)
    }
    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition = 0L


    lateinit var mediaPlayer: MediaPlayer
    //    lateinit var songManager: SongsManager
    lateinit var songTimer: SongTimer
    lateinit var songTitle: String
    lateinit var view2: View
    private var progressUpdater = Thread(ProcessUpdate())
    var isPlaying: Boolean   = true

    var handler = Handler()
    var seekForwardTime = 5000
    var seekBackwardTime = 5000
    var currentSongIndex = 0
    var isShuffle = false
    var isRepeat = false
    var songList = ArrayList<HashMap<String, String>>()
    private var musicList = mutableListOf<Music>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        view2 = inflater.inflate(R.layout.fragment_exo_player, container, false)

        music = args.currentMusic


        mediaPlayer = MediaPlayer()
        songTimer = SongTimer()

        view2.seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                mediaPlayer.seekTo(seekBar!!.progress)

                if ((seekBar.progress > 0).and(view2.imagePlay.visibility == View.GONE)) {
                    mediaPlayer.start()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
//                handler.removeCallbacks(runnable)
                mediaPlayer.pause()
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
        })

        mediaPlayer.setOnPreparedListener {
            // Do something. For example: playButton.setEnabled(true);
            view2.imagePlay.setBackgroundResource(R.drawable.ic_pause)
            view2.visualizerView.getPathMedia(mediaPlayer)
            onPrepared(mediaPlayer)

        }

        mediaPlayer.setOnCompletionListener(this)
        musicList = DeviceMusic.musicsList //.addAll(MusicProvider.getMusicList(requireContext()))
        songList.addAll(SongsManager.getPlayList())



        Log.d("song index: ", "${songList[1]["songPath"]}")

        Log.d("songFrag song args: ", "${args.currentMusic.id}, ${args.currentMusic.title}")

        if (args.currentMusic in musicList){
            Log.d("ddddd true: ", "${musicList.indexOf(args.currentMusic)}")
        } else Log.d("ddddd false: ", "${musicList.indexOf(args.currentMusic)}")

        //get data song
//        getPlaySong(musicList.indexOf(args.currentMusic))
//
//        progressUpdater.start()
//        //methods button action
//        getButtonSong()

        if (Util.SDK_INT >= 24) {
            initializePlayer()
        }
        return view2
    }
    private fun initializePlayer() {
        player = ExoPlayer.Builder(requireContext())
            .build()
            .also { exoPlayer ->
                val mediaItem = MediaItem.fromUri("file:///storage/emulated/0/Music/(01's)김상민 - You.mp3")
                Log.d("mediaItem url:", "$mediaItem")
                exoPlayer.setMediaItem(mediaItem)
                view2.videoView.player = exoPlayer

                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.seekTo(currentWindow, playbackPosition)
                exoPlayer.prepare()
            }
    }

    private fun getPlaySong(songIndex: Int) {
        val uri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, music.id.toString())
        view2.seekBar.progress = 0
        var flag = getCoverArtPath(musicList[songIndex].albumId, requireContext())
        var bitmap: Bitmap?

       /* if (flag != null) {
            bitmap = BitmapFactory.decodeFile(flag)
            view2.imageCover.setImageBitmap(bitmap)
        }
        else {
            Glide.with(this)
                .load(musicList[songIndex].albumUri)
                .error(R.drawable.ic_all_inclusive_black_24dp)
                .into(view2.imageCover)
        }*/
        Log.d("getPlaySong: ", "index: $songIndex, id: ${music.id}, ${musicList[songIndex].title}")
        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(musicList[songIndex].path)
            mediaPlayer.prepare()
            songTitle = musicList[songIndex].title?.replace("_", " ").toString()
            view2.tvJudulLagu.text = songTitle
            view2.seekBar.progress = 0
            view2.seekBar.max = 100


        } catch (e: IOException) {
            e.printStackTrace()
        }

        view2.seekBar.max = mediaPlayer.duration

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
                    val totalDuration = mediaPlayer.duration.toLong()
                    val currentDuration = mediaPlayer.currentPosition.toLong()
                    view2.tvTotalDuration.text = "" + songTimer.milliSecondsToTimer(totalDuration)
                    view2.tvCurrentDuration.text = "" + songTimer.milliSecondsToTimer(currentDuration)
                    Thread.sleep(500)
                    if (mediaPlayer != null) {
                        seekBar.progress = mediaPlayer.currentPosition
                    }
                } catch (e: Exception) {}
            }
        }

    }


    private fun getButtonSong() {

        view2.imagePlay.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                view2.imagePlay.setBackgroundResource(R.drawable.ic_play)
            } else {
                view2.visualizerView.getPathMedia(mediaPlayer)
                mediaPlayer.start()
                view2.imagePlay.setBackgroundResource(R.drawable.ic_pause)
            }
        }

        view2.imageNext.setOnClickListener {
            if (currentSongIndex >= musicList.size-1) {
                currentSongIndex = 0
                Log.d("next index:"," ${musicList.size}, $currentSongIndex")

            } else {
                currentSongIndex += 1
                Log.d("next index:"," ${musicList.size}, $currentSongIndex")

            }
            mediaPlayer.stop()
            view2.imagePlay.setBackgroundResource(R.drawable.ic_play)
            getPlaySong(currentSongIndex)

        }

        view2.imagePrev.setOnClickListener {
            if (currentSongIndex == 0){
                currentSongIndex = musicList.size-1
            } else {
                currentSongIndex -= 1
            }

            mediaPlayer.stop()
            view2.imagePlay.setBackgroundResource(R.drawable.ic_play)
            getPlaySong(currentSongIndex)
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
    private fun onPrepared(player: MediaPlayer) {
        player.start()
    }
    private fun updateSeekBar() {
        handler.postDelayed(runnable, 100)
    }
    private val runnable: Runnable = object : Runnable {
        override fun run() {
            val totalDuration = mediaPlayer.duration
            val currentDuration = mediaPlayer.currentPosition
            view2.tvTotalDuration.text = "" + songTimer.milliSecondsToTimer(totalDuration.toLong())
            view2.tvCurrentDuration.text = "" + songTimer.milliSecondsToTimer(currentDuration.toLong())
            val progress = songTimer.getProgressPercentage(currentDuration.toLong(),
                totalDuration.toLong()
            )
            view2.seekBar.progress = progress
            handler.postDelayed(this, 50)
        }
    }

    override fun onCompletion(p0: MediaPlayer?) {
        if (isRepeat) {
            getPlaySong(currentSongIndex)
        } else if (isShuffle) {
            val rand = Random()
            currentSongIndex = rand.nextInt(musicList.size - 1 - 0 + 1) + 0
            getPlaySong(currentSongIndex)
        } else {
            currentSongIndex = if (currentSongIndex < musicList.size - 1) {
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
        isPlaying = false
        mediaPlayer.release()
//        musicList = mutableListOf<Music>()
        Log.d("생명주기:", "onDestroy")
    }
}

