package com.bitpunchlab.android.foryoumessages.account

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
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



        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}