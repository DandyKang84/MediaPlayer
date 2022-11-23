package com.example.mediaplayer

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DBHelper( context: Context, dbName: String,  version: Int): SQLiteOpenHelper(context, dbName, null, version) {

    // DBHelper 처음 객체가 만들어질 때 딱 한번만 실행된다.
    override fun onCreate(db: SQLiteDatabase?) {
        val query = """
            create table musicTBL(
            id text primary key,
            title text,
            artist text,
            albumId text,
            duration Integer,
            likes Integer)
            """.trimIndent()
        db?.execSQL(query)
    }
    // 버전이 변경이 되었을 때 불러지는 콜백함수
    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        val query = """
            drop table musicTBL
        """.trimIndent()
        db?.execSQL(query)
        this.onCreate(db)
    }

    fun selectMusicAll(): MutableList<Music>? {
        var musicList: MutableList<Music>? = mutableListOf<Music>()
        var cursor: Cursor? = null
        val query = """
            select * from musicTBL
        """.trimIndent()
        //쿼리문 실행하기위한 함수
        val db = this.readableDatabase
        try{
            cursor = db.rawQuery(query, null)
            if(cursor.count > 0){
                while (cursor.moveToNext()){ // cursor를 다음행으로 이동시킨다.
                    val id = cursor.getString(0)
                    val title = cursor.getString(1)
                    val artist = cursor.getString(2)
                    val albumId = cursor.getString(3)
                    val duration = cursor.getInt(4)
                    val likes = cursor.getInt(5)
                    val music = Music(id, title, artist, albumId, duration, likes)
                    musicList?.add(music)
                }
            }else{
                musicList = null
            }
        }catch (e: java.lang.Exception){
            Log.d("chap17mp3_dp1","class DBHelper.selectMusicAll() ${e.printStackTrace()}")
            musicList = null
        }finally {
            cursor?.close()
            db.close()
        }
        return musicList
    }

    fun insertMusic(music: Music): Boolean {
        var flag = false
        val query = """
            insert into musicTBL(id, title, artist, albumId, duration, likes)
            values('${music.id}','${music.title}','${music.artist}','${music.albumId}',${music.duration},${music.likes})
        """.trimIndent()
        //쿼리문 실행하기위한 함수
        val db = this.writableDatabase
        try{
            db.execSQL(query)
            flag = true
        }catch (e: java.lang.Exception){
            Log.d("chap17mp3_dp1","class DBHelper.insertMusic() ${e.printStackTrace()}")
            flag = false
        }finally {
            db.close()
        }
        return true
    }

    fun updateLike(music: Music): Boolean {
        var flag = false
        val query = """
            update musicTBL 
            set likes = ${music.likes}
            where id = '${music.id}'
        """.trimIndent()
        //쿼리문 실행하기위한 함수
        val db = this.writableDatabase
        try{
            db.execSQL(query)
            flag = true
        }catch (e: java.lang.Exception){
            Log.d("chap17mp3_dp1","class DBHelper.updateLike() ${e.printStackTrace()}")
            flag = false
        }finally {
            db.close()
        }
        return true
    }

    fun searchMusic(query: String?): MutableList<Music>? {
        var musicList: MutableList<Music>? = mutableListOf<Music>()
        var cursor: Cursor? = null
        // 제목,아이티스 중에 중복되는글자 다 가져와~
        val query = """
            select * from musicTBL
            where title like '${query}%' or artist like '${query}%'
        """.trimIndent()
        //쿼리문 실행하기위한 함수
        val db = this.readableDatabase
        try{
            cursor = db.rawQuery(query, null)
            if(cursor.count > 0){
                while (cursor.moveToNext()){ // cursor를 다음행으로 이동시킨다.
                    val id = cursor.getString(0)
                    val title = cursor.getString(1)
                    val artist = cursor.getString(2)
                    val albumId = cursor.getString(3)
                    val duration = cursor.getInt(4)
                    val likes = cursor.getInt(5)
                    val music = Music(id, title, artist, albumId, duration, likes)
                    musicList?.add(music)
                }
            }else{
                musicList = null
            }
        }catch (e: java.lang.Exception){
            Log.d("chap17mp3_dp1","class DBHelper.selectMusicAll() ${e.printStackTrace()}")
            musicList = null
        }finally {
            cursor?.close()
            db.close()
        }
        return musicList
    }

    fun selectMusicLike(): MutableList<Music>? {
        var musicList: MutableList<Music>? = mutableListOf<Music>()
        var cursor: Cursor? = null
        val query = """
            select * from musicTBL
            where likes = 1
        """.trimIndent()
        //쿼리문 실행하기위한 함수
        val db = this.readableDatabase
        try{
            cursor = db.rawQuery(query, null)
            if(cursor.count > 0){
                while (cursor.moveToNext()){ // cursor를 다음행으로 이동시킨다.
                    val id = cursor.getString(0)
                    val title = cursor.getString(1)
                    val artist = cursor.getString(2)
                    val albumId = cursor.getString(3)
                    val duration = cursor.getInt(4)
                    val likes = cursor.getInt(5)
                    val music = Music(id, title, artist, albumId, duration, likes)
                    musicList?.add(music)
                }
            }else{
                musicList = null
            }
        }catch (e: java.lang.Exception){
            Log.d("chap17mp3_dp1","class DBHelper.selectMusicLike() ${e.printStackTrace()}")
            musicList = null
        }finally {
            cursor?.close()
            db.close()
        }
        return musicList
    }
}