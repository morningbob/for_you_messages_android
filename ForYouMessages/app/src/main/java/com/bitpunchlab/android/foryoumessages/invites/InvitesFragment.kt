package com.bitpunchlab.android.foryoumessages.invites

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import com.bitpunchlab.android.foryoumessages.ContactsList
import com.bitpunchlab.android.foryoumessages.R
import com.bitpunchlab.android.foryoumessages.contacts.ContactsViewModel
import com.bitpunchlab.android.foryoumessages.databinding.FragmentInvitesBinding
import com.bitpunchlab.android.foryoumessages.firebaseClient.FirebaseClientViewModel
import com.bitpunchlab.android.foryoumessages.firebaseClient.FirebaseClientViewModelFactory


class InvitesFragment : Fragment() {

    private var _binding : FragmentInvitesBinding? = null
    private val binding get() = _binding!!
    private lateinit var invitesAdapter: InvitesAdapter
    private lateinit var contactsViewModel: ContactsViewModel
    private lateinit var firebaseClient: FirebaseClientViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInvitesBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        firebaseClient = ViewModelProvider(requireActivity(), FirebaseClientViewModelFactory(requireActivity()))
            .get(FirebaseClientViewModel::class.java)

        // the invites list from firestore just list the emails
        // we need to get the contact object for the user
        // that needs too many queries.  I'm going to change
        // all lists to use contact objects instead of emails

        contactsViewModel = ViewModelProvider(requireActivity())
            .get(ContactsViewModel::class.java)

        // we retrieve the user's contacts from the database
        firebaseClient.retrieveContacts(ContactsList.REQUESTED_CONTACT)

        invitesAdapter = InvitesAdapter(AcceptOnClickListener { contact ->
            contactsViewModel.onContactClicked(contact)
        },
        RejectOnClickListener { contact -> {
            contactsViewModel.onContactClicked(contact)
        } })
        binding.invitesRecycler.adapter = invitesAdapter

        contactsViewModel.invites.observe(viewLifecycleOwner, Observer { inviteList ->
            inviteList?.let {
                invitesAdapter.submitList(inviteList)
                invitesAdapter.notifyDataSetChanged()
            }
        })

        // we get the latest contact list from firestore and save it in contact view model
        // the adapter only pay attention to contact view model
        firebaseClient.userContacts.observe(viewLifecycleOwner, Observer { contacts ->
            Log.i("invites contacts", "contacts size: ${contacts.size}")
            contactsViewModel._invites.value = contacts
        })

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}