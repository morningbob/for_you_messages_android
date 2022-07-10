package com.bitpunchlab.android.foryoumessages.account

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bitpunchlab.android.foryoumessages.CreateAccountAppState
import com.bitpunchlab.android.foryoumessages.R
import com.bitpunchlab.android.foryoumessages.databinding.FragmentCreateAccountBinding
import com.bitpunchlab.android.foryoumessages.firebaseClient.FirebaseClientViewModel
import com.bitpunchlab.android.foryoumessages.firebaseClient.FirebaseClientViewModelFactory


class CreateAccountFragment : Fragment() {

    private var _binding : FragmentCreateAccountBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseClientViewModel: FirebaseClientViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateAccountBinding.inflate(inflater, container, false)
        firebaseClientViewModel = ViewModelProvider(requireActivity(), FirebaseClientViewModelFactory(requireActivity()))
            .get(FirebaseClientViewModel::class.java)


        binding.lifecycleOwner = viewLifecycleOwner
        binding.firebaseClient = firebaseClientViewModel

        firebaseClientViewModel.readyRegisterLiveData.observe(viewLifecycleOwner, Observer { ready ->
            if (ready) {
                Log.i("ready register test", "ready")
                binding.buttonSend.visibility = View.VISIBLE
            } else {
                Log.i("ready register test", "not ready")
                binding.buttonSend.visibility = View.INVISIBLE
            }
        })

        binding.buttonSend.setOnClickListener {
            // register the user in Firebase Auth and Firebase Database
            firebaseClientViewModel.registerNewUser()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val createAccountAppStateObserver = Observer<CreateAccountAppState> { appState ->
        when (appState) {
            CreateAccountAppState.NORMAL -> 0
            CreateAccountAppState.READY_REGISTER -> 1
            CreateAccountAppState.REGISTER_SUCCESS -> {
                // alert user of registration success
                // reset fields
                //firebaseClientViewModel.resetAllFields()
            }
            CreateAccountAppState.RESET -> 2
            else -> {
                
            }
        }
    }
}