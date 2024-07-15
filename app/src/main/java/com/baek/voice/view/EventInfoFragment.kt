package com.baek.voice.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baek.voice.R
import com.baek.voice.adapter.EventAdapter
import com.baek.voice.application.BaseFragment
import com.baek.voice.common.findIndexed
import com.baek.voice.databinding.FragmentEventInfoBinding
import com.baek.voice.viewModel.EventListViewModel
import com.baek.voice.viewModel.SttViewModel
import com.baek.voice.viewModel.TtsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class EventInfoFragment : BaseFragment() {

    override var isBackAvailable: Boolean = true
    private lateinit var binding: FragmentEventInfoBinding
    private val TAG = javaClass.simpleName

    private val ttsViewModel: TtsViewModel by viewModels()
    private val sttViewModel: SttViewModel by viewModels()
    private val eventViewModel: EventListViewModel by viewModels()


    private lateinit var adapter: EventAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var eventListText: String


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        monitorTextToSpeech()
        sttViewModel.resetRecognizedText()
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_event_info, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ttsViewModel.isTtsInitialized.observe(viewLifecycleOwner) { isInitialized ->
            if (isInitialized) {
                ttsViewModel.oneSpeakOut(getString(R.string.event_info_main_audio))
            }
        }
        recyclerView = binding.recyclerEventList
        layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager

        clickItem()
        monitorSpeechToText()

    }



    /** 기기 음성 인식 결과*/
    private fun monitorTextToSpeech() {
        ttsViewModel.doneId.removeObservers(viewLifecycleOwner)

        ttsViewModel.doneId.observe(viewLifecycleOwner) {
            if (it == getString(R.string.event_info_main_audio)) {
                readEventList()

            } else if (it == eventListText) {
                sttViewModel.startListening()
                ttsViewModel.resetDoneId()
            }else if(it==getString(R.string.stt_retry_message)){
                ttsViewModel.oneSpeakOut(eventListText)
            }

        }
    }



    /** 사용자 음성 인식 결과*/
    private fun monitorSpeechToText(){
        sttViewModel.recognizedText.observe(viewLifecycleOwner) { sttId ->
            if (sttViewModel.isResetting && sttId.isEmpty()) {
                sttViewModel.completeResetting()
                return@observe // 무시하고 빠져나가기
            }
            lifecycleScope.launch {
                delay(1000)
                when (sttId) {
                    "다시 듣기" -> readEventList()
                    "뒤로 가기" -> {
                        findNavController().popBackStack()
                        sttViewModel.resetRecognizedText()
                    }
                    else -> handleSttCommand(sttId)
                }
            }

        }
    }


    private fun readEventList() {
        eventViewModel.data.value?.let { eventList ->
            eventListText = ""
            for (i in eventList.indices) {
                if (i == eventList.size - 1) {
                    eventListText += "${i + 1}번은 ${eventList[i]} 입니다.${getString(R.string.event_info_main_menu_audio)}" +
                            "다시듣기를 원하시면 다시듣기를 말씀해주세요."
                    break
                }
                eventListText += "${i + 1}번은 ${eventList[i]}. "
            }
            ttsViewModel.oneSpeakOut(eventListText)
        }

    }
    private fun clickItem() {

        eventViewModel.data.observe(viewLifecycleOwner) { data ->
            adapter = EventAdapter(data, object : EventAdapter.OnItemClickListener {
                override fun onItemClick(item: String) {
                    ttsViewModel.stop()
                    val action =
                        EventInfoFragmentDirections.actionEventInfoFragmentToEventScreenFragment(
                            item
                        )
                    findNavController().navigate(action)
                }
            })
            recyclerView.adapter = adapter
        }
    }
    private fun handleSttCommand(sttId: String) {
        eventViewModel.data.value?.let { eventList ->
            val matchedEvent = eventList.findIndexed { index, event ->
                sttId == "${index + 1}번" ||
                        sttId == "${index + 1}본" ||
                        sttId == event ||
                        sttId == "${index + 1}번 $event" || sttId == "${index + 1}본 $event"
            }

            if (matchedEvent != null) {
                sttViewModel.stopListening()
                val position = adapter.getItemPosition(matchedEvent)
                if (position != RecyclerView.NO_POSITION) {
                    ttsViewModel.stop()
                    // Item 클릭 이벤트 트리거
                    val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
                    viewHolder?.itemView?.performClick()
                    val action = EventInfoFragmentDirections.actionEventInfoFragmentToEventScreenFragment(matchedEvent)
                    findNavController().navigate(action)
                }
                sttViewModel.resetRecognizedText()
            }else{
                lifecycleScope.launch {
                    delay(2000)
                    ttsViewModel.oneSpeakOut(getString(R.string.stt_retry_message))
                }
            }
        }

    }

}