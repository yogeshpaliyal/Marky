package com.yogeshpaliyal.marky

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val _editorMode = MutableLiveData<Int>(Mode.EDITOR)
    val editorMode : LiveData<Int> = _editorMode


    val content = MutableLiveData<String>("")


    fun changeToEditor(){
        _editorMode.value = Mode.EDITOR
    }

    fun changeToPreview(){
        _editorMode.value = Mode.PREVIEW

    }

}