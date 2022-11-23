package com.example.mediaplayer

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.PagerAdapter
import com.example.mediaplayer.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    companion object{
        val REQ_READ = 99
        val DB_NAME = "musicDB"
        var VERSION = 1
    }

    lateinit var binding: ActivityMainBinding
    lateinit var adapter: MusicRecyclerAdapter
    private var musicList : MutableList<Music>? = mutableListOf<Music>()
    lateinit var oneFragment: OneFragment
    lateinit var twoFragment: TwoFragment
    lateinit var toggle: ActionBarDrawerToggle
    //승인받을 퍼미션항목요청
    val permissions= arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //승인이 되었는지 점검
        if(isPermitted()){
            startProcess()
        }else{
            //외부저장소 읽기권한이 없다면 사용자(유저)에게 읽기 권한 신청을 해야합
            ActivityCompat.requestPermissions(this, permissions, REQ_READ)
        }

        setSupportActionBar(binding.toolbar)
        toggle = ActionBarDrawerToggle(this, binding.drawerLayout, R.string.drawer_open,R.string.drawer_closed)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toggle.syncState()

        val pagerAdapter = PagerAdapter(this)
        val title = mutableListOf<String>("음악목록","찜리스트")

        oneFragment = OneFragment()
        twoFragment = TwoFragment()
        pagerAdapter.addFragment(oneFragment, title[0])
        pagerAdapter.addFragment(twoFragment, title[1])
        binding.viewpager.adapter = pagerAdapter

        TabLayoutMediator(binding.tablayoutMain1, binding.viewpager){ tab, poition ->
            tab.text = title.get(poition)

        }.attach()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQ_READ && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            startProcess()
        }else{
            Toast.makeText(this,"권한요청을 승인 하셔야 뮤직 앱을 실행 하실 수 있습니다",Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun startProcess() {
//        var musicList : MutableList<Music>? = mutableListOf<Music>()
        //먼저 데이타베이스(핸드폰내 음원DB)에서 음원정보를 가져온다. 없으면 공유메모리에서 음원정보를 가져온다.
        val dbHelper = DBHelper(this, DB_NAME, VERSION)
        // db에서 뮤직리스트를 가져옴 selectMusicAll()
        musicList = dbHelper.selectMusicAll()
        // db에 음원리스트가 null이면 콘텐트리졸버를 통해서 공유메모리로가서 가져와라
        if(musicList == null) {
            val playMusicList = getMusicList()
            if(playMusicList != null){
                for(i in 0..playMusicList.size -1){
                    val music = playMusicList.get(i)
                    //여기서 db에 음원을 넣어준다.
                    dbHelper.insertMusic(music)
                }
                musicList = playMusicList
            }else{
                Log.d("chap17mp3_dp1","class MainActivity.startProcess() 외장메모리에 음원파일이 없습니다.")
            }
        }
    }

    private fun getMusicList(): MutableList<Music>? {
        var imsiMusicList : MutableList<Music>? = mutableListOf<Music>()
        //1. 공유메모리에서 음악정보 주소
        val musicURL = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        //2. 음원에서 가져올 정보를 배열로 만든다
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION
        )
        //3. 콘텐트리졸버 쿼리작성을 통해서 musicList로 가져오기
        val cursor = contentResolver.query(musicURL, projection, null, null, null)
        if (cursor?.count!! > 0){

            while (cursor!!.moveToNext()) { // cursor를 다음행으로 이동시킨다.
                val id = cursor.getString(0)
                val title = cursor.getString(1).replace("'", "")
                val artist = cursor.getString(2).replace("'", "")
                val album = cursor.getString(3)
                val duration = cursor.getInt(4)
                val music = Music(id, title, artist, album, duration, 0)
                // 가져온 음원정보를 데이타베이스 에 저장(음원정보를 입력한다)
                imsiMusicList?.add(music)
            }
        }else{
            imsiMusicList = null
        }
        return imsiMusicList
    }

    // 사용하는앱이 외부저장소(핸드폰앱권한)를 읽읅권한이 있는지 체크(퍼미션)
    fun isPermitted():Boolean{
        // 펴미션권한 1개 요구
        if(ContextCompat.checkSelfPermission(this,permissions[0]) !=PackageManager.PERMISSION_GRANTED){
            return false
        }else{
            return true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        //메뉴에서 서치항목을 찾는다
        val searchMenu = menu?.findItem(R.id.menu_search)
        val searchview = searchMenu?.actionView as SearchView

        searchview.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return true
            }

            //글자가 바뀔때마다 가져온다
            override fun onQueryTextChange(query: String?): Boolean {
                val dbHelper = DBHelper(applicationContext, MainActivity.DB_NAME,MainActivity.VERSION)
                if(query.isNullOrBlank()){
                    musicList?.clear()
                    when(binding.viewpager.currentItem){
                        0 -> oneFragment.changeItem()
                        1 -> twoFragment.changeItem()
                    }
                }else{
                    musicList?.clear()
                    when(binding.viewpager.currentItem){
                        0 -> oneFragment.changeSearch(query)
                        1 -> twoFragment.changeSearch(query)
                    }
                }
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val dbHelper = DBHelper(applicationContext, DB_NAME, VERSION)
        when(item.itemId){
            R.id.menu_like -> {
                musicList?.clear()
                dbHelper.selectMusicLike()?.let{musicList?.addAll(it)}
                binding.viewpager.currentItem = 1}
            R.id.menu_main -> {
                musicList?.clear()
                dbHelper.selectMusicAll()?.let{musicList?.addAll(it)}
                binding.viewpager.currentItem = 0}
        }
        return super.onOptionsItemSelected(item)
    }
}