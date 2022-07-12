package com.bitpunchlab.android.foryoumessages.contacts

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bitpunchlab.android.foryoumessages.models.User

class ContactsViewModel : ViewModel() {

    var _contacts = MutableLiveData<List<User>>()
    val contacts get() = _contacts

    var _chosenContact = MutableLiveData<User>()
    val chosenContact get() = _chosenContact


    fun onContactClicked(contact: User) {
        _chosenContact.value = contact
    }

    fun finishedContact() {
        _chosenContact.value = null
    }
}