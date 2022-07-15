package com.bitpunchlab.android.foryoumessages.account

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bitpunchlab.android.foryoumessages.LoginAppState
import com.bitpunchlab.android.foryoumessages.R
import com.bitpunchlab.android.foryoumessages.databinding.FragmentLoginBinding
import com.bitpunchlab.android.foryoumessages.firebaseClient.FirebaseClientViewModel
import com.bitpunchlab.android.foryoumessages.firebaseClient.FirebaseClientViewModelFactory


class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseClient: FirebaseClientViewModel
    private var shouldNavigateToMainFragment = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        firebaseClient = ViewModelProvider(requireActivity(), FirebaseClientViewModelFactory(requireActivity()))
            .get(FirebaseClientViewModel::class.java)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.firebaseClient = firebaseClient

        firebaseClient.loginAppState.observe(viewLifecycleOwner, loginAppStateObserver)

        firebaseClient.readyLoginLiveData.observe(viewLifecycleOwner, Observer { ready ->
            ready?.let {
                if (ready) {
                    Log.i("ready login livedata", "true")
                    binding.buttonSend.visibility = View.VISIBLE
                } else {
                    Log.i("ready login livedata", "false")
                    binding.buttonSend.visibility = View.INVISIBLE
                }
            }
        })

        //firebaseClient.auth
        firebaseClient.loggedIn.observe(viewLifecycleOwner, Observer { login ->
            if (login) {

            }
        })

        binding.buttonSend.setOnClickListener {
            firebaseClient.authenticateUser()
        }

        binding.buttonCreateAccount.setOnClickListener {
            findNavController().navigate(R.id.action_LoginFragment_to_createAccountFragment)
        }

        return binding.root

    }

    private var loginAppStateObserver = Observer<LoginAppState> { appState ->
        when (appState) {
            LoginAppState.NORMAL -> 0
            LoginAppState.LOGGED_IN -> {
                Log.i("app state observer", "logged in state detected")
                // reset fields
                firebaseClient.resetAllFields()
                // navigate to main page of the app
                //Log.i("login observer", findNavController().currentDestination?.id.toString())
                //if (findNavController().currentDestination?.id == R.id.LoginFragment) {
                //if (shouldNavigateToMainFragment) {
                //    shouldNavigateToMainFragment = false
                    Log.i("app state", "navigate once")
                findNavController().navigate(R.id.action_LoginFragment_to_MainFragment)
                //}
                //}
            }
            LoginAppState.LOGIN_ERROR -> {
                loginErrorAlert()
                firebaseClient.loginAppState.value = LoginAppState.RESET
            }
            LoginAppState.LOGGED_OUT -> 0
            LoginAppState.RESET -> {
                //firebaseClient.resetAllFields()
                firebaseClient.clearSameEmail()
            }
            else -> 0
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        shouldNavigateToMainFragment = true
    }

    private fun loginErrorAlert() {
        val errorAlert = AlertDialog.Builder(requireContext())

        errorAlert.setTitle(getString(R.string.login_failure_alert_title))
        errorAlert.setMessage(getString(R.string.login_failure_alert_desc))

        errorAlert.setPositiveButton(getString(R.string.ok),
            DialogInterface.OnClickListener { dialog, button ->
                // do nothing
            })

        errorAlert.show()
    }


}