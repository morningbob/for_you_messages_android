package com.bitpunchlab.android.foryoumessages.contactsList

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
import com.bitpunchlab.android.foryoumessages.ContactsList
import com.bitpunchlab.android.foryoumessages.ContactsTypeTitleMap
import com.bitpunchlab.android.foryoumessages.R
import com.bitpunchlab.android.foryoumessages.contacts.ContactOnClickListener
import com.bitpunchlab.android.foryoumessages.contacts.ContactsAdapter
import com.bitpunchlab.android.foryoumessages.contacts.ContactsViewModel
import com.bitpunchlab.android.foryoumessages.contacts.ContactsViewModelFactory
import com.bitpunchlab.android.foryoumessages.database.ForYouDatabase
import com.bitpunchlab.android.foryoumessages.databinding.FragmentAcceptedContactsBinding
import com.bitpunchlab.android.foryoumessages.databinding.FragmentContactsListBinding
import com.bitpunchlab.android.foryoumessages.firebaseClient.FirebaseClientViewModel
import com.bitpunchlab.android.foryoumessages.firebaseClient.FirebaseClientViewModelFactory
import com.bitpunchlab.android.foryoumessages.models.Contact
import kotlinx.coroutines.InternalCoroutinesApi


class ContactsListFragment : Fragment() {

    private var _binding : FragmentContactsListBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseClient : FirebaseClientViewModel
    private lateinit var contactsListAdapter: ContactsAdapter
    private lateinit var contactsViewModel: ContactsViewModel
    private var contactTypeList = MutableLiveData<List<Contact>>()
    private lateinit var localDatabase: ForYouDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    @OptIn(InternalCoroutinesApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentContactsListBinding.inflate(inflater, container, false)
        localDatabase = ForYouDatabase.getInstance(requireContext())
        firebaseClient = ViewModelProvider(requireActivity(), FirebaseClientViewModelFactory(requireActivity()))
            .get(FirebaseClientViewModel::class.java)
        // if the user object or the contact object is still null after retrieving from
        // auth, we'll get it again here.
        firebaseClient.getUserAndContactObject()
        contactsViewModel = ViewModelProvider(requireActivity(), ContactsViewModelFactory(localDatabase,
            firebaseClient.currentUserEntity.value!!.userID))
            .get(ContactsViewModel::class.java)
        contactsListAdapter = ContactsAdapter(ContactOnClickListener { contact ->
            contactsViewModel.onContactClicked(contact)
        })
        val currentContactType = requireArguments().getParcelable<ContactsList>("contactType")
        if (currentContactType == null) {
            Log.i("contact list fragment", "error, can't get contacts list type.")
            findNavController().popBackStack()
        }

        // config current contact list according to contact type
        contactsViewModel.user.observe(viewLifecycleOwner, Observer { currentUser ->
            currentUser?.let {
                val contactsTypeHashmap = HashMap<ContactsList, List<Contact>>().apply {
                    this[ContactsList.REQUESTED_CONTACT] = currentUser.requestedContacts
                    this[ContactsList.ACCEPTED_CONTACT] = currentUser.acceptedContacts
                    this[ContactsList.REJECTED_CONTACT] = currentUser.rejectedContacts
                    this[ContactsList.DELETED_CONTACT] = currentUser.deletedContacts
                }
                contactTypeList.value = contactsTypeHashmap[currentContactType]
            }
        })

        binding.textviewTitle.text = ContactsTypeTitleMap[currentContactType]

        binding.lifecycleOwner = viewLifecycleOwner
        binding.contactsListRecycler.adapter = contactsListAdapter

        contactTypeList.observe(viewLifecycleOwner, Observer { contacts ->
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