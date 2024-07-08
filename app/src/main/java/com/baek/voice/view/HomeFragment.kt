package com.baek.voice.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityManager
import android.view.animation.AnimationUtils
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.baek.voice.R
import com.baek.voice.application.BaseFragment
import com.baek.voice.databinding.FragmentHomeBinding
import com.baek.voice.viewModel.SttViewModel
import com.baek.voice.viewModel.TtsViewModel

class HomeFragment : BaseFragment() {
    override var isBackAvailable: Boolean = false
    private lateinit var binding: FragmentHomeBinding
    private val TAG = javaClass.simpleName
    private val viewModel: TtsViewModel by viewModels()
    private val sttViewModel: SttViewModel by viewModels()
    private var REQUEST_RECORD_AUDIO_PERMISSION = 0
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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fadeInUpAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in_up)
        binding.titleText.startAnimation(fadeInUpAnimation)
        binding.lifecycleOwner = this
        viewModel.isTtsInitialized.observe(viewLifecycleOwner) { isInitialized ->
            if (isInitialized == true) {
                viewModel.speakOut(getString(R.string.intro_audio))
            }
        }
        sttViewModel.recognizedText.observe(viewLifecycleOwner, Observer { text ->
            binding.textView.text = text
        })
        sttViewModel.permissionRequestNeeded.observe(viewLifecycleOwner, Observer { needed ->
            if (needed) {
                requestPermissions(
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    REQUEST_RECORD_AUDIO_PERMISSION
                )
            }
        })
        sttViewModel.shouldShowPermissionRationale.observe(viewLifecycleOwner, Observer { shouldShow ->
            if (shouldShow) {
                showPermissionRationale()
            }
        })
        binding.eventInfoBtn.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_eventInfoFragment)
        }
        binding.topBooksLoanBtn.setOnClickListener {
//            findNavController().navigate(R.id.action_homeFragment_to_topBooksLoanFramgnet)
            sttViewModel.startListening()

        }
        sttViewModel.checkPermission(requireContext())

    }
    private fun showPermissionRationale() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.RECORD_AUDIO)) {
            // 사용자에게 권한이 필요한 이유를 설명하는 안내
            sttViewModel.speakOut(requireContext(), this.getString(R.string.permission_audio))
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
        } else {
            // 다시 권한 요청
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            val permissionGranted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            sttViewModel.handlePermissionResult(permissionGranted)
        }
    }
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
//            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
//                sttViewModel.startListening()
//            }
//        }
//    }
}