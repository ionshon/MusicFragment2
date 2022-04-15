package com.inu.andoid.musicfragment.utils

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.inu.andoid.musicfragment2.model.DeviceMusic.musicsList
import com.inu.andoid.musicfragment2.model.Music

object MusicProvider {

    // 컨텐트 리졸버로 음원 목록 가져오기
    // 1. 데이터 테이블 주소
    private val musicUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    var musicList = mutableListOf<Music>()
    // 2. 가져올 데이터 컬컴 정의
    val proj2 = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.DURATION
    )
    private val proj = arrayOf(
        MediaStore.Audio.AudioColumns._ID, // 6
        MediaStore.Audio.AudioColumns.TITLE, // 0
        MediaStore.Audio.AudioColumns.ARTIST,// 7
        MediaStore.Audio.AudioColumns.ALBUM_ID, // 5
        MediaStore.Audio.AudioColumns.DURATION, // 3
        MediaStore.Audio.AudioColumns.DATA, // 4
        MediaStore.Audio.AudioColumns.TRACK, // 1
        MediaStore.Audio.AudioColumns.YEAR, // 2
    )

    fun getMusicList(context: Context): List<Music> {
        //3.  컨텐트 리졸버에 해당 데이터 요청
        val cursor = context.contentResolver.query(musicUri, proj, null, null, null)
        // 4. 커서로 전달받은 데이터를 꺼내서 저장

        Log.d("provider: ", "${cursor!!.moveToNext()}")
        //    val defaultUri = Uri.parse("android.resource://com.inu.contentresolver/drawable/resource01")
        while (cursor!!.moveToNext()) {
            val id = cursor.getInt(0)
            val title = cursor.getString(1)
            val artist = cursor.getString(2)
            val albumId = cursor.getLong(3) //Long = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)) //cursor!!.getString(3)
            var albumUri = Uri.parse("content://media/external/audio/albumart/$albumId")
            val duration = cursor.getLong(4)
            val path = cursor.getString(5)
            //       val path = cursor.getString(cursor.getColumnIndex("_data"))
            //    Log.d("패스 로그:", "$path")
            if (duration > 200000) {  // 약 1분 이하 곡 제외
                //   i += 1
                val music = Music(id, title, artist, albumId, duration, albumUri, path) //,, path, albumArtBit)
                musicList.add(music)
            }
            Log.d("albumpath: ", "$albumUri")
        }

        cursor.close()

        return  musicList
    }
}