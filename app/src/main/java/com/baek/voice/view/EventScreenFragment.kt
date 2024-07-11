package com.baek.voice.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.baek.voice.R
import com.baek.voice.application.BaseFragment
import com.baek.voice.databinding.FragmentEventInfoBinding
import com.baek.voice.databinding.FragmentEventScreenBinding
import com.baek.voice.viewModel.TtsViewModel

class EventScreenFragment: BaseFragment(){
    override var isBackAvailable: Boolean = true
    private lateinit var binding : FragmentEventScreenBinding
    private val TAG = javaClass.simpleName
    private val args: EventScreenFragmentArgs by navArgs()
    private val ttsViewModel : TtsViewModel by viewModels()
    private lateinit var selectedEvent :String
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding= DataBindingUtil.inflate(inflater, R.layout.fragment_event_screen,container,false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val selectedItem = args.selectedItem
        ttsViewModel.isTtsInitialized.observe(viewLifecycleOwner){
            if(it==true){
                selectedEvent="${selectedItem}${getString(R.string.selected_event_audio)}"
                ttsViewModel.oneSpeakOut(selectedEvent)
            }
        }
        ttsViewModel.doneId.observe(viewLifecycleOwner){speakId->
            if(speakId==selectedEvent){
                //행사 안내 다 끝냈다고 가정
                ttsViewModel.oneSpeakOut(getString(R.string.event_info_complete_audio))
            }else if(speakId==getString(R.string.event_info_complete_audio)){
                //행사 안내 종료멘트 후 홈으로 이동
                findNavController().navigate(R.id.action_eventScreenFragment_to_homeFragment)
            }
        }

    }
}