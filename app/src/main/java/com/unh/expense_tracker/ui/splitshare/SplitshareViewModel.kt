package com.unh.expense_tracker.ui.splitshare

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SplitshareViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is splitshare Fragment"
    }
    val text: LiveData<String> = _text
}