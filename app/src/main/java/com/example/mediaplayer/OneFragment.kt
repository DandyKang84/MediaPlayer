package com.example.mediaplayer

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mediaplayer.databinding.FragmentOneBinding

class OneFragment: Fragment() {
    lateinit var binding: FragmentOneBinding
    private lateinit var adapter: MusicRecyclerAdapter
    lateinit var mainContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainContext = context
    }

    override fun onResume() {
        super.onResume()
        changeItem()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentOneBinding.inflate(inflater, container, false)
        changeItem()
        return binding.root
    }

    fun changeItem() {
        val dbHelper = DBHelper(mainContext, MainActivity.DB_NAME, MainActivity.VERSION)
        val musicList = dbHelper.selectMusicAll()
        val linearLayoutManager = LinearLayoutManager(mainContext.applicationContext)
        binding.recyclerView.layoutManager = linearLayoutManager
        adapter = MusicRecyclerAdapter(mainContext.applicationContext,  musicList)
        binding.recyclerView.adapter = adapter
    }

    fun changeSearch(query: String?) {
        val dbHelper = DBHelper(mainContext, MainActivity.DB_NAME, MainActivity.VERSION)
        val musicList = dbHelper.searchMusic(query)
        val linearLayoutManager = LinearLayoutManager(mainContext.applicationContext)
        binding.recyclerView.layoutManager = linearLayoutManager
        adapter = MusicRecyclerAdapter(mainContext.applicationContext,  musicList)
        binding.recyclerView.adapter = adapter
    }
}