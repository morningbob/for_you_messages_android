package com.bitpunchlab.android.foryoumessages.contacts

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.view.*
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.bitpunchlab.android.foryoumessages.*
import com.bitpunchlab.android.foryoumessages.databinding.FragmentContactsBinding
import com.bitpunchlab.android.foryoumessages.firebaseClient.FirebaseClientViewModel
import com.bitpunchlab.android.foryoumessages.firebaseClient.FirebaseClientViewModelFactory
import com.bitpunchlab.android.foryoumessages.models.ContactEntity


class ContactsFragment : Fragment() {

    private var _binding: FragmentContactsBinding? = null
    private val binding get() = _binding!!
    private lateinit var contactsAdapter: ContactsAdapter
    private lateinit var contactsViewModel: ContactsViewModel
    private lateinit var firebaseClient: FirebaseClientViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        _binding = FragmentContactsBinding.inflate(inflater, container, false)
        contactsViewModel = ViewModelProvider(requireActivity())
            .get(ContactsViewModel::class.java)

        firebaseClient = ViewModelProvider(requireActivity(), FirebaseClientViewModelFactory(requireActivity()))
            .get(FirebaseClientViewModel::class.java)

        // we retrieve the user's contacts from the database
        firebaseClient.retrieveContacts(ContactsList.USER_CONTACTS)

        contactsAdapter = ContactsAdapter(ContactOnClickListener { contact ->
            // here onclick should show the write message fragment
            contactsViewModel.onContactClicked(contact)
        })

        binding.lifecycleOwner = viewLifecycleOwner
        binding.contactsRecycler.adapter = contactsAdapter

        contactsViewModel.contacts.observe(viewLifecycleOwner, Observer { contacts ->
            contactsAdapter.submitList(contacts)
            contactsAdapter.notifyDataSetChanged()
        })

        // here, when the contact is clicked, we present the options in an alert
        // for the user to choose.  Like delete, write message
        contactsViewModel.chosenContact.observe(viewLifecycleOwner, Observer { chosen ->
            val contact = ContactEntity(contactEmail = chosen.contactEmail,
                contactName = chosen.contactName,
                contactPhone = chosen.contactPhone)
            val bundle = Bundle()
            bundle.putParcelable("contact", contact)
            //bundle.
            findNavController().navigate(R.id.action_contactsFragment_to_contactFragment, bundle)
        })

        firebaseClient.requestContactAppState.observe(viewLifecycleOwner, requestContactAppStateObserver)
        //firebaseClient.deleteContactAppState.observe(viewLifecycleOwner, )

