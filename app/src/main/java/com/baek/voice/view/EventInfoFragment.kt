package com.baek.voice.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baek.voice.R
import com.baek.voice.adapter.EventAdapter
import com.baek.voice.application.BaseFragment
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_event_info, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = binding.recyclerEventList
        layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager
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


        ttsViewModel.isTtsInitialized.observe(viewLifecycleOwner) { isInitialized ->
            if (isInitialized) {
                ttsViewModel.oneSpeakOut(getString(R.string.event_info_main_audio))
            }
        }


        val bookList = resources.getStringArray(R.array.event_info_list)
        var bookListText = ""

        ttsViewModel.doneId.observe(viewLifecycleOwner) {
            if (it == getString(R.string.event_info_main_audio)) {
                for (i in bookList.indices) {
                    if (i == bookList.size - 1) {
                        bookListText += "${i + 1}번은 ${bookList[i]} 입니다.${getString(R.string.event_info_main_menu_audio)}" +
                                "다시듣기를 원하시면 다시듣기를 말씀해주세요."
                        break
                    }
                    bookListText += "${i + 1}번은 ${bookList[i]}. "

                }
                ttsViewModel.oneSpeakOut(bookListText)

            } else if (it == bookListText) {
                sttViewModel.startListening()
            }

        }


    }



}