package com.example.mediaplayer

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mediaplayer.databinding.ItemRecyclerBinding
import kotlinx.coroutines.Job
import java.text.SimpleDateFormat


class FragmentAdapter(var context: Context, var musicList: MutableList<Music>?): RecyclerView.Adapter<FragmentAdapter.CustomViewHolder>() {
    companion object{
        val ALBUM_SIZE = 80
    }
    private var mediaPlayer : MediaPlayer? = null
    private var messengerJob : Job? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = ItemRecyclerBinding.inflate(LayoutInflater.from(parent.context),parent, false)
        return CustomViewHolder(binding)
        Log.d("mediaplayer","class MusicRecyclerAdapter.onCreateViewHolder() 업데이트")
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val binding = (holder as CustomViewHolder).binding
        val music = musicList?.get(position)
        Log.d("mediaplayer","class MusicRecyclerAdapter.onBindViewHolder() dddddddddddd${binding}")
        Log.d("mediaplayer","class MusicRecyclerAdapter.onBindViewHolder() **************************${music}")

        binding.tvArtist.text = music?.artist
        binding.tvTitle.text = music?.title
        binding.tvDuration.text = SimpleDateFormat("mm:ss").format(music?.duration)
        val bitmap = music?.getAlbumImage(context,MusicRecyclerAdapter.ALBUM_SIZE)
        if(bitmap != null){
            binding.ivAlbumArt.setImageBitmap(bitmap)
        }else{
            binding.ivAlbumArt.setImageResource(R.drawable.ic_music_24)
        }
        when(music?.likes){
            0 -> {binding.ivItemLike.setImageResource(R.drawable.ic_favorite_24)}
            1 -> {binding.ivItemLike.setImageResource(R.drawable.ic_heart24)}
        }
        //이벤트처리
        binding.root.setOnClickListener {
            val playList : ArrayList<Parcelable>? = musicList as ArrayList<Parcelable>
            val intent = Intent(binding.root.context, PlaymusicActivity::class.java)
            intent.putExtra("playList",playList)
            intent.putExtra("position",position)
            intent.putExtra("music",music)
            binding.root.context.startActivity(intent)
        }

        // 이벤트처리 좋아요눌렀을 때 데이타베이스 좋아요 등록
        binding.ivItemLike.setOnClickListener {
            if(music?.likes == 0){
                binding.ivItemLike.setImageResource(R.drawable.ic_heart24)
                music?.likes = 1
            }else{
                binding.ivItemLike.setImageResource(R.drawable.ic_favorite_24)
                music?.likes = 0
            }
            if(music != null){
                val dbHelper = DBHelper(context, MainActivity.DB_NAME, MainActivity.VERSION)
                val flag = dbHelper.updateLike(music)

                if(flag == false){
                    Log.d("mediaplayer","class MusicRecyclerAdapter.onBindViewHolder() 업데이트 실패")
                }else{
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return musicList?.size?:0
    }

    class CustomViewHolder(val binding: ItemRecyclerBinding): RecyclerView.ViewHolder(binding.root)
}