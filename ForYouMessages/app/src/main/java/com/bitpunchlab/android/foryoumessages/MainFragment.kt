package com.bitpunchlab.android.foryoumessages

import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bitpunchlab.android.foryoumessages.databinding.FragmentMainBinding
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey

private const val KEY_ALIAS = "mainYou"


class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null


    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentMainBinding.inflate(inflater, container, false)

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonCreateKeys.setOnClickListener {
            createAsymmetricKeyPair()
            val keyPair = getAsymmetricKeyPair()
            Log.i("Key Pair, private: ", keyPair?.private.toString())
            Log.i("Key Pair, public: ", keyPair?.public.toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

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
}