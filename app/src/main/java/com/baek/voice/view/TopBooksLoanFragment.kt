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
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private val bookListViewModel: BookListViewModel by viewModels()
    private lateinit var adapter: BookAdapter
    private var backKeyPressedTime: Long = 0

    private lateinit var bookList: Array<String>
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
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bookList = resources.getStringArray(R.array.top_book_list)
        bookListText = ""


        ttsViewModel.isTtsInitialized.observe(viewLifecycleOwner) { isInitialized ->
            if (isInitialized == true) {
                ttsViewModel.oneSpeakOut(getString(R.string.top_books_main_audio))
            }
        }

        val recyclerView = binding.recyclerBookList
        layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager

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

        bookListTTS()

        sttViewModel.recognizedText.observe(viewLifecycleOwner) { sttId ->
            if(sttId=="다시 듣기"){
                bookListTTS()

            }else{
                for (i in bookList.indices) {
                    if (sttId == "${i + 1}번"||sttId== bookList[i]) {
                        sttViewModel.stopListening()
                        val position = adapter.getItemPosition(bookList[i])
                        if (position != RecyclerView.NO_POSITION) {
                            val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
                            viewHolder?.itemView?.performClick()
                        }
                        break
                    }
                }
            }
        }

    }

    private fun bookListTTS() {
        ttsViewModel.doneId.observe(viewLifecycleOwner) {
            if (it == getString(R.string.top_books_main_audio)) {
                for (i in bookList.indices) {
                    if (i == bookList.size - 1) {
                        bookListText += "${i + 1}번은 ${bookList[i]} 입니다.${getString(R.string.top_books_main_menu_audio)}" +
                                "다시듣기를 원하시면 다시듣기를 말씀해주세요."
                        break
                    }
                    bookListText += "${i + 1}번은 ${bookList[i]}. "
                }
                ttsViewModel.oneSpeakOut(bookListText)
            } else if (it == bookListText) {
                Log.d("tset", "onViewCreated: $bookListText")
                sttViewModel.startListening()
            }
        }
    }


}