package com.bitpunchlab.android.foryoumessages.contacts

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bitpunchlab.android.foryoumessages.models.Contact
import com.bitpunchlab.android.foryoumessages.models.User

class ContactsViewModel : ViewModel() {

    var _contacts = MutableLiveData<List<Contact>>()
    val contacts get() = _contacts

    var _invites = MutableLiveData<List<Contact>>()
    val invites get() = _invites

    var _requestedList = MutableLiveData<List<Contact>>()
    val requestedList get() = _requestedList

    var _acceptedList = MutableLiveData<List<Contact>>()
    val acceptedList get() = _acceptedList

    var _chosenContact = MutableLiveData<Contact>()
    val chosenContact get() = _chosenContact


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