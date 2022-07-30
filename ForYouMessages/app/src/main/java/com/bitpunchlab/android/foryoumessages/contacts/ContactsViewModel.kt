package com.bitpunchlab.android.foryoumessages.contacts

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bitpunchlab.android.foryoumessages.ContactsList
import com.bitpunchlab.android.foryoumessages.database.ForYouDatabase
import com.bitpunchlab.android.foryoumessages.firebaseClient.FirebaseClientViewModel
import com.bitpunchlab.android.foryoumessages.models.*

class ContactsViewModel(private val localDatabase: ForYouDatabase, private val userID: String) : ViewModel() {

    var _contacts = MutableLiveData<List<Contact>>()
    val contacts get() = _contacts

    var _invites = MutableLiveData<List<Contact>>()
    val invites get() = _invites

    //var user = localDatabase.userDAO.getUser(userID) //MutableLiveData<UserEntity>()
    lateinit var user : LiveData<UserWithContactListsAndContacts>

    var _currentTypeContactList = MutableLiveData<List<Contact>>()
    val currentTypeContactList get() = _currentTypeContactList

    var _chosenContact = MutableLiveData<ContactEntity>()
    val chosenContact get() = _chosenContact


    fun onContactClicked(contact: ContactEntity) {
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

    fun getUserLocalDatabase(id: String) {
        user = localDatabase.userDAO.getUserWithContactListsAndContacts(id)
    }
}

class ContactsViewModelFactory(private val localDatabase: ForYouDatabase,
    private val userID: String)
    : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactsViewModel::class.java)) {
            return ContactsViewModel(localDatabase, userID) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}