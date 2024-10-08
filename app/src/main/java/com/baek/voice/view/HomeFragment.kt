package com.baek.voice.view

import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.content.res.ColorStateList
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityManager
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.baek.voice.R
import com.baek.voice.application.BaseFragment
import com.baek.voice.common.PermissionRequestHandler
import com.baek.voice.common.SharedPreferenceHelper
import com.baek.voice.databinding.FragmentHomeBinding
import com.baek.voice.viewModel.PermissionViewModel
import com.baek.voice.viewModel.SttViewModel
import com.baek.voice.viewModel.TtsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeFragment : BaseFragment() {
    override var isBackAvailable: Boolean = false
    private lateinit var binding: FragmentHomeBinding
    private val TAG = javaClass.simpleName
    private val ttsViewModel: TtsViewModel by viewModels()
    private val sttViewModel: SttViewModel by viewModels()
    private val permissionViewModel: PermissionViewModel by viewModels()
    private var talkBack: Boolean = false
    private val AUX:String = "aux"
    private var auxBtnState:Boolean=false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_home, container,
            false
        )
        sttViewModel.resetRecognizedText()
        talkBack = isTalkBackEnabled(requireContext())
        permissionCheck()
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fadeInUpAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in_up)
        binding.titleText.startAnimation(fadeInUpAnimation)
        binding.lifecycleOwner = this


        PermissionRequestHandler.initializePermissionLauncher(this)
        PermissionRequestHandler.requestAudioPermission(this)



        auxBtnState=SharedPreferenceHelper(requireContext(),AUX,false).prefGetter() as Boolean
        updateUI(auxBtnState)

        monitorSpeechToText()

        binding.auxBtn.setOnClickListener{
            auxBtnState = !auxBtnState
            updateUI(auxBtnState)
        }
        binding.eventInfoBtn.setOnClickListener {
            sttViewModel.stopListening()
            ttsViewModel.stop()
            findNavController().navigate(R.id.action_homeFragment_to_eventInfoFragment)
        }
        binding.topBooksLoanBtn.setOnClickListener {
            ttsViewModel.stop()
            sttViewModel.stopListening()
            findNavController().navigate(R.id.action_homeFragment_to_topBooksLoanFramgnet)
        }

        binding.audioReplayBtn.setOnClickListener{
            ttsViewModel.oneSpeakOut(getString(R.string.main_audio))
            sttViewModel.stopListening()
        }


    }



    /** 사용자 음성 인식 결과*/
    private fun monitorSpeechToText(){
        sttViewModel.recognizedText.observe(viewLifecycleOwner){sttId->
            if (sttViewModel.isResetting && sttId.isEmpty()) {
                sttViewModel.completeResetting()
                return@observe
            }

            when(sttId){
                "1번","일본","1본","추천 도서 대출","일본 추천 도서 대출","일번 추천 도서 대출","1번 추천 도서 대출","추천도서 대출","추천도서대출"->{
                    binding.topBooksLoanBtn.performClick()
                    sttViewModel.resetRecognizedText()
                }
                "2번","이본","2본","행사안내","이번 행사 안내","2번 행사안내","행사 안내","2번 행사 안내"->{
                    binding.eventInfoBtn.performClick()
                    sttViewModel.resetRecognizedText()
                }

                "3번","삼본","3본","다시듣기","3번 다시듣기","삼번 다시 듣기","다시 듣기","3번 다시 듣기"->{
                    binding.audioReplayBtn.performClick()
                    sttViewModel.resetRecognizedText()
                }
                else-> {
                    lifecycleScope.launch {
                        delay(2000)
                        ttsViewModel.oneSpeakOut(getString(R.string.stt_retry_message))
                    }
                }
            }

        }
    }

    /** 기기 음성 인식 결과*/
    private fun monitorTextToSpeech(){
        ttsViewModel.doneId.observe(viewLifecycleOwner){speakId->
            if(speakId==getString(R.string.intro_audio)){
                if(!auxBtnState){
                    ttsViewModel.oneSpeakOut(getString(R.string.intro_aux_audio))
                }else{
                    //이어폰이 연결되어 있다면 바로 메뉴 설명
                    ttsViewModel.oneSpeakOut(getString(R.string.main_audio))

                }
            }else if(speakId==getString(R.string.main_audio)){ //메뉴 설명이 끝난 뒤 STT실행
                lifecycleScope.launch {
                    delay(1000)
                    sttViewModel.startListening()
                    ttsViewModel.resetDoneId()
                }

            }else if(speakId==getString(R.string.stt_retry_message)){
                ttsViewModel.oneSpeakOut(getString(R.string.main_audio))
            }
        }
    }


    /** 권한 체크*/
    private fun permissionCheck(){
        permissionViewModel.permissionStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                PermissionViewModel.PermissionStatus.GRANTED -> {
                    ttsViewModel.isTtsInitialized.observe(viewLifecycleOwner) { isInitialized ->
                        if (isInitialized == true) {
                            ttsViewModel.oneSpeakOut(getString(R.string.intro_audio))
                            //이어폰 연결이 안되어 있다면 aux 연결 안내 멘트
                            monitorTextToSpeech()
                        }
                    }
                }

                PermissionViewModel.PermissionStatus.DENIED_ONCE, PermissionViewModel.PermissionStatus.DENIED_TWICE -> {
                    if (talkBack) {
                        PermissionRequestHandler.requestAudioPermission(this)
                    } else {
                        ttsViewModel.oneSpeakOut(getString(R.string.permission_audio))
                        PermissionRequestHandler.requestAudioPermission(this)
                    }
                }



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



    private fun updateUI(isOn: Boolean) {
        if (isOn) {
            ttsViewModel.oneSpeakOut(getString(R.string.main_audio))
            binding.auxBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.text_brown))
            SharedPreferenceHelper(requireContext(), AUX, true).prefSetter()
            binding.auxBtn.text = "이어폰 연결 상태: ON"
        } else {
            binding.auxBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.sub))
            SharedPreferenceHelper(requireContext(), AUX, false).prefSetter()
            binding.auxBtn.text = "이어폰 연결 상태: OFF"
        }
    }

}