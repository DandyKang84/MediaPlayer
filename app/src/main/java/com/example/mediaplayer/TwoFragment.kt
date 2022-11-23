package com.example.mediaplayer

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mediaplayer.databinding.FragmentTwoBinding


class TwoFragment : Fragment() {

    lateinit var binding: FragmentTwoBinding
    lateinit var fragmentAdapter: FragmentAdapter
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
        binding = FragmentTwoBinding.inflate(inflater, container, false)
        changeItem()
        return binding.root
    }

    fun changeItem() {
        val dbHelper = DBHelper(mainContext, MainActivity.DB_NAME, MainActivity.VERSION)
        val musicList = dbHelper.selectMusicLike()
        val linearLayoutManager = LinearLayoutManager(mainContext.applicationContext)
        binding.recyclerViewTwo.layoutManager = linearLayoutManager
        fragmentAdapter = FragmentAdapter(mainContext.applicationContext, musicList)
        binding.recyclerViewTwo.adapter = fragmentAdapter
    }

    fun changeSearch(query: String?) {
        val dbHelper = DBHelper(mainContext, MainActivity.DB_NAME, MainActivity.VERSION)
        val musicList = dbHelper.searchMusic(query)
        val linearLayoutManager = LinearLayoutManager(mainContext.applicationContext)
        binding.recyclerViewTwo.layoutManager = linearLayoutManager
        fragmentAdapter = FragmentAdapter(mainContext.applicationContext,  musicList)
        binding.recyclerViewTwo.adapter = fragmentAdapter
    }

}