package com.baek.voice.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.baek.voice.R
import com.baek.voice.application.BaseFragment
import com.baek.voice.databinding.FragmentTopBooksLoanBinding
import com.baek.voice.viewModel.TtsViewModel

class TopBooksLoanFragment : BaseFragment(){
    override var isBackAvailable: Boolean = true
    private val TAG = javaClass.simpleName
    private lateinit var binding: FragmentTopBooksLoanBinding
    private val ttsViewModel: TtsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= DataBindingUtil.inflate(inflater, R.layout.fragment_top_books_loan,container,
            false
        )
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ttsViewModel.isTtsInitialized.observe(viewLifecycleOwner) { isInitialized ->
            if (isInitialized == true) {
                ttsViewModel.oneSpeakOut(getString(R.string.top_books_main_audio))
                ttsViewModel.speakOut(getString(R.string.top_books_main_menu_audio))

            }
        }

    }
}