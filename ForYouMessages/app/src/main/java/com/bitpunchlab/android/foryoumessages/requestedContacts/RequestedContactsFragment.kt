package com.bitpunchlab.android.foryoumessages.requestedContacts

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bitpunchlab.android.foryoumessages.ContactsList
import com.bitpunchlab.android.foryoumessages.R
import com.bitpunchlab.android.foryoumessages.contacts.ContactOnClickListener
import com.bitpunchlab.android.foryoumessages.contacts.ContactsAdapter
import com.bitpunchlab.android.foryoumessages.contacts.ContactsViewModel
import com.bitpunchlab.android.foryoumessages.databinding.FragmentRequestedContactsBinding
import com.bitpunchlab.android.foryoumessages.firebaseClient.FirebaseClientViewModel
import com.bitpunchlab.android.foryoumessages.firebaseClient.FirebaseClientViewModelFactory


class RequestedContactsFragment : Fragment() {

    private var _binding : FragmentRequestedContactsBinding? = null
    private val binding get() = _binding!!
    private lateinit var contactsViewModel: ContactsViewModel
    private lateinit var contactsAdapter: ContactsAdapter
    private lateinit var firebaseClient: FirebaseClientViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRequestedContactsBinding.inflate(inflater, container, false)
        contactsViewModel = ViewModelProvider(requireActivity()).get(ContactsViewModel::class.java)
        contactsAdapter = ContactsAdapter(ContactOnClickListener { contact ->
            contactsViewModel.onContactClicked(contact)
        })
        firebaseClient = ViewModelProvider(requireActivity(), FirebaseClientViewModelFactory(requireActivity()))
            .get(FirebaseClientViewModel::class.java)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.requestedRecycler.adapter = contactsAdapter

        firebaseClient.retrieveContacts(ContactsList.REQUESTED_CONTACT)

        contactsViewModel.requestedList.observe(viewLifecycleOwner, Observer { requested ->
            //if (requested != null || requested.size != 0)
            requested?.let {
                contactsAdapter.submitList(requested)
                contactsAdapter.notifyDataSetChanged()
            }
        })


        return binding.root
    }

}