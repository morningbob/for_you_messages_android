package com.bitpunchlab.android.foryoumessages

import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.bitpunchlab.android.foryoumessages.contacts.ContactsViewModel
import com.bitpunchlab.android.foryoumessages.contacts.ContactsViewModelFactory
import com.bitpunchlab.android.foryoumessages.database.ForYouDatabase
import com.bitpunchlab.android.foryoumessages.firebaseClient.FirebaseClientViewModel
import com.bitpunchlab.android.foryoumessages.firebaseClient.FirebaseClientViewModelFactory
import com.bitpunchlab.android.foryoumessages.databinding.FragmentMainBinding
import kotlinx.coroutines.InternalCoroutinesApi
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey

private const val KEY_ALIAS = "mainYou"


class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseClient: FirebaseClientViewModel
    private lateinit var contactsViewModel: ContactsViewModel
    private lateinit var localDatabase: ForYouDatabase

    @OptIn(InternalCoroutinesApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        setHasOptionsMenu(true)

        _binding = FragmentMainBinding.inflate(inflater, container, false)
        firebaseClient = ViewModelProvider(requireActivity(), FirebaseClientViewModelFactory(requireActivity()))
            .get(FirebaseClientViewModel::class.java)
        localDatabase = ForYouDatabase.getInstance(requireContext())
        contactsViewModel = ViewModelProvider(requireActivity(), ContactsViewModelFactory(localDatabase,
            firebaseClient.currentUserEntity.value!!.userID))
            .get(ContactsViewModel::class.java)
        //binding.lifeCyclerOwner = viewLifecycleOwner
        binding.user = firebaseClient.currentUserEntity.value

        firebaseClient.loginAppState.observe(viewLifecycleOwner, loginAppStateObserver)

        binding.buttonCreateKeys.setOnClickListener {
            createAsymmetricKeyPair()
            val keyPair = getAsymmetricKeyPair()
            Log.i("Key Pair, private: ", keyPair?.private.toString())
            Log.i("Key Pair, public: ", keyPair?.public.toString())
        }

        binding.buttonLogout.setOnClickListener {
            firebaseClient.logoutUser()
        }

        // here, will be triggered whenever the app login the user
        // we notice contactsVM to load the correct user
        firebaseClient.currentUserEntity.observe(viewLifecycleOwner, Observer { currentUser ->
            currentUser?.let {
                contactsViewModel.getUserLocalDatabase(currentUser.userID)
            }
        })

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout -> {
                firebaseClient.logoutUser()
                //firebaseClient.loginAppState.value = LoginAppState.LOGGED_OUT
                firebaseClient.createAccountAppState.value = CreateAccountAppState.NORMAL
                true
            }
            R.id.toContacts -> {
                findNavController().navigate(R.id.action_MainFragment_to_contactsFragment)
                true
            }
            else -> NavigationUI.onNavDestinationSelected(item,
                requireView().findNavController())
                    || super.onOptionsItemSelected(item)
        }
    }
/*
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return NavigationUI.onNavDestinationSelected(item,
            requireView().findNavController())
                || super.onOptionsItemSelected(item)
    }
*/
    private fun generateKeys() {

    }

    fun createAsymmetricKeyPair(): KeyPair {
        var generator: KeyPairGenerator

        //if (hasMarshmallow()) {
            generator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore")
            val builder = KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
                //.setUserAuthenticationRequired(true)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)

            generator.initialize(builder.build())
        //} else {
            //generator = KeyPairGenerator.getInstance("RSA")
            //generator.initialize(2048)
        //}

        return generator.generateKeyPair()
    }

    fun getAsymmetricKeyPair(): KeyPair? {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        val privateKey = keyStore.getKey(KEY_ALIAS, null) as PrivateKey?
        val publicKey = keyStore.getCertificate(KEY_ALIAS)?.publicKey

        return if (privateKey != null && publicKey != null) {
            KeyPair(publicKey, privateKey)
        } else {
            null
        }
    }

    private var loginAppStateObserver = Observer<LoginAppState> { appState ->
        when (appState) {
            LoginAppState.LOGGED_OUT -> {
                // return to login page, or pop off self
                findNavController().popBackStack()
                Log.i("main, app state", "logged out once")
            }
            LoginAppState.LOGGED_IN -> {
                // we display the user's name in home page
                binding
            }
            else -> {

            }
        }
    }
}