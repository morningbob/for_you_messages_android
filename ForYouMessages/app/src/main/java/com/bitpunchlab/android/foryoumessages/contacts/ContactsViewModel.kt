package com.bitpunchlab.android.foryoumessages.contacts

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bitpunchlab.android.foryoumessages.ContactsList
import com.bitpunchlab.android.foryoumessages.models.Contact
import com.bitpunchlab.android.foryoumessages.models.ContactEntity
import com.bitpunchlab.android.foryoumessages.models.User
import com.bitpunchlab.android.foryoumessages.models.UserEntity

class ContactsViewModel : ViewModel() {

    var _contacts = MutableLiveData<List<Contact>>()
    val contacts get() = _contacts

    var _invites = MutableLiveData<List<Contact>>()
    val invites get() = _invites

    var _currentUser = MutableLiveData<UserEntity>(UserEntity())
    val currentUser get() = _currentUser

    var _currentTypeContactList = MutableLiveData<List<Contact>>()
    val currentTypeContactList get() = _currentTypeContactList

    var _chosenContact = MutableLiveData<Contact>()
    val chosenContact get() = _chosenContact

    val contactsTypeHashmap = HashMap<ContactsList, List<Contact>>().apply {
        this[ContactsList.REQUESTED_CONTACT] = currentUser.value!!.requestedContacts
        this[ContactsList.ACCEPTED_CONTACT] = currentUser.value!!.acceptedContacts
        this[ContactsList.REJECTED_CONTACT] = currentUser.value!!.rejectedContacts
        this[ContactsList.DELETED_CONTACT] = currentUser.value!!.deletedContacts
    }


    fun onContactClicked(contact: Contact) {
        _chosenContact.value = contact
    }

    fun finishedContact() {
        _chosenContact.value = null
    }

    // after the user accepted or rejected the invite,
    // the contact show not be shown anymore.
    // we already deleted it from the invites list in the database,
    // now, we need to remove it here
    fun removeContact(contact: Contact) {
        if (!invites.value.isNullOrEmpty()) {
            val tempList = invites.value!!.toMutableList()
            //if (contact in tempList) {

            if (tempList.remove(contact)) {
                Log.i("remove contact", "found the contact")
            } else {
                Log.i("remove contact", "can't find the contact.")
            }
            //}
            _invites.value = tempList
        }
    }
}