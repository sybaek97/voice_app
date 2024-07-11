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
import androidx.navigation.fragment.findNavController
import com.baek.voice.R
import com.baek.voice.application.BaseFragment
import com.baek.voice.common.PermissionRequestHandler
import com.baek.voice.common.SharedPreferenceHelper
import com.baek.voice.databinding.FragmentHomeBinding
import com.baek.voice.viewModel.PermissionViewModel
import com.baek.voice.viewModel.SttViewModel
import com.baek.voice.viewModel.TtsViewModel

class HomeFragment : BaseFragment() {
    override var isBackAvailable: Boolean = false
    private lateinit var binding: FragmentHomeBinding
    private val TAG = javaClass.simpleName
    private val ttsViewModel: TtsViewModel by viewModels()
    private val sttViewModel: SttViewModel by viewModels()
    private val permissionViewModel: PermissionViewModel by viewModels()
    private var talkBack: Boolean = false
    private val AUX:String = "aux"
    private var auxStatus:Boolean=false

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
        talkBack = isTalkBackEnabled(requireContext())
        permissionCheck()
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auxStatus= SharedPreferenceHelper(requireContext(),AUX,false).prefGetter() as Boolean
        val fadeInUpAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in_up)
        binding.titleText.startAnimation(fadeInUpAnimation)
        binding.lifecycleOwner = this


        PermissionRequestHandler.initializePermissionLauncher(this)
        PermissionRequestHandler.requestAudioPermission(this)

//        permissionCheck()
        updateButtonState()




        binding.auxBtn.setOnClickListener{
            ttsViewModel.oneSpeakOut(getString(R.string.main_audio))
            updateButtonToggle()
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
        sttViewModel.recognizedText.observe(viewLifecycleOwner){sttId->
            when(sttId){
                "1번","추천 도서 대출"->{
                    binding.topBooksLoanBtn.performClick()}
                "2번","행사 안내"->{
                    binding.eventInfoBtn.performClick()}
                "3번","다시 듣기"->{
                    binding.audioReplayBtn.performClick()}
                else -> {
                    ttsViewModel.oneSpeakOut(getString(R.string.stt_retry_message))
                    sttViewModel.startListening()
                }
            }
        }

    }
    private fun permissionCheck(){


        permissionViewModel.permissionStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                PermissionViewModel.PermissionStatus.GRANTED -> {
                    ttsViewModel.isTtsInitialized.observe(viewLifecycleOwner) { isInitialized ->
                        if (isInitialized == true) {
                            ttsViewModel.oneSpeakOut(getString(R.string.intro_audio))
                            //이어폰 연결이 안되어 있다면 aux 연결 안내 멘트
                            auxCheck()
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
    private fun auxCheck(){
        ttsViewModel.doneId.observe(viewLifecycleOwner){speakId->
            if(speakId==getString(R.string.intro_audio)){
                if(!auxStatus){
                    ttsViewModel.oneSpeakOut(getString(R.string.intro_aux_audio))
                }else{
                    //이어폰이 연결되어 있다면 바로 메뉴 설명
                    ttsViewModel.oneSpeakOut(getString(R.string.main_audio))

                }
            }else if(speakId==getString(R.string.main_audio)){ //메뉴 설명이 끝난 뒤 STT실행

                sttViewModel.startListening()
                ttsViewModel.resetDoneId()
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


    private fun updateButtonToggle() {
        if(auxStatus){
            binding.auxBtn.backgroundTintList= ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.sub))
            binding.auxBtn.text="OFF"
            SharedPreferenceHelper(requireContext(),AUX,false).prefSetter()
        }else{
            binding.auxBtn.backgroundTintList= ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.text_brown))
            binding.auxBtn.text="ON"
            SharedPreferenceHelper(requireContext(),AUX,true).prefSetter()

        }
    }
    private fun updateButtonState() {
        if(auxStatus){
            binding.auxBtn.backgroundTintList= ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.text_brown))
            binding.auxBtn.text="ON"
        }else{
            binding.auxBtn.backgroundTintList= ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.sub))
            binding.auxBtn.text="OFF"
        }
    }
}