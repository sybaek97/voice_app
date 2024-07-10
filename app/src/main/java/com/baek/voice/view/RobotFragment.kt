package com.baek.voice.view

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.baek.voice.R
import com.baek.voice.application.BaseFragment
import com.baek.voice.common.SharedPreferenceHelper
import com.baek.voice.databinding.FragmentRobotBinding
import com.baek.voice.viewModel.TtsViewModel

class RobotFragment: BaseFragment(){
    override var isBackAvailable: Boolean = true
    private lateinit var binding : FragmentRobotBinding
    private val TAG = javaClass.simpleName
    private val args: RobotFragmentArgs by navArgs()
    private val ttsViewModel : TtsViewModel by viewModels()
    private val AUX:String = "aux"
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding= DataBindingUtil.inflate(inflater, R.layout.fragment_robot,container,false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val selectedItem = args.selectedItem
        binding.lifecycleOwner = this
        ttsViewModel.isTtsInitialized.observe(viewLifecycleOwner){
            if(it==true){
                if(SharedPreferenceHelper(requireContext(),AUX,false).prefGetter() as Boolean){
                    ttsViewModel.oneSpeakOut("${selectedItem}${getString(R.string.book_choice_audio)} ${getString(R.string.book_choice_robot_audio)}")
                    binding.auxBtn.apply{
                        visibility= View.VISIBLE
                        text="버튼을 눌러 이어폰을 제거"
                        backgroundTintList= ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.sub))
                        SharedPreferenceHelper(requireContext(),AUX,false).prefSetter()
                    }


                }else{
                    ttsViewModel.oneSpeakOut("${selectedItem}${getString(R.string.book_choice_audio)}")
                }
            }
        }

        binding.auxBtn.setOnClickListener{
            ttsViewModel.oneSpeakOut(getString(R.string.book_choice_robot_aux_check_audio))
            binding.auxBtn.visibility= View.GONE

        }


        //만약 aux선이 연결되어있지 않을 경우 바로 로봇 출발
        ttsViewModel.doneId.observe(viewLifecycleOwner){voiceId->
            if(voiceId=="${selectedItem}${getString(R.string.book_choice_audio)}"){
                ttsViewModel.oneSpeakOut(getString(R.string.book_choice_robot_aux_check_audio))
            }
        }



    }

}