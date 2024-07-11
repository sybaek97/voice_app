package com.baek.voice.viewModel

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.accessibility.AccessibilityManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.baek.voice.R
import java.util.*

class SttViewModel(application: Application) : AndroidViewModel(application){

    private val _recognizedText = MutableLiveData<String>()
    val recognizedText: LiveData<String> get() = _recognizedText
    private lateinit var startMediaPlayer: MediaPlayer
    private lateinit var endMediaPlayer: MediaPlayer
    private lateinit var audioManager: AudioManager
    private var speechRecognizer: SpeechRecognizer =
        SpeechRecognizer.createSpeechRecognizer(application)

    init {

        // SoundPool 초기화
        audioManager = application.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        startMediaPlayer = MediaPlayer.create(application, R.raw.button01a)
        endMediaPlayer = MediaPlayer.create(application, R.raw.stt_sound)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                endMediaPlayer.start()

            }
            override fun onError(error: Int) {
                when(error){
                    5,7->stopListening()
                }
            }

            override fun onResults(results: Bundle?) {
                Log.e("SpeechRecognizer", "Error:")
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    _recognizedText.value = matches[0]
                }else{
                    Log.e("SpeechRecognizer", "Error:")

                }

            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }



    fun startListening() {

//        soundPool.play(soundId, 1f, 1f, 0, 0, 1f)


        startMediaPlayer.start()
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
        }
        speechRecognizer.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer.stopListening()
    }




    override fun onCleared() {
        super.onCleared()
        speechRecognizer.destroy()
    }




}