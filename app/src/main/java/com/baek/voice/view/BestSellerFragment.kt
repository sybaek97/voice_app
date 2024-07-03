package com.baek.voice.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.baek.voice.R
import com.baek.voice.application.BaseFragment
import com.baek.voice.databinding.FragmentBestSellerBinding

class BestSellerFragment: BaseFragment(){
    override var isBackAvailable: Boolean = true
    private val TAG = javaClass.simpleName
    private lateinit var binding: FragmentBestSellerBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=DataBindingUtil.inflate(inflater, R.layout.fragment_best_seller,container,
            false
        )
        return binding.root

    }

}