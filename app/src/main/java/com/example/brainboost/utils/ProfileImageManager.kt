package com.example.brainboost.utils

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object ProfileImageManager {

    private val _imageUri = MutableLiveData<Uri?>()
    val imageUri: LiveData<Uri?> get() = _imageUri

    fun setImageUri(uri: Uri?) {
        _imageUri.postValue(uri)
    }

    // REMOVE this duplicate function:
    // fun getImageUri(): LiveData<Uri?> = imageUri
}
