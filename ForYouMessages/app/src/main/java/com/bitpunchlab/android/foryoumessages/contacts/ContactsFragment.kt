package com.bitpunchlab.android.foryoumessages.contacts

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.view.*
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bitpunchlab.android.foryoumessages.LoginAppState
import com.bitpunchlab.android.foryoumessages.R
import com.bitpunchlab.android.foryoumessages.databinding.FragmentContactsBinding
import com.bitpunchlab.android.foryoumessages.firebaseClient.FirebaseClientViewModel
import com.bitpunchlab.android.foryoumessages.firebaseClient.FirebaseClientViewModelFactory


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

        contactsAdapter = ContactsAdapter(ContactOnClickListener { user ->
            contactsViewModel.onContactClicked(user)
        })

        binding.lifecycleOwner = viewLifecycleOwner
        binding.contactsRecycler.adapter = contactsAdapter

        contactsViewModel.contacts.observe(viewLifecycleOwner, Observer { contacts ->
            contactsAdapter.submitList(contacts)
            contactsAdapter.notifyDataSetChanged()
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
                //firebaseClient.requestContact()
                requestContactAlert()
                true
            }
            else -> super.onOptionsItemSelected(item)
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
                    // empty phone alert
                    invalidPhoneAlert()
                } else {
                    //val phone = phoneEdittext.text.toString()
                    // search for the phone number in database
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
}