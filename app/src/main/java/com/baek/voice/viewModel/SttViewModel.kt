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
import android.widget.Toast
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
    private var startMediaPlayer: MediaPlayer = MediaPlayer.create(application, R.raw.button01a)
    private var endMediaPlayer: MediaPlayer = MediaPlayer.create(application, R.raw.stt_sound)
    private var speechRecognizer: SpeechRecognizer =
        SpeechRecognizer.createSpeechRecognizer(application)
    private var isListening = false
    private var errorHandler = Handler(Looper.getMainLooper())
    var isResetting  = false

    init {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                startMediaPlayer.start()
                isListening = true
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
//                _recognizedText.value="empty"
                endMediaPlayer.start()
                isListening = false
            }
            override fun onError(error: Int) {
                val message = when (error) {

                    SpeechRecognizer.ERROR_AUDIO -> "오디오 에러"
                    SpeechRecognizer.ERROR_CLIENT -> "클라이언트 에러"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "퍼미션 없음"
                    SpeechRecognizer.ERROR_NETWORK -> "네트워크 에러"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "네트웍 타임아웃"
                    SpeechRecognizer.ERROR_NO_MATCH -> "찾을 수 없음"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RECOGNIZER 가 바쁨"
                    SpeechRecognizer.ERROR_SERVER -> "서버가 이상함"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "말하는 시간초과"
                    else -> "알 수 없는 오류임"
                }
                if (isListening) { // 플래그를 사용하여 중복 호출 방지
                    isListening = false
                    stopListening()
                    Toast.makeText(
                        application.applicationContext,
                        "에러 발생: $message",
                        Toast.LENGTH_SHORT
                    ).show()

                    errorHandler.postDelayed({
                        isListening = false
                    }, 2000)
                }
            }

            override fun onResults(results: Bundle?) {
                if (isResetting) {
                    return
                }
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty() && matches[0].isNotBlank()) {
                    _recognizedText.value = matches[0]
                } else {
                    Toast.makeText(application.applicationContext, "음성을 인식하지 못했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                }
                isListening = false // 결과 수신 후 플래그 해제

            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }



    fun startListening() {

//        soundPool.play(soundId, 1f, 1f, 0, 0, 1f)

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
        if (isListening) {
            speechRecognizer.stopListening()
            isListening = false // 플래그 해제
        }
    }

    fun resetRecognizedText() {
        isResetting = true
        _recognizedText.value = ""
    }

    fun completeResetting() {
        isResetting = false
    }


    override fun onCleared() {
        super.onCleared()
        speechRecognizer.destroy()
    }




}