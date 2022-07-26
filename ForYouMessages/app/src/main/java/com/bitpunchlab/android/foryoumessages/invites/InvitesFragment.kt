package com.bitpunchlab.android.foryoumessages.invites

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
import androidx.lifecycle.get
import com.bitpunchlab.android.foryoumessages.AcceptContactAppState
import com.bitpunchlab.android.foryoumessages.ContactsList
import com.bitpunchlab.android.foryoumessages.R
import com.bitpunchlab.android.foryoumessages.RejectContactAppState
import com.bitpunchlab.android.foryoumessages.contacts.ContactsViewModel
import com.bitpunchlab.android.foryoumessages.databinding.FragmentInvitesBinding
import com.bitpunchlab.android.foryoumessages.firebaseClient.FirebaseClientViewModel
import com.bitpunchlab.android.foryoumessages.firebaseClient.FirebaseClientViewModelFactory
import com.bitpunchlab.android.foryoumessages.models.Contact


class InvitesFragment : Fragment() {

    private var _binding : FragmentInvitesBinding? = null
    private val binding get() = _binding!!
    private lateinit var invitesAdapter: InvitesAdapter
    private lateinit var contactsViewModel: ContactsViewModel
    private lateinit var firebaseClient: FirebaseClientViewModel
    private var targetContact : Contact? = null

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
        firebaseClient.retrieveContacts(ContactsList.INVITES)

        invitesAdapter = InvitesAdapter(AcceptOnClickListener { contact ->
            //contactsViewModel.onContactClicked(contact)
            Log.i("Invites fragment","the accept button is clicked")
            firebaseClient.acceptInvite(contact)

        },
        RejectOnClickListener { contact ->
            //contactsViewModel.onContactClicked(contact)
            Log.i("Invites fragment","the reject button is clicked")
            firebaseClient.rejectInvite(contact)
        })
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

        firebaseClient.acceptContactAppState.observe(viewLifecycleOwner, acceptContactAppStateObserver)
        firebaseClient.rejectContactAppState.observe(viewLifecycleOwner, rejectContactAppStateObserver)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val acceptContactAppStateObserver = Observer<AcceptContactAppState> { appState ->
        appState?.let {
            when (appState) {
                AcceptContactAppState.ASK_CONFIRMATION -> {
                    confirmAcceptAlert(firebaseClient.appInviterContact!!)
                }
                // clear the contact here from the invites list
                AcceptContactAppState.CONFIRMED_ACCEPTANCE -> {
                    targetContact?.let {
                        contactsViewModel.removeContact(targetContact!!)
                    }
                }
                else -> 0
            }
        }
    }

    private val rejectContactAppStateObserver = Observer<RejectContactAppState> { appState ->
        appState?.let {
            when (appState) {
                RejectContactAppState.ASK_CONFIRMATION -> {
                    confirmRejectAlert(firebaseClient.appInviterContact!!)
                }
                // clear the contact here from the invites list
                RejectContactAppState.CONFIRMED_REJECTION -> {
                    targetContact?.let {
                        contactsViewModel.removeContact(targetContact!!)
                    }
                }
                else -> 0
            }
        }
    }

    private fun confirmAcceptAlert(contact: Contact) {
        val confirmAlert = AlertDialog.Builder(requireContext())

        confirmAlert.setCancelable(false)
        confirmAlert.setPositiveButton("Confirm",
            DialogInterface.OnClickListener { dialog, button ->
                firebaseClient.acceptContactAppState.value = AcceptContactAppState.CONFIRMED_ACCEPTANCE
                // clear the contact from the invites list
            })
        confirmAlert.setNegativeButton(getString(R.string.cancel),
            DialogInterface.OnClickListener { dialog, button ->
                // do nothing
            })
        confirmAlert.show()
    }

    private fun confirmRejectAlert(contact: Contact) {
        val confirmAlert = AlertDialog.Builder(requireContext())

        confirmAlert.setCancelable(false)
        confirmAlert.setPositiveButton("Confirm",
            DialogInterface.OnClickListener { dialog, button ->
                firebaseClient.rejectContactAppState.value = RejectContactAppState.CONFIRMED_REJECTION

            })
        confirmAlert.setNegativeButton(getString(R.string.cancel),
            DialogInterface.OnClickListener { dialog, button ->
                // do nothing
            })
        confirmAlert.show()
    }
}