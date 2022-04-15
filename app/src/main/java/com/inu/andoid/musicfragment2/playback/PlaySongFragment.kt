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
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.inu.andoid.musicfragment2.R
import com.inu.andoid.musicfragment2.model.DeviceMusic
import com.inu.andoid.musicfragment2.model.Music
import com.inu.andoid.musicfragment2.utils.SongTimer
import com.inu.andoid.musicfragment2.utils.SongsManager
import kotlinx.android.synthetic.main.activity_play_song.*
import kotlinx.android.synthetic.main.fragment_play_song.view.*
import java.io.IOException
import java.util.*


class PlaySongFragment : Fragment(),
    MediaPlayer.OnCompletionListener {

    private val args by navArgs<MusicFragmentArgs>()
    private lateinit var music : Music

    lateinit var mediaPlayer: MediaPlayer
//    lateinit var songManager: SongsManager
    lateinit var songTimer: SongTimer
    lateinit var songTitle: String
    lateinit var view2: View
//    private val stopThread: StopThread = TODO()
    private var progressUpdater = Thread(ProcessUpdate())
    var isPlaying: Boolean   = true

    var seekForwardTime = 5000
    var seekBackwardTime = 5000
    var currentSongIndex = 0
    var isShuffle = false
    var isRepeat = false
    var songList = ArrayList<HashMap<String, String>>()
    private var musicList = mutableListOf<Music>()
    var duration = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        view2 = inflater.inflate(R.layout.fragment_play_song, container, false)
        val toolbar = view?.toolbar

        music = args.currentMusic

//        val activity = activity as AppCompatActivity?

//        activity!!.setSupportActionBar(toolbar)
//        assert(activity!!.supportActionBar != null)
//        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        activity.supportActionBar?.setDisplayShowTitleEnabled(false)
//        val actionBar: ActionBar = activity!!.setSupportActionBar()

//        view2.tvJudulLagu.isSelected = true

        mediaPlayer = MediaPlayer()
        songTimer = SongTimer()

        view2.seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                mediaPlayer.seekTo(seekBar!!.progress)
//
                if (seekBar.progress > 0)/*.and(view2.imagePlay.visibility == View.GONE))*/ {
                    mediaPlayer.start()
                    view2.imagePlay.setBackgroundResource(R.drawable.ic_pause)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
//                handler.removeCallbacks(runnable)
                view2.imagePlay.setBackgroundResource(R.drawable.ic_play)
                mediaPlayer.pause()
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
        })

        mediaPlayer.setOnPreparedListener {
            // Do something. For example: playButton.setEnabled(true);
            view2.imagePlay.setBackgroundResource(R.drawable.ic_pause)
            view2.visualizerView.getPathMedia(mediaPlayer)
            onPrepared(it)
        }

        musicList = DeviceMusic.musicsList //.addAll(MusicProvider.getMusicList(requireContext()))
//        songList.addAll(SongsManager.getPlayList())

        Log.d("songFrag song args: ", "${args.currentMusic.id}, ${args.currentMusic.title}")

     //get data song
        mediaPlayer.reset()
        getPlaySong(musicList.indexOf(args.currentMusic))

        progressUpdater.start()
        //methods button action
        getButtonSong()
        return view2
    }

    private fun getPlaySong(songIndex: Int) {
        val uri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, music.id.toString())
        view2.seekBar.progress = 0
        var flag = getCoverArtPath(musicList[songIndex].albumId, requireContext())
        var bitmap: Bitmap?

        if (flag != null) {
            bitmap = BitmapFactory.decodeFile(flag)
            view2.imageCover.setImageBitmap(bitmap)
        }
        else {
            Glide.with(this)
                .load(musicList[songIndex].albumUri)
                .error(R.drawable.ic_all_inclusive_black_24dp)
                .into(view2.imageCover)
        }
        Log.d("getPlaySong: ", "isplaying : ${isPlaying}, index: $songIndex, id: ${music.id}, ${musicList[songIndex].title}")
        try {
            mediaPlayer.setDataSource(musicList[songIndex].path)
            mediaPlayer.prepare()

            mediaPlayer.setOnCompletionListener{
                onCompletion(it)
            }
//            isPlaying = true
            songTitle = musicList[songIndex].title.replace("_", " ").toString()
            view2.tvJudulLagu.text = songTitle
            view2.seekBar.progress = 0
            view2.seekBar.max = 100
            duration = mediaPlayer.duration
            view2.seekBar.max = duration

        } catch (e: IOException) {
            e.printStackTrace()
        }finally {
            println("play error...");
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
            Log.d("Runnable: ", "isplaying : ${isPlaying}")
//            while (isPlaying) {

                try {
                    while (!Thread.currentThread().isInterrupted) {
//                        println("Thread is alive...")
//                        Thread.sleep(500) //5초간 멈춤
                        val totalDuration = duration.toLong() //mediaPlayer.duration
                        val currentDuration = mediaPlayer.currentPosition.toLong()
                        view2.tvTotalDuration.text = "" + songTimer.milliSecondsToTimer(totalDuration)
                        view2.tvCurrentDuration.text = "" + songTimer.milliSecondsToTimer(currentDuration)

                        Thread.sleep(500)

//                        if (mediaPlayer != null) {
                            view2.seekBar.progress = mediaPlayer.currentPosition
//                        }
                    }

                } catch (e: Exception) {

                }finally {
                    println("Thread is dead...");
                }
//            }
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
            playNext()
        }


        view2.imagePrev.setOnClickListener {
            if (currentSongIndex == 0){
                currentSongIndex = musicList.size-1
            } else {
                currentSongIndex -= 1
            }

            mediaPlayer.stop()
            mediaPlayer.reset()
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

    override fun onCompletion(p0: MediaPlayer?) {
        println("onCompletion")
        when {
            isRepeat -> {
                getPlaySong(currentSongIndex)
            }
            isShuffle -> {
                val rand = Random()
                currentSongIndex = rand.nextInt(musicList.size - 1 - 0 + 1) + 0
                getPlaySong(currentSongIndex)
            }
            else -> {
                playNext()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
//        musicList = mutableListOf<Music>()
        Log.d("생명주기:", "onDestroy")
    }

    private fun playNext(){

        if (currentSongIndex >= musicList.size-1) {
            currentSongIndex = 0
            Log.d("next index:"," ${musicList.size}, $currentSongIndex")

        } else {
            currentSongIndex += 1
            Log.d("next index:"," ${musicList.size}, $currentSongIndex")

        }
        mediaPlayer.stop()
        mediaPlayer.reset()
        view2.imagePlay.setBackgroundResource(R.drawable.ic_play)
        getPlaySong(currentSongIndex)
    }
}

