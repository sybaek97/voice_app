package com.baek.voice.common

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.baek.voice.viewModel.PermissionViewModel

object PermissionRequestHandler {
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private const val PREF_NAME = "PermissionPrefs"
    private const val PREF_RECORD_AUDIO_DENIED_COUNT = "RecordAudioDeniedCount"

    fun initializePermissionLauncher(fragment: Fragment) {
        val viewModel = ViewModelProvider(fragment)[PermissionViewModel::class.java]

        permissionLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                viewModel.updatePermissionStatus(PermissionViewModel.PermissionStatus.GRANTED)
            } else {
                val deniedCount = getDeniedCount(fragment) + 1
                incrementDeniedCount(fragment)
                if (deniedCount >= 2) {
                    viewModel.updatePermissionStatus(PermissionViewModel.PermissionStatus.DENIED_TWICE)
                    // 설정 창으로 이동
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", fragment.requireActivity().packageName, null)
                    }
                    fragment.startActivity(intent)
                } else {
                    viewModel.updatePermissionStatus(PermissionViewModel.PermissionStatus.DENIED_ONCE)
                }
            }
        }
    }

    fun requestAudioPermission(fragment: Fragment) {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    private fun incrementDeniedCount(fragment: Fragment) {
        val sharedPreferences = fragment.requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val deniedCount = sharedPreferences.getInt(PREF_RECORD_AUDIO_DENIED_COUNT, 0) + 1
        sharedPreferences.edit().putInt(PREF_RECORD_AUDIO_DENIED_COUNT, deniedCount).apply()
    }

    private fun getDeniedCount(fragment: Fragment): Int {
        val sharedPreferences = fragment.requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getInt(PREF_RECORD_AUDIO_DENIED_COUNT, 0)
    }
}