        // we get the latest contact list from firestore and save it in contact view model
        // the adapter only pay attention to contact view model
        firebaseClient.userContacts.observe(viewLifecycleOwner, Observer { contacts ->
            contactsViewModel.contacts.value = contacts
        })

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_contacts, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.requestContact -> {
                requestContactAlert()
                true
            }
            R.id.logout -> {
                firebaseClient.logoutUser()
                // this is the case when user created the account and use it immediately
                // we need to get back to normal
                firebaseClient.createAccountAppState.value = CreateAccountAppState.NORMAL
                true
            }
            else -> NavigationUI.onNavDestinationSelected(item,
                requireView().findNavController())
                    || super.onOptionsItemSelected(item)
        }
    }

    private fun requestContactAlert() {
        val requestAlert = AlertDialog.Builder(requireContext())
        val phoneEdittext = EditText(context)

        requestAlert.setCancelable(false)
        requestAlert.setTitle("Request to Add a user in Contacts")
        requestAlert.setMessage("Please enter the user's phone number.  Note that the person must be a user of this app before you can add him/her in the Contacts.  If the person is a user, he/she will be notified to accept or reject the request when he/she starts the app.  If the person accepts the request, we'll add him/her in your Contacts.")
        requestAlert.setView(phoneEdittext)

        requestAlert.setPositiveButton(getString(R.string.send),
            DialogInterface.OnClickListener { dialog, button ->
                val phone = phoneEdittext.text.toString()
                if (phone.isNullOrEmpty() ||
                        phone.count() < 10 ||
                        phone.count() > 13 ||
                    !PhoneNumberUtils.isGlobalPhoneNumber(phone)) {
                    Log.i("request alert", "invalid phone number.")
                    // empty phone alert
                    invalidPhoneAlert()
                } else {
                    //val phone = phoneEdittext.text.toString()
                    // search for the phone number in database\
                    Log.i("request alert", "valid phone number.")
                    firebaseClient.requestContact(phone)
                }
            })

        requestAlert.setNegativeButton(getString(R.string.cancel),
            DialogInterface.OnClickListener { dialog, button ->
                // do nothing
            })
        requestAlert.show()
    }

    private fun invalidPhoneAlert() {
        val errorAlert = AlertDialog.Builder(requireContext())

        errorAlert.setTitle("Invalid Phone Number")
        errorAlert.setMessage("The phone number is invalid.  ")

        errorAlert.setPositiveButton(getString(R.string.ok),
            DialogInterface.OnClickListener { dialog, button ->
                // do nothing
            })

        errorAlert.show()
    }

    private val requestContactAppStateObserver = Observer<RequestContactAppState> { appState ->
        when (appState) {
            RequestContactAppState.ASK_CONFIRMATION -> {
                confirmRequestAlert(firebaseClient.appInviteeContact!!.contactName)
            }
            RequestContactAppState.PHONE_NOT_FOUND -> {
                phoneNotFoundAlert()
            }
            RequestContactAppState.CONTACT_NOT_FOUND -> {
                contactNotFoundAlert()
            }
            else -> 0
        }
    }

    private val deleteContactAppStateObserver = Observer<DeleteContactAppState> { appState ->
        when (appState) {
            DeleteContactAppState.ASK_CONFIRMATION -> {
                //confirmDeleteAlert()
            }
            else -> 0
        }
    }

    private fun confirmRequestAlert(contactName: String) {
        val confirmAlert = AlertDialog.Builder(requireContext())
        confirmAlert.setCancelable(false)
        confirmAlert.setTitle("Confirm Request")
        confirmAlert.setMessage("Are you sure to request add ${contactName} in Contacts?")

        confirmAlert.setPositiveButton("Confirm",
            DialogInterface.OnClickListener { dialog, button ->
                firebaseClient.requestContactAppState.value = RequestContactAppState.CONFIRMED_REQUEST
            })

        confirmAlert.setNegativeButton(getString(R.string.cancel),
            DialogInterface.OnClickListener { dialog, button ->
                // do nothing
            })

        confirmAlert.show()
    }

    private fun phoneNotFoundAlert() {
        val notAlert = AlertDialog.Builder(requireContext())

        notAlert.setTitle("Phone Number Not Found")
        notAlert.setMessage("The phone number is not in our record.  He/She must be our user before you can add him/her to the contacts.")

        notAlert.setPositiveButton(getString(R.string.ok),
            DialogInterface.OnClickListener { dialog, button ->
                // do nothing
            })

        notAlert.show()
    }

    private fun contactNotFoundAlert() {
        val notAlert = AlertDialog.Builder(requireContext())

        notAlert.setTitle("Database Error")
        notAlert.setMessage("We can't get your information from the database currently.  Please try again later.")

        notAlert.setPositiveButton(getString(R.string.ok),
            DialogInterface.OnClickListener { dialog, button ->
                // do nothing
            })

        notAlert.show()
    }

    private fun confirmDeleteAlert(contactName: String) {
        val confirmAlert = AlertDialog.Builder(requireContext())
        confirmAlert.setCancelable(false)
        confirmAlert.setTitle("Confirm Delete")
        confirmAlert.setMessage("Are you sure to delete ${contactName} in Contacts?")

        confirmAlert.setPositiveButton(getString(R.string.confirm),
            DialogInterface.OnClickListener { dialog, button ->
                //firebaseClient.requestContactAppState.value = RequestContactAppState.CONFIRMED_REQUEST
                firebaseClient.deleteContactAppState.value = DeleteContactAppState.CONFIRMED_DELETION
            })

        confirmAlert.setNegativeButton(getString(R.string.cancel),
            DialogInterface.OnClickListener { dialog, button ->
                // do nothing
            })

        confirmAlert.show()
    }
}