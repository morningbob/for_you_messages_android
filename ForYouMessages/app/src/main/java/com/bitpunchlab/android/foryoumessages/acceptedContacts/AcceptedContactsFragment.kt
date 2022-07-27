package com.bitpunchlab.android.foryoumessages.acceptedContacts

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
import com.bitpunchlab.android.foryoumessages.databinding.FragmentAcceptedContactsBinding
import com.bitpunchlab.android.foryoumessages.firebaseClient.FirebaseClientViewModel
import com.bitpunchlab.android.foryoumessages.firebaseClient.FirebaseClientViewModelFactory


class AcceptedContactsFragment : Fragment() {

    private var _binding : FragmentAcceptedContactsBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseClient : FirebaseClientViewModel
    private lateinit var acceptedAdapter: ContactsAdapter
    private lateinit var contactsViewModel: ContactsViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAcceptedContactsBinding.inflate(inflater, container, false)
        firebaseClient = ViewModelProvider(requireActivity(), FirebaseClientViewModelFactory(requireActivity()))
            .get(FirebaseClientViewModel::class.java)
        contactsViewModel = ViewModelProvider(requireActivity()).get(ContactsViewModel::class.java)
        acceptedAdapter = ContactsAdapter(ContactOnClickListener { contact ->
            contactsViewModel.onContactClicked(contact)
        })
        firebaseClient.retrieveContacts(ContactsList.ACCEPTED_CONTACT)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.acceptedRecycler.adapter = acceptedAdapter

        contactsViewModel.acceptedList.observe(viewLifecycleOwner, Observer { accepted ->
            acceptedAdapter.submitList(accepted)
            acceptedAdapter.notifyDataSetChanged()
        })

        firebaseClient.userContacts.observe(viewLifecycleOwner, Observer { contacts ->
            contactsViewModel._acceptedList.value = contacts
        })

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}