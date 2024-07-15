package com.baek.voice.viewModel

import android.app.Application
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.LinkedList
import java.util.Locale
import java.util.Queue

class TtsViewModel(application: Application):AndroidViewModel(application),OnInitListener {
    private val _isTtsInitialized = MutableLiveData<Boolean>()
    val isTtsInitialized: LiveData<Boolean> get() = _isTtsInitialized

     private val _doneId = MutableLiveData<String?>()
    val doneId: LiveData<String?> get() = _doneId

    private val tts: TextToSpeech = TextToSpeech(application, this)
    private var textsQueue: Queue<String> = LinkedList()

    init {
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {

            }

            override fun onDone(utteranceId: String?) {
                if (textsQueue.isNotEmpty()) {
                    val nextText = textsQueue.poll()
                    tts.speak(nextText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
                }
                _doneId.postValue(utteranceId)
            }

            override fun onError(utteranceId: String?) {}
        })
    }
    fun resetDoneId() {
        _doneId.value = null
    }
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.KOREAN)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            } else {
                _isTtsInitialized.value = true
            }
        } else {
            _isTtsInitialized.value = false
        }
    }
    fun oneSpeakOut(text: String) {
        if (isTtsInitialized.value == true) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, text)
        }
    }
    override fun onCleared() {
        super.onCleared()
        tts.stop()
        tts.shutdown()
    }
    fun stop(){
        tts.stop()
    }

}