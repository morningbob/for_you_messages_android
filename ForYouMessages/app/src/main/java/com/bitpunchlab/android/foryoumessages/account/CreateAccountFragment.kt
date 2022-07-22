package com.bitpunchlab.android.foryoumessages.account

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bitpunchlab.android.foryoumessages.CreateAccountAppState
import com.bitpunchlab.android.foryoumessages.R
import com.bitpunchlab.android.foryoumessages.databinding.FragmentCreateAccountBinding
import com.bitpunchlab.android.foryoumessages.firebaseClient.FirebaseClientViewModel
import com.bitpunchlab.android.foryoumessages.firebaseClient.FirebaseClientViewModelFactory


class CreateAccountFragment : Fragment() {

    private var _binding : FragmentCreateAccountBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseClientViewModel: FirebaseClientViewModel
    private val navigateToHome = MutableLiveData<Boolean>(false)

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

        observeReadyRegister()
        firebaseClientViewModel.createAccountAppState.observe(viewLifecycleOwner, createAccountAppStateObserver)

        binding.buttonSend.setOnClickListener {
            // register the user in Firebase Auth and Firebase Database
            firebaseClientViewModel.prepareToRegisterNewUser()
        }

        navigateToHome.observe(viewLifecycleOwner, Observer { navigate ->
            if (navigate) {
                findNavController().navigate(R.id.action_createAccountFragment_to_MainFragment)
            }
        })
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeReadyRegister() {
        firebaseClientViewModel.readyRegisterLiveData.observe(viewLifecycleOwner, Observer { ready ->
            ready?.let {
                if (ready) {
                    Log.i("ready register test", "ready")
                    binding.buttonSend.visibility = View.VISIBLE
                } else {
                    Log.i("ready register test", "not ready")
                    binding.buttonSend.visibility = View.INVISIBLE
                }
            }
        })
    }

    private val createAccountAppStateObserver = Observer<CreateAccountAppState> { appState ->
        when (appState) {
            CreateAccountAppState.NORMAL -> 0
            CreateAccountAppState.READY_REGISTER -> 1
            CreateAccountAppState.AUTH_REGISTRATION_SUCCESS -> {
                firebaseClientViewModel.createAndSaveNewUser()
            }
            CreateAccountAppState.SAME_EMAIL_ERROR -> {
                firebaseClientViewModel.clearSameEmail()
                sameEmailAlert()
            }
            CreateAccountAppState.SAME_PHONE_ERROR -> {
                firebaseClientViewModel.clearSamePhone()
                samePhoneAlert()
            }
            CreateAccountAppState.REGISTRATION_ERROR -> {
                registerErrorAlert()
                firebaseClientViewModel.createAccountAppState.value = CreateAccountAppState.RESET
            }
            CreateAccountAppState.REGISTRATION_SUCCESS -> {
                // alert user of registration success
                registerSuccessAlert()
                firebaseClientViewModel.createAccountAppState.value = CreateAccountAppState.RESET
                navigateToHome.value = true
            }
            CreateAccountAppState.RESET -> {
                firebaseClientViewModel.resetAllFields()
                //firebaseClientViewModel.createAccountAppState.value = CreateAccountAppState.LOGGED_IN
            }
            CreateAccountAppState.LOGGED_IN -> {
                // navigate to main fragment
                //findNavController().navigate(R.id.action_createAccountFragment_to_MainFragment)
            }
            else -> 0
        }
    }

    private fun registerSuccessAlert() {
        val successAlert = AlertDialog.Builder(requireContext())

        successAlert.setTitle(getString(R.string.registration_success_alert_title))
        successAlert.setMessage(getString(R.string.registration_success_alert_desc))

        successAlert.setPositiveButton(getString(R.string.ok),
            DialogInterface.OnClickListener { dialog, button ->
                // do nothing
            })

        successAlert.show()
    }

    private fun registerErrorAlert() {
        val errorAlert = AlertDialog.Builder(requireContext())

        errorAlert.setTitle(getString(R.string.registration_failure_alert_title))
        errorAlert.setMessage(getString(R.string.registration_failure_alert_desc))

        errorAlert.setPositiveButton(getString(R.string.ok),
            DialogInterface.OnClickListener { dialog, button ->
                // do nothing
            })

        errorAlert.show()
    }

    private fun sameEmailAlert() {
        val errorAlert = AlertDialog.Builder(requireContext())

        errorAlert.setTitle("Registration Error")
        errorAlert.setMessage("There is error in registration.  The email already exists.  Please try to login or use another email.")

        errorAlert.setPositiveButton(getString(R.string.ok),
            DialogInterface.OnClickListener { dialog, button ->
                // do nothing
            })

        errorAlert.show()
    }

    private fun samePhoneAlert() {
        val errorAlert = AlertDialog.Builder(requireContext())

        errorAlert.setTitle("Registration Error")
        errorAlert.setMessage("There is error in registration.  The phone number is already registered.  Please recover the account and login.")

        errorAlert.setPositiveButton(getString(R.string.ok),
            DialogInterface.OnClickListener { dialog, button ->
                // do nothing
            })

        errorAlert.show()
    }
}