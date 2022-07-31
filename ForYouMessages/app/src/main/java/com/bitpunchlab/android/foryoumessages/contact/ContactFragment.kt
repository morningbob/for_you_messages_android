package com.bitpunchlab.android.foryoumessages.contact

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
import com.bitpunchlab.android.foryoumessages.DeleteContactAppState
import com.bitpunchlab.android.foryoumessages.R
import com.bitpunchlab.android.foryoumessages.contacts.ContactsViewModel
import com.bitpunchlab.android.foryoumessages.contacts.ContactsViewModelFactory
import com.bitpunchlab.android.foryoumessages.database.ForYouDatabase
import com.bitpunchlab.android.foryoumessages.databinding.FragmentContactBinding
import com.bitpunchlab.android.foryoumessages.databinding.FragmentContactsBinding
import com.bitpunchlab.android.foryoumessages.firebaseClient.FirebaseClientViewModel
import com.bitpunchlab.android.foryoumessages.firebaseClient.FirebaseClientViewModelFactory
import com.bitpunchlab.android.foryoumessages.models.Contact
import com.bitpunchlab.android.foryoumessages.models.ContactEntity
import com.bitpunchlab.android.foryoumessages.models.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch


class ContactFragment : Fragment() {

    private var _binding : FragmentContactBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseClient: FirebaseClientViewModel
    private lateinit var contactsViewModel: ContactsViewModel
    private var contact : ContactEntity? = null
    private lateinit var coroutineScope: CoroutineScope
    private lateinit var localDatabase: ForYouDatabase
    private var userToBeUpdated = UserEntity()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    @OptIn(InternalCoroutinesApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentContactBinding.inflate(inflater, container, false)
        localDatabase = ForYouDatabase.getInstance(requireContext())
        firebaseClient = ViewModelProvider(requireActivity(), FirebaseClientViewModelFactory(requireActivity()))
            .get(FirebaseClientViewModel::class.java)
        contactsViewModel = ViewModelProvider(requireActivity(), ContactsViewModelFactory(localDatabase,
            firebaseClient.currentUserEntity.value!!.userID))
            .get(ContactsViewModel::class.java)

        coroutineScope = CoroutineScope(Dispatchers.IO)
        //val contactEntity = requireArguments().getParcelable<ContactEntity>("contact")
        contact = requireArguments().getParcelable<ContactEntity>("contact")
        //contact = Contact(name = contactEntity!!.contactName,
        //    email = contactEntity!!.contactEmail,
        //    phone = contactEntity!!.contactPhone)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.contact = contact

        binding.buttonDelete.setOnClickListener {
            //firebaseClient.deleteContactAppState.value = DeleteContactAppState.ASK_CONFIRMATION
            firebaseClient.deleteContact(contact!!)
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
                Log.i("delete app state observer", "contact name: ${contact!!.contactName}")
                //firebaseClient.deleteContact(contact!!)
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
                updateUserInDatabase(contact!!)
                // and we need to pop off this fragment
                findNavController().popBackStack()
            })

        deleteAlert.setNegativeButton(getString(R.string.cancel),
            DialogInterface.OnClickListener { dialog, button ->
                // do nothing
            })
        deleteAlert.show()
    }

    private fun updateUserInDatabase(contactToRemove: ContactEntity) {
        /*
        if (contactsViewModel.user.value!!.contacts.isNotEmpty()) {
            var contactsUpdated = contactsViewModel.user.value!!.contacts.toMutableList()
            contactsUpdated.remove(contactToRemove)
            //userToBeUpdated.contacts = contactsUpdated
            userToBeUpdated = contactsViewModel.user.value!!.copy()
            userToBeUpdated.contacts = contactsUpdated

            coroutineScope.launch {
                localDatabase.userDAO.insertUser(userToBeUpdated)
                Log.i("update user", "user saved in database")
                // then, we pull from the local database, not remove contact manually
                // this is because we need to reflect the changes in the interface
                contactsViewModel.getUserLocalDatabase(firebaseClient.currentUserEntity.value!!.userID)
            }
        }

         */
        var originalContactList = contactsViewModel.user.value!!.contactLists.find {
            it.contactList.listName == "contacts"
        }!!
        if (originalContactList.contacts.isNotEmpty()) {
            var contactsUpdated = originalContactList.contacts.toMutableList()
            contactsUpdated.remove(contactToRemove)

            // so we are not doing it this way, the contacts lists are not the fields of
            // user entity anymore.  we can just modify the ContactListWithContacts
            // maybe, we can just modify the ContactListWithContacts, and save only that object
            originalContactList.contacts = contactsUpdated
            // now save it to database

            //userToBeUpdated = contactsViewModel.user.value!!.copy()
            //userToBeUpdated.contacts = contactsUpdated

            coroutineScope.launch {
                //localDatabase.userDAO.insertUser(userToBeUpdated)
                //Log.i("update user", "user saved in database")

            }
        }

    }
}