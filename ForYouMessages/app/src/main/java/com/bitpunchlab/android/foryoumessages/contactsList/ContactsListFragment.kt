package com.bitpunchlab.android.foryoumessages.contactsList

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bitpunchlab.android.foryoumessages.ContactsList
import com.bitpunchlab.android.foryoumessages.ContactsTypeTitleMap
import com.bitpunchlab.android.foryoumessages.R
import com.bitpunchlab.android.foryoumessages.contacts.ContactOnClickListener
import com.bitpunchlab.android.foryoumessages.contacts.ContactsAdapter
import com.bitpunchlab.android.foryoumessages.contacts.ContactsViewModel
import com.bitpunchlab.android.foryoumessages.databinding.FragmentAcceptedContactsBinding
import com.bitpunchlab.android.foryoumessages.databinding.FragmentContactsListBinding
import com.bitpunchlab.android.foryoumessages.firebaseClient.FirebaseClientViewModel
import com.bitpunchlab.android.foryoumessages.firebaseClient.FirebaseClientViewModelFactory


class ContactsListFragment : Fragment() {

    private var _binding : FragmentContactsListBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseClient : FirebaseClientViewModel
    private lateinit var contactsListAdapter: ContactsAdapter
    private lateinit var contactsViewModel: ContactsViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentContactsListBinding.inflate(inflater, container, false)
        firebaseClient = ViewModelProvider(requireActivity(), FirebaseClientViewModelFactory(requireActivity()))
            .get(FirebaseClientViewModel::class.java)
        contactsViewModel = ViewModelProvider(requireActivity()).get(ContactsViewModel::class.java)
        contactsListAdapter = ContactsAdapter(ContactOnClickListener { contact ->
            contactsViewModel.onContactClicked(contact)
        })
        val currentContactType = requireArguments().getParcelable<ContactsList>("contactType")
        if (currentContactType == null) {
            Log.i("contact list fragment", "error, can't get contacts list type.")
            findNavController().popBackStack()
        }
        // clear previous result, that might be requested list, accepted list
        firebaseClient.userContacts.value = null
        firebaseClient.retrieveContacts(currentContactType!!)
        binding.textviewTitle.text = ContactsTypeTitleMap[currentContactType]
        binding.lifecycleOwner = viewLifecycleOwner
        binding.contactsListRecycler.adapter = contactsListAdapter

        firebaseClient.userContacts.observe(viewLifecycleOwner, Observer { contacts ->
            contactsListAdapter.submitList(contacts)
            contactsListAdapter.notifyDataSetChanged()
        })

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}