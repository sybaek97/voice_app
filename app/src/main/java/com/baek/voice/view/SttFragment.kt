package com.baek.voice.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.baek.voice.R
import com.baek.voice.application.BaseFragment
import com.baek.voice.databinding.FragmentSttBinding

class SttFragment : BaseFragment(){
    override var isBackAvailable: Boolean = true
    private lateinit var binding:FragmentSttBinding
    private val TAG = javaClass.simpleName

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=DataBindingUtil.inflate(inflater, R.layout.fragment_stt,container,false)
        return binding.root
    }
}