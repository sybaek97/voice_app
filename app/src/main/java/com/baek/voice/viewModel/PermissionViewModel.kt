package com.baek.voice.viewModel


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PermissionViewModel: ViewModel() {

    private val _permissionStatus = MutableLiveData<PermissionStatus>()
    val permissionStatus: LiveData<PermissionStatus> get() = _permissionStatus

    fun updatePermissionStatus(status: PermissionStatus) {
        Log.d("PermissionViewModel", "updatePermissionStatus: $status")
        _permissionStatus.value = status
    }

    enum class PermissionStatus {
        GRANTED,
        DENIED_ONCE,
        DENIED_TWICE
    }
}