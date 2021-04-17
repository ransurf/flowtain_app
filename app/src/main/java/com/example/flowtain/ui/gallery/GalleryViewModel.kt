package com.example.flowtain.ui.gallery


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.flowtain.util.PrefUtil

class GalleryViewModel() : ViewModel() {

    private val _points = MutableLiveData<Long>()
    val points: LiveData<Long> = _points


}