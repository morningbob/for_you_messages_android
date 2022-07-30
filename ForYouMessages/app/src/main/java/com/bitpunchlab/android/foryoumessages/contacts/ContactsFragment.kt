package com.bitpunchlab.android.foryoumessages.contacts

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.view.*
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.bitpunchlab.android.foryoumessages.*
import com.bitpunchlab.android.foryoumessages.database.ForYouDatabase
import com.bitpunchlab.android.foryoumessages.databinding.FragmentContactsBinding
import com.bitpunchlab.android.foryoumessages.firebaseClient.FirebaseClientViewModel
import com.bitpunchlab.android.foryoumessages.firebaseClient.FirebaseClientViewModelFactory
import com.bitpunchlab.android.foryoumessages.models.Contact
import com.bitpunchlab.android.foryoumessages.models.ContactEntity
import com.bitpunchlab.android.foryoumessages.models.ContactListWithContacts
import com.bitpunchlab.android.foryoumessages.models.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch


class ContactsFragment : Fragment() {

    private var _binding: FragmentContactsBinding? = null
    private val binding get() = _binding!!
    private lateinit var contactsAdapter: ContactsAdapter
    private lateinit var contactsViewModel: ContactsViewModel
    private lateinit var firebaseClient: FirebaseClientViewModel
    private var contactToBeDeleted: ContactEntity? = null
    private lateinit var localDatabase: ForYouDatabase
    private var contacts = MutableLiveData<ContactListWithContacts>()
    private var userToBeUpdated = UserEntity()
    private lateinit var coroutineScope: CoroutineScope

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    @OptIn(InternalCoroutinesApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        _binding = FragmentContactsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        coroutineScope = CoroutineScope(Dispatchers.IO)
        contactsAdapter = ContactsAdapter(ContactOnClickListener { contact ->
            // here onclick should show the write message fragment
            contactsViewModel.onContactClicked(contact)
        })
        binding.contactsRecycler.adapter = contactsAdapter
        localDatabase = ForYouDatabase.getInstance(requireContext())
        firebaseClient = ViewModelProvider(requireActivity(), FirebaseClientViewModelFactory(requireActivity()))
            .get(FirebaseClientViewModel::class.java)
        // if the user object or the contact object is still null after retrieving from
        // auth, we'll get it again here.
        firebaseClient.getUserAndContactObject()

        contactsViewModel = ViewModelProvider(requireActivity(), ContactsViewModelFactory(localDatabase,
            firebaseClient.currentUserEntity.value!!.userID))
            .get(ContactsViewModel::class.java)

        // as the contactsVM will get the user entity object from the local database,
        // we need to observe whenever the it changes, to get our contacts
        contactsViewModel.user.observe(viewLifecycleOwner, Observer { currentUser ->
            currentUser?.let {
                contacts.value = currentUser.contactLists.find {
                    it.contactList.listName == "contacts"
                }

            }
        })

        contacts.observe(viewLifecycleOwner, Observer { contacts ->
            contactsAdapter.submitList(contacts.contacts)
            contactsAdapter.notifyDataSetChanged()
        })

        // here, when the contact is clicked, we present the options in an alert
        // for the user to choose.  Like delete, write message
        contactsViewModel.chosenContact.observe(viewLifecycleOwner, Observer { chosen ->
            chosen?.let {
                contactToBeDeleted = chosen
                val contact = ContactEntity(
                    contactEmail = chosen.contactEmail,
                    contactName = chosen.contactName,
                    contactPhone = chosen.contactPhone
                )
                val bundle = Bundle()
                bundle.putParcelable("contact", contact)

                contactsViewModel.finishedContact()

                findNavController().navigate(
                    R.id.action_contactsFragment_to_contactFragment,
                    bundle
                )
            }
        })

        firebaseClient.requestContactAppState.observe(viewLifecycleOwner, requestContactAppStateObserver)
        firebaseClient.deleteContactAppState.observe(viewLifecycleOwner, deleteContactAppStateObserver)

        // we get the latest contact list from firestore and save it in contact view model
        // the adapter only pay attention to contact view model
        firebaseClient.userContacts.observe(viewLifecycleOwner, Observer { contacts ->
            //contactsViewModel.contacts.value = contacts
        })

        firebaseClient.loginAppState.observe(viewLifecycleOwner, loginAppStateObserver)

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
            // invites fragment has it's own custom fragment
            R.id.toInvites -> {
                findNavController().navigate(R.id.action_contactsFragment_to_invitesFragment)
                true
            }
            // requested contacts uses general contact list fragment to display contacts
            R.id.toRequestedContacts -> {
                val bundle = Bundle()
                bundle.putParcelable("contactType", ContactsList.REQUESTED_CONTACT)
                findNavController()
                    .navigate(R.id.action_contactsFragment_to_contactsListFragment, bundle)
                true
            }
            R.id.toAcceptedContacts -> {
                val bundle = Bundle()
                bundle.putParcelable("contactType", ContactsList.ACCEPTED_CONTACT)
                findNavController()
                    .navigate(R.id.action_contactsFragment_to_contactsListFragment, bundle)
                true
            }
            R.id.toRejectedContacts -> {
                val bundle = Bundle()
                bundle.putParcelable("contactType", ContactsList.REJECTED_CONTACT)
                findNavController()
                    .navigate(R.id.action_contactsFragment_to_contactsListFragment, bundle)
                true
            }
            R.id.toDeletedContacts -> {
                val bundle = Bundle()
                bundle.putParcelable("contactType", ContactsList.DELETED_CONTACT)
                findNavController()
                    .navigate(R.id.action_contactsFragment_to_contactsListFragment, bundle)
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
                confirmDeleteAlert(contactToBeDeleted!!)
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

    private fun confirmDeleteAlert(contact: ContactEntity) {
        val confirmAlert = AlertDialog.Builder(requireContext())
        confirmAlert.setCancelable(false)
        confirmAlert.setTitle("Confirm Delete")
        confirmAlert.setMessage("Are you sure to delete ${contact.contactName} in Contacts?")

        confirmAlert.setPositiveButton(getString(R.string.confirm),
            DialogInterface.OnClickListener { dialog, button ->
                firebaseClient.deleteContactAppState.value = DeleteContactAppState.CONFIRMED_DELETION
                // here, we modify the user entity object, delete the contact and save in local
                // database
                //contactsViewModel.removeContact(contact)

            })

        confirmAlert.setNegativeButton(getString(R.string.cancel),
            DialogInterface.OnClickListener { dialog, button ->
                // do nothing
            })

        confirmAlert.show()
    }

    private var loginAppStateObserver = Observer<LoginAppState> { appState ->
        when (appState) {
            LoginAppState.LOGGED_OUT -> {
                // return to login page, or pop off self
                findNavController().popBackStack()
                Log.i("contacts fragment, app state", "logged out once")
            }
            else -> {

            }
        }
    }

    private fun updateUserInDatabase(contactToRemove: ContactEntity) {
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

            //userToBeUpdated = contactsViewModel.user.value!!.copy()
            //userToBeUpdated.contacts = contactsUpdated

            coroutineScope.launch {
                localDatabase.userDAO.insertUser(userToBeUpdated)
                Log.i("update user", "user saved in database")

            }
        }

    }
}