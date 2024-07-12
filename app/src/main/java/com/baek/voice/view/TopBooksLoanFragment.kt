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
import com.baek.voice.adapter.BookAdapter
import com.baek.voice.application.BaseFragment
import com.baek.voice.databinding.FragmentTopBooksLoanBinding
import com.baek.voice.viewModel.BookListViewModel
import com.baek.voice.viewModel.SttViewModel
import com.baek.voice.viewModel.TtsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TopBooksLoanFragment : BaseFragment() {
    override var isBackAvailable: Boolean = true
    private val TAG = javaClass.simpleName
    private lateinit var binding: FragmentTopBooksLoanBinding
    private val ttsViewModel: TtsViewModel by viewModels()
    private val sttViewModel: SttViewModel by viewModels()
    private val bookListViewModel: BookListViewModel by viewModels()


    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BookAdapter

    private lateinit var bookListText: String


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_top_books_loan, container,
            false
        )
        bookListTTS()

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TTS 초기화 여부 관찰자 설정
        ttsViewModel.isTtsInitialized.observe(viewLifecycleOwner) { isInitialized ->
            if (isInitialized == true) {
                ttsViewModel.oneSpeakOut(getString(R.string.top_books_main_audio))
            }
        }

        recyclerView = binding.recyclerBookList
        layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager


        clickItem()



        sttViewModel.recognizedText.observe(viewLifecycleOwner) { sttId ->
            binding.titleText.text = sttId
            lifecycleScope.launch {
                delay(1000)
                when (sttId) {
                    "다시 듣기" -> recycler()
                    "뒤로 가기" -> {
                        findNavController().popBackStack()
                        sttViewModel.resetRecognizedText()

                    }

                    else -> handleSttCommand(sttId)
                }
            }

        }

    }

    private fun bookListTTS() {
        ttsViewModel.doneId.removeObservers(viewLifecycleOwner)

        ttsViewModel.doneId.observe(viewLifecycleOwner) {

            if (it == getString(R.string.top_books_main_audio)) {
                recycler()
            } else if (it == bookListText) {
                sttViewModel.startListening()
            }
        }
    }


    /** 책 목록 TTS로 읽기*/
    private fun recycler() {
        bookListViewModel.data.value?.let { bookList ->
            bookListText = ""
            for (i in bookList.indices) {
                if (i == bookList.size - 1) {
                    bookListText += "${i + 1}번은 ${bookList[i]} 입니다.${getString(R.string.top_books_main_menu_audio)}" +
                            "다시듣기를 원하시면 다시듣기를 말씀해주세요."
                    break
                }
                bookListText += "${i + 1}번은 ${bookList[i]}. "
            }
            ttsViewModel.oneSpeakOut(bookListText)
        }

    }

    private fun handleSttCommand(sttId: String) {

        bookListViewModel.data.value?.let { bookList ->
            for (i in bookList.indices) {
                if (sttId == "${i + 1}번" ||
                    sttId == "${i + 1}본" ||
                    sttId == bookList[i] ||
                    sttId == "${i + 1}번 ${bookList[i]}" || sttId == "${i + 1}본 ${bookList[i]}") {
                    sttViewModel.stopListening()
                    val position = adapter.getItemPosition(bookList[i])
                    if (position != RecyclerView.NO_POSITION) {
                        ttsViewModel.stop()
                        // Item 클릭 이벤트 트리거
                        sttViewModel.resetRecognizedText()
                        val viewHolder =
                            recyclerView.findViewHolderForAdapterPosition(position)
                        viewHolder?.itemView?.performClick()

                        val action =
                            TopBooksLoanFragmentDirections.actionTopBooksLoanFramgnetToRobotFragment(
                                bookList[i]
                            )
                        findNavController().navigate(action)

                    }
                    break
                } else if (sttId.isNotEmpty()) {
                    ttsViewModel.oneSpeakOut(getString(R.string.stt_retry_message))
                    lifecycleScope.launch {
                        delay(1000)
                        sttViewModel.startListening()
                    }
                }
            }
        }
    }

    private fun clickItem() {

        bookListViewModel.data.observe(viewLifecycleOwner) { data ->
            adapter = BookAdapter(data, object : BookAdapter.OnItemClickListener {
                override fun onItemClick(item: String) {
                    ttsViewModel.stop()
                    val action =
                        TopBooksLoanFragmentDirections.actionTopBooksLoanFramgnetToRobotFragment(
                            item
                        )
                    findNavController().navigate(action)
                }
            })
            recyclerView.adapter = adapter
        }
    }

}