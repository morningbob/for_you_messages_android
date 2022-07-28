package com.bitpunchlab.android.foryoumessages.models

import androidx.room.Entity
import androidx.room.PrimaryKey

// we store contacts inside user.  but we don't need to query contacts as
// in the database.  so I decided to just serialize contact object in contacts field
// using type converter.  it is because the contact object is relatively simple.
// it is more efficient to store the whole object in one of user object's field.
// at the end of the day, we just need the various contact lists like requested contacts list
// to display to the user.
@Entity(tableName = "user_table")

data class UserEntity (
    @PrimaryKey val userID: String = "",
    var userName: String = "",
    var userPhone: String = "",
    var userEmail: String = "",
    var contacts : List<Contact> = emptyList(),
    var requestedContacts : List<Contact> = emptyList(),
    var acceptedContacts: List<Contact> = emptyList(),
    var invites : List<Contact> = emptyList(),
    var rejectedContacts : List<Contact> = emptyList(),
    var deletedContacts : List<Contact> = emptyList()

    )