package com.inu.andoid.musicfragment2.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.Serializable
import java.time.Duration

@Parcelize
data class Music(
    var id:Int,
    var title:String,
    var artist:String,
    var albumId:Long,
    var duration: Long,
    var albumUri: Uri,
    var path : String
): Parcelable {
//    override fun toString():String =
//        "Music id : $id, albumId : $albumId, title : $title, artist : $artist, duration : $duration, padd: $stri"
}
