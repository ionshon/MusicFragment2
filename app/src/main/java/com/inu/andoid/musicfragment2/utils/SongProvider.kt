package com.inu.andoid.musicfragment2.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.inu.andoid.musicfragment2.model.Music
import java.util.*

object SongProvider {
    private val TITLE = 0
    private val TRACK = 1
    private val YEAR = 2
    private val DURATION = 3
    private val PATH = 4
    private val ALBUM = 5
    private val ARTIST_ID = 6
    private val ARTIST = 7
    private val ID = 8
    private val ALBUMID = 9

    private val BASE_PROJECTION = arrayOf(
            MediaStore.Audio.AudioColumns.TITLE, // 0
            MediaStore.Audio.AudioColumns.TRACK, // 1
            MediaStore.Audio.AudioColumns.YEAR, // 2
            MediaStore.Audio.AudioColumns.DURATION, // 3
            MediaStore.Audio.AudioColumns.DATA, // 4
            MediaStore.Audio.AudioColumns.ALBUM, // 5
            MediaStore.Audio.AudioColumns.ARTIST_ID, // 6
            MediaStore.Audio.AudioColumns.ARTIST, // 7
            MediaStore.Audio.AudioColumns._ID,
            MediaStore.Audio.AudioColumns.ALBUM_ID,
    )
    private val mAllDeviceSongs = ArrayList<Music>()

    fun getAllDeviceSongs(context: Context): MutableList<Music> {
        val cursor = makeSongCursor(context)
        return getSongs(cursor)
    }


    private fun getSongs(cursor: Cursor?): MutableList<Music> {
        val songs = ArrayList<Music>()
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val song = getSongFromCursorImpl(cursor)
                if (song.duration >= 30000) {
                    songs.add(song)
                    mAllDeviceSongs.add(song)
                }
            } while (cursor.moveToNext())
        }

        cursor?.close()

        return songs
    }


    private fun getSongFromCursorImpl(cursor: Cursor): Music {
        val title = cursor.getString(TITLE)
        val trackNumber = cursor.getInt(TRACK)
        val year = cursor.getInt(YEAR)
        val duration = cursor.getLong(DURATION)
        val path = cursor.getString(PATH)
        val albumName = cursor.getString(ALBUM)
        val artistId = cursor.getInt(ARTIST_ID)
        val artistName = cursor.getString(ARTIST)
        val id = cursor.getInt(ID)
        val albumId = cursor.getLong(ALBUMID)
        val uri = Uri.parse("content://media/external/audio/albumart/$albumId")

        return Music(id, title, artistName, albumId, duration, uri, path)
    }

    internal fun makeSongCursor(context: Context): Cursor? {
        try {
            return context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    BASE_PROJECTION, null, null, null)
        } catch (e: SecurityException) {
            return null
        }

    }
}
