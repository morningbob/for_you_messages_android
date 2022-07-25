package com.bitpunchlab.android.foryoumessages.account

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bitpunchlab.android.foryoumessages.CreateAccountAppState
import com.bitpunchlab.android.foryoumessages.LoginAppState
import com.bitpunchlab.android.foryoumessages.R
import com.bitpunchlab.android.foryoumessages.firebaseClient.FirebaseClientViewModel
import com.bitpunchlab.android.foryoumessages.firebaseClient.FirebaseClientViewModelFactory


class LogoutFragment : Fragment() {

    private lateinit var firebaseClient : FirebaseClientViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        firebaseClient = ViewModelProvider(requireActivity(), FirebaseClientViewModelFactory(requireActivity()))
            .get(FirebaseClientViewModel::class.java)

        firebaseClient.logoutUser()

        firebaseClient.loginAppState.observe(viewLifecycleOwner, Observer { appState ->
            when (appState) {
                LoginAppState.LOGGED_OUT -> {
                    findNavController().popBackStack()
                    firebaseClient.createAccountAppState.value = CreateAccountAppState.NORMAL
                }
                else -> 0
            }
        })

        return inflater.inflate(R.layout.fragment_logout, container, false)
    }


}