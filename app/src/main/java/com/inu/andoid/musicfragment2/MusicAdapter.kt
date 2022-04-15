package com.inu.andoid.musicfragment2

import android.os.Binder
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.inu.andoid.musicfragment2.databinding.ItemLayoutBinding
import com.inu.andoid.musicfragment2.model.Music
import kotlinx.android.synthetic.main.item_layout.view.*
import java.text.SimpleDateFormat

class MusicAdapter: RecyclerView.Adapter<MusicAdapter.Holder>() {
    private lateinit var binding: ItemLayoutBinding
    private val musicList = mutableListOf<Music>()

    inner class Holder(private val binding: ItemLayoutBinding): RecyclerView.ViewHolder(binding.root) {

        fun setMusic(music: Music) {
            with(binding) {
                textTitle.text = music.title
                texArtist.text = music.artist
                val sdf = SimpleDateFormat("mm:ss")
                textDuration.text = sdf.format(music.duration)
            }
//                1. 로드할 대상 Uri    2. 입력될 이미지뷰
            Glide.with(binding.root.context)
                .load(music.albumUri)
                //    .placeholder(R.drawable.ic_menu_close_clear_cancel).into(binding.imageAlbum)
                .placeholder(R.drawable.outline_music_note_24)
                .error(R.drawable.outline_music_note_24)
//                .fallback(R.drawable.outline_music_note_24)
                .into(binding.imageAlbum)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val music = musicList[position]
        holder.setMusic(music)
        holder.itemView.main_item.setOnClickListener {
            Log.d("sdf","sdf")
            val action = ListFragmentDirections.actionListFragmentToPlaySongFragment(music)
            holder.itemView.findNavController().navigate(action)
        }
    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    fun addSongs(songs: MutableList<Music>) {
        musicList.clear()
        musicList.addAll(songs)
//        notifyDataSetChanged()
    }

}