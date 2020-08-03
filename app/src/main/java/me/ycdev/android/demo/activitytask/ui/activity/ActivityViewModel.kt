package me.ycdev.android.demo.activitytask.ui.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ActivityViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Activity Fragment"
    }
    val text: LiveData<String> = _text
}
