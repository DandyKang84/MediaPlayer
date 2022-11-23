package com.example.mediaplayer

import android.graphics.Bitmap
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.mediaplayer.databinding.ActivityPlaymusicBinding
import kotlinx.coroutines.*
import java.text.SimpleDateFormat

class PlaymusicActivity : AppCompatActivity() {

    companion object{
        val ALBUM_SIZE = 150
    }

    private lateinit var binding : ActivityPlaymusicBinding
    private var playList : MutableList<Parcelable>? = null
    private var position : Int = 0
    private var music : Music? = null
    private var mediaPlayer : MediaPlayer? = null
    private var messengerJob : Job? = null
    private var bitmap: Bitmap? = null
    private val PREVIOUS = 0
    private val NEXT = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaymusicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //인텐트 정보 가져오기
        playList = intent.getParcelableArrayListExtra("playList")
        position = intent.getIntExtra("position", 0)
        music = playList?.get(position) as Music

        //화면에 보이기 (바인딩)
        setplay(music)
        // ------------------이벤트 버튼-----------------------------
        //이벤트 설정 목록버튼
        binding.listButton.setOnClickListener {
            mediaPlayer?.stop()
            messengerJob?.cancel()
            mediaPlayer?.release()
            mediaPlayer = null
            finish()
        }
        //이벤트 설정 정지버튼
        binding.stopButton.setOnClickListener {
            mediaPlayer?.stop()
            messengerJob?.cancel()
            mediaPlayer = MediaPlayer.create(this, music?.getMusicUri())
            binding.seekBar.progress = 0
            binding.playDuration.text = "00:00"
        }
        //이벤트 설정 재생버튼
        binding.playButton.setOnClickListener {
            if (mediaPlayer?.isPlaying == true){
                mediaPlayer?.pause()
                binding.playButton.setImageResource(R.drawable.stop_blue1)
            } else {
                thread()
            }
        }//end of playButton
        // 이벤트 설정 뒤로가기버튼
        binding.backButton.setOnClickListener {
            position = getPosition(PREVIOUS, position)
            setReplay()
        }
        // 건너뛰기버튼
        binding.goButton.setOnClickListener {
            position = getPosition(NEXT, position)
            setReplay()
        }
    }
    // -------------------------함수 처리-------------------------------
    fun getPosition(option:Int , position : Int) :Int {
        var newPosition: Int = position
        when (position) {
            0 -> {newPosition =
                if(option == PREVIOUS){ playList!!.size -1 } else  position +1
            }
            in 1 until (playList!!.size -1) -> {
                newPosition =
                    if (option == PREVIOUS){ position -1 } else position +1
            }
            playList!!.size -1 -> {
                newPosition =
                    if (option == PREVIOUS) { position - 1 } else 0
            }
        }
        return newPosition
    }

    fun thread() {
        mediaPlayer?.start()
        binding.playButton.setImageResource(R.drawable.play_blue)
        val backgroundScope = CoroutineScope(Dispatchers.Default + Job())
        var replay = false
        messengerJob = backgroundScope.launch {
            while(mediaPlayer?.isPlaying == true) {

                runOnUiThread {
                    var currentPosition = mediaPlayer?.currentPosition!!
                    binding.seekBar.progress = currentPosition
                    val currentDurateion = SimpleDateFormat("mm:ss").format(mediaPlayer!!.currentPosition)
                    binding.playDuration.text = currentDurateion
                }
                try {
                    // 1초마다 수행되도록 딜레이
                    delay(1000)
                } catch (e: Exception) {
                    Log.d("로그", "스레드 오류 발생")
                }
            }//end of while
            runOnUiThread {
                if (mediaPlayer!!.currentPosition >= (binding.seekBar.max - 1000)) {
                    binding.seekBar.progress = 0
                    binding.playDuration.text = "00:00"
                    if(!replay){
                        position += 1
                    }
                    music = playList?.get(position) as Music
                    setplay(music)
                    thread()
                }
            }
        }
    }

    fun setplay(music: Music?){
        binding.albumTitle.text = music?.title
        binding.albumArtist.text = music?.artist
        binding.totalDuration.text = SimpleDateFormat("mm:ss").format(music?.duration)
        binding.playDuration.text = "00:00"
        bitmap = music?.getAlbumImage(this, ALBUM_SIZE)
        if (bitmap != null) {
            binding.albumImage.setImageBitmap(bitmap)
        } else {
            binding.albumImage.setImageResource(R.drawable.ic_music_24)
        }
        mediaPlayer = MediaPlayer.create(this, music?.getMusicUri())
        binding.seekBar.max = mediaPlayer?.duration!!
        binding.seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                Log.d("kr.or.mrhi.musicplayer", "onStartTrackingTouch()")
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                Log.d("kr.or.mrhi.musicplayer", "onStopTrackingTouch()")
            }
        })
    }

    fun setReplay(){
        mediaPlayer?.stop()
        messengerJob?.cancel()
        music = playList?.get(position) as Music

        binding.albumTitle.text = music?.title
        binding.albumArtist.text = music?.artist
        binding.totalDuration.text = SimpleDateFormat("mm:ss").format(music?.duration)
        binding.playDuration.text = "00:00"
        bitmap = music?.getAlbumImage(this, ALBUM_SIZE)
        if (bitmap != null) {
            binding.albumImage.setImageBitmap(bitmap)
        } else {
            binding.albumImage.setImageResource(R.drawable.ic_music_24)
        }
        //음악 등록
        mediaPlayer = MediaPlayer.create(this, music?.getMusicUri())

        //시크바 음악 재생위치 변경
        binding.seekBar.max = mediaPlayer!!.duration
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
                }
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {
                Log.d("chap17mp3_dp1", "움직인다")
            }
            override fun onStopTrackingTouch(p0: SeekBar?) {
                Log.d("chap17mp3_dp1", "안움직인다?")
            }
        })
    }
}
