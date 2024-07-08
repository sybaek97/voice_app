package com.baek.voice.viewModel

import android.app.Application
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.Locale

class TtsViewModel(application: Application):AndroidViewModel(application),OnInitListener {
    private val _isTtsInitialized = MutableLiveData<Boolean>()
    val isTtsInitialized: LiveData<Boolean> get() = _isTtsInitialized
    private val tts: TextToSpeech = TextToSpeech(application, this)
    init {
       _isTtsInitialized.value=false
    }
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.KOREAN)
            _isTtsInitialized.value = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
        } else {
            _isTtsInitialized.value = false
        }
    }

    fun speakOut(text: String) {
        if (_isTtsInitialized.value == true) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
        }
    }

    override fun onCleared() {
        super.onCleared()
        tts.stop()
        tts.shutdown()
    }

}