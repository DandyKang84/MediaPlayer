package com.example.mediaplayer

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Parcel
import android.os.ParcelFileDescriptor
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import java.io.IOException

@Parcelize
class Music(var id:String, var title:String?, var artist:String?, var albumId: String?, var duration: Int?, var likes : Int?) : Parcelable {
    //serializable로 안하고 parcelable 하는 이유는 속도처리, 용량처리가 더 좋음
    companion object : Parceler<Music>{
        override fun create(parcel: Parcel): Music {
            return Music(parcel)
        }

        //parcelable 쓸때
        override fun Music.write(parcel: Parcel, flags: Int) {
            parcel.writeString(id)
            parcel.writeString(title)
            parcel.writeString(artist)
            parcel.writeString(albumId)
            parcel.writeInt(duration!!)
            parcel.writeInt(likes!!)
        }
    }

    //parcelable로 읽을 때
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt()
    )

    //앨범 uri를 가져온다
    fun getAlbumUri(): Uri {
        //앨범에 위치를 정해준다
        return Uri.parse("content://media/external/audio/albumart/"+albumId)
    }

    //음악 Uri를 가져온다
    fun getMusicUri() : Uri{
        //음악 위치를 가져온다
        return Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,id)
    }

    //음악 비트맵을 가져와서 원하는 사이즈로 비트맵 만들기
    //너가 원하는 곳에 사진을 뿌려줄게

    fun getAlbumImage(context: Context, albumImageSize: Int): Bitmap? {
        val contentResolver : ContentResolver = context.contentResolver
        //앨범 경로인데 uri를 준것
        val uri =  getAlbumUri()
        //비트맵 옵션설정
        val option = BitmapFactory.Options()

        if(uri != null){
            //파일로 가져온다
            var parcelFileDescriptor : ParcelFileDescriptor? = null
            try{
                parcelFileDescriptor = contentResolver.openFileDescriptor(uri,"r")
                //우리가 정한 옵션비트맵으로 가져온다
                var bitmap = BitmapFactory.decodeFileDescriptor(parcelFileDescriptor!!.fileDescriptor,null, option)
                //비트맵을 가져왔는데 우리가 원하는 사이즈가 아닐경우를 위해서 처리
                if(bitmap != null){
                    val tempBitmap= Bitmap.createScaledBitmap(bitmap, albumImageSize, albumImageSize, true)
                    //우리가 원하는 사이즈를 비트맵에 넣는다
                    bitmap.recycle()
                    bitmap = tempBitmap
                }
                return bitmap
            }catch (e: Exception){
                Log.d("mediaplayer","getAlbumImage() ${e.toString()}")
            }finally {
                try {
                    parcelFileDescriptor?.close()
                }catch (e: IOException){
                    Log.d("mediaplayer","getAlbumImage() parcelFileDescriptor ${e.toString()}")
                }
            }
        }
        return null
    }

}