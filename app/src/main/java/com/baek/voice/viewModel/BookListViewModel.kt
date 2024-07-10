package com.baek.voice.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.baek.voice.R

class BookListViewModel(application: Application):AndroidViewModel(application) {

    private val _data = MutableLiveData<List<String>>()
    val data: LiveData<List<String>> = _data
    init {
        loadData()
    }
    private fun loadData(){
        val resource= getApplication<Application>().resources
        val dataList = resource.getStringArray(R.array.top_book_list).toList()
        _data.value=dataList
    }
}