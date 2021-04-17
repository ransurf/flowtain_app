package com.example.flowtain.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel(p: Long) : ViewModel() {
    private val _points = MutableLiveData<Long>().apply {
        value = p
    }
    val points: LiveData<Long> = _points
}