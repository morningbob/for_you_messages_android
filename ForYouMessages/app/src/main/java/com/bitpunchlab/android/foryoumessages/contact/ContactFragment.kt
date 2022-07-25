package com.bitpunchlab.android.foryoumessages.contact

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bitpunchlab.android.foryoumessages.DeleteContactAppState
import com.bitpunchlab.android.foryoumessages.R
import com.bitpunchlab.android.foryoumessages.databinding.FragmentContactBinding
import com.bitpunchlab.android.foryoumessages.databinding.FragmentContactsBinding
import com.bitpunchlab.android.foryoumessages.firebaseClient.FirebaseClientViewModel
import com.bitpunchlab.android.foryoumessages.firebaseClient.FirebaseClientViewModelFactory
import com.bitpunchlab.android.foryoumessages.models.Contact
import com.bitpunchlab.android.foryoumessages.models.ContactEntity


class ContactFragment : Fragment() {

    private var _binding : FragmentContactBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseClient: FirebaseClientViewModel
    private var contact : Contact? = null
    //private lateinit var

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentContactBinding.inflate(inflater, container, false)
        firebaseClient = ViewModelProvider(requireActivity(), FirebaseClientViewModelFactory(requireActivity()))
            .get(FirebaseClientViewModel::class.java)

        val contactEntity = requireArguments().getParcelable<ContactEntity>("contact")
        contact = Contact(name = contactEntity!!.contactName,
            email = contactEntity!!.contactEmail,
            phone = contactEntity!!.contactPhone)

        binding.buttonDelete.setOnClickListener {
            firebaseClient.deleteContactAppState.value = DeleteContactAppState.ASK_CONFIRMATION
        }

        firebaseClient.deleteContactAppState.observe(viewLifecycleOwner, deleteContactAppStateObserver)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val deleteContactAppStateObserver = Observer<DeleteContactAppState> { appState ->
        when (appState) {
            DeleteContactAppState.ASK_CONFIRMATION -> {
                deleteContactAlert()
            }
            DeleteContactAppState.CONFIRMED_DELETION -> {
                firebaseClient.deleteContact(contact!!)
            }
            else -> 0
        }
    }

    private fun deleteContactAlert() {
        val deleteAlert = AlertDialog.Builder(requireContext())
        deleteAlert.setCancelable(false)
        deleteAlert.setTitle("Delete Contact")
        deleteAlert.setMessage("Are you sure you want to delete the contact ${contact!!.contactName}")

        deleteAlert.setPositiveButton("Confirm",
            DialogInterface.OnClickListener { dialog, button ->
                firebaseClient.deleteContactAppState.value = DeleteContactAppState.CONFIRMED_DELETION
            })

        deleteAlert.setNegativeButton(getString(R.string.cancel),
            DialogInterface.OnClickListener { dialog, button ->
                // do nothing
            })
        deleteAlert.show()
    }
}