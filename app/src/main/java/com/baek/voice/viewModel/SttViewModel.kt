package com.baek.voice.viewModel

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.accessibility.AccessibilityManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.baek.voice.R
import java.util.*

class SttViewModel(application: Application) : AndroidViewModel(application),
    TextToSpeech.OnInitListener {

    private val _recognizedText = MutableLiveData<String>()
    val recognizedText: LiveData<String> get() = _recognizedText

    private val _permissionRequestNeeded = MutableLiveData<Boolean>()
    val permissionRequestNeeded: LiveData<Boolean> get() = _permissionRequestNeeded
    private val _shouldShowPermissionRationale = MutableLiveData<Boolean>()
    val shouldShowPermissionRationale: LiveData<Boolean> get() = _shouldShowPermissionRationale
    private val speechRecognizer: SpeechRecognizer =
        SpeechRecognizer.createSpeechRecognizer(application)
    private lateinit var tts: TextToSpeech

    init {
        tts = TextToSpeech(application, this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                _recognizedText.value = "Error: $error"
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    _recognizedText.value = matches[0]
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.KOREAN
        }
    }

    fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
        }
        speechRecognizer.startListening(intent)
    }
    fun handlePermissionResult(granted: Boolean) {
        if (!granted) {
            _shouldShowPermissionRationale.value = true
        }
    }
    fun checkPermission(context: Context) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (isTalkBackEnabled(context)) {
                speakOut(context, context.getString(R.string.permission_audio))
                Handler(Looper.getMainLooper()).postDelayed({
                    _permissionRequestNeeded.value = true
                }, 3000)
            } else {
                _permissionRequestNeeded.value = true
            }
        }
    }

    private fun isTalkBackEnabled(context: Context): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return enabledServices.contains("TalkBack") || am.isTouchExplorationEnabled
    }

    fun speakOut(context: Context, message: String) {
        tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer.destroy()
        tts.stop()
        tts.shutdown()
    }
}