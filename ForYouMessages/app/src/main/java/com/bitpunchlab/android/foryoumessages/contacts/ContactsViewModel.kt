package com.bitpunchlab.android.foryoumessages.contacts

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bitpunchlab.android.foryoumessages.models.Contact
import com.bitpunchlab.android.foryoumessages.models.User

class ContactsViewModel : ViewModel() {

    var _contacts = MutableLiveData<List<Contact>>()
    val contacts get() = _contacts

    var _invites = MutableLiveData<List<Contact>>()
    val invites get() = _invites

    var _chosenContact = MutableLiveData<Contact>()
    val chosenContact get() = _chosenContact


    fun onContactClicked(contact: Contact) {
        _chosenContact.value = contact
    }

    fun finishedContact() {
        _chosenContact.value = null
    }
}