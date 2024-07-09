package com.baek.voice.view

import android.content.Context
import android.content.res.ColorStateList
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
import androidx.lifecycle.Observer
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
    private var isButtonOn: Boolean = false
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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fadeInUpAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in_up)
        binding.titleText.startAnimation(fadeInUpAnimation)
        binding.lifecycleOwner = this


        PermissionRequestHandler.initializePermissionLauncher(this)
        PermissionRequestHandler.requestAudioPermission(this)
        permissionViewModel.permissionStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                PermissionViewModel.PermissionStatus.GRANTED -> {
                    ttsViewModel.isTtsInitialized.observe(viewLifecycleOwner) { isInitialized ->
                        if (isInitialized == true) {
                            ttsViewModel.speakOut(getString(R.string.intro_audio))
                            /** AUX 선 유무 확인 후*/
                            ttsViewModel.oneSpeakOut(getString(R.string.main_audio))

                        }
                    }
                }

                PermissionViewModel.PermissionStatus.DENIED_ONCE, PermissionViewModel.PermissionStatus.DENIED_TWICE -> {
                    if (talkBack) {
                        PermissionRequestHandler.requestAudioPermission(this)
                    } else {
                        ttsViewModel.speakOut(getString(R.string.permission_audio))
                        PermissionRequestHandler.requestAudioPermission(this)
                    }
                }

            }
        }

        if(SharedPreferenceHelper(requireContext(),"aux",false).prefGetter() as Boolean){
            binding.auxBtn.backgroundTintList= ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.text_brown))
            binding.auxBtn.text="OFF"
        }else{
            binding.auxBtn.backgroundTintList= ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.sub))
            binding.auxBtn.text="ON"
        }

        sttViewModel.recognizedText.observe(viewLifecycleOwner, Observer { text ->
            binding.textView.text = text
        })



        binding.auxBtn.setOnClickListener{
            isButtonOn = !isButtonOn
            updateButtonState()
        }
        binding.eventInfoBtn.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_eventInfoFragment)
        }
        binding.topBooksLoanBtn.setOnClickListener {
            ttsViewModel.stop()
            findNavController().navigate(R.id.action_homeFragment_to_topBooksLoanFramgnet)
        }

        binding.audioReplayBtn.setOnClickListener{
            ttsViewModel.oneSpeakOut(getString(R.string.main_audio))
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


    private fun updateButtonState() {
        if (isButtonOn) {
            binding.auxBtn.backgroundTintList= ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.sub))
            binding.auxBtn.text="ON"
            SharedPreferenceHelper(requireContext(),"aux",true).prefSetter()
        } else {
            binding.auxBtn.backgroundTintList= ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.text_brown))
            binding.auxBtn.text="OFF"
            SharedPreferenceHelper(requireContext(),"aux",false).prefSetter()
        }
    }
}