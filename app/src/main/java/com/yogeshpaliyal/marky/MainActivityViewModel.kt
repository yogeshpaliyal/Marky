package com.yogeshpaliyal.marky

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val _editorMode = MutableLiveData<Mode>(Editor)
    val editorMode : LiveData<Mode> = _editorMode


    val content = MutableLiveData<String>("")


    fun changeToEditor(){
        _editorMode.value = Editor
    }

    fun changeToPreview(){
        _editorMode.value = Preview

    }

}