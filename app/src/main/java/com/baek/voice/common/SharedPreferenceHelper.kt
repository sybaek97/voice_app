package com.baek.voice.common

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.baek.voice.R

class SharedPreferenceHelper(context: Context, private val key: String, private val value: Any?) {
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private val preferences: SharedPreferences = EncryptedSharedPreferences.create(
        context.getString(R.string.SHARED_PREFERENCE_SETTING),
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val prefEditor = preferences.edit()

    fun prefSetter() {
        when (value) {
            is String -> prefEditor.putString(key, value).apply()
            is Int -> prefEditor.putInt(key, value).apply()
            is Boolean -> prefEditor.putBoolean(key, value).apply()
            is Long -> prefEditor.putLong(key, value).apply()
            is Float -> prefEditor.putFloat(key, value).apply()
            else -> {}
        }
    }

    fun prefGetter(): Any? {
        return when(value) {
            is String -> preferences.getString(key, null)
            is Int -> preferences.getInt(key, 0)
            is Boolean -> preferences.getBoolean(key, false)
            is Long -> preferences.getLong(key, 0L)
            is Float -> preferences.getFloat(key, 0F)
            else -> null
        }
    }
}