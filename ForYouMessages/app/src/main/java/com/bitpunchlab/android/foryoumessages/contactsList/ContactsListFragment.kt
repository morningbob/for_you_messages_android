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
import com.bitpunchlab.android.foryoumessages.databinding.FragmentAcceptedContactsBinding
import com.bitpunchlab.android.foryoumessages.databinding.FragmentContactsListBinding
import com.bitpunchlab.android.foryoumessages.firebaseClient.FirebaseClientViewModel
import com.bitpunchlab.android.foryoumessages.firebaseClient.FirebaseClientViewModelFactory
import com.bitpunchlab.android.foryoumessages.models.Contact


class ContactsListFragment : Fragment() {

    private var _binding : FragmentContactsListBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseClient : FirebaseClientViewModel
    private lateinit var contactsListAdapter: ContactsAdapter
    private lateinit var contactsViewModel: ContactsViewModel
    private var contactTypeList = MutableLiveData<List<Contact>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        _binding = FragmentContactsListBinding.inflate(inflater, container, false)
        firebaseClient = ViewModelProvider(requireActivity(), FirebaseClientViewModelFactory(requireActivity()))
            .get(FirebaseClientViewModel::class.java)
        // if the user object or the contact object is still null after retrieving from
        // auth, we'll get it again here.
        firebaseClient.getUserAndContactObject()
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
        //firebaseClient.userContacts.value = null
        //firebaseClient.retrieveContacts(currentContactType!!)
        // get the corresponding list from user entity
        //firebaseClient.currentUserEntity.observe(viewLifecycleOwner, Observer { currentUser ->
        //    contactsViewModel._currentTypeContactList.value =
        //})
        contactTypeList.value = contactsViewModel.contactsTypeHashmap[currentContactType]

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