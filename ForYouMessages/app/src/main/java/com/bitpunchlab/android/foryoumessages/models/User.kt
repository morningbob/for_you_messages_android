package com.bitpunchlab.android.foryoumessages.models

class User {
    // the id will the the id from Firebase Auth
    var userID : String = ""
    var userName : String = ""
    var userEmail : String = ""
    var userPhone : String = ""
    var contacts = emptyList<Contact>()
    // we keep the record of the requests user issued
    var requestedContacts = HashMap<String, String>()
    // the list of users who accepted the user's request
    var acceptedContacts = HashMap<String, String>()
    // the list of invites the user got
    var invites = HashMap<String, String>()
    var rejectedContacts = HashMap<String, String>()

    constructor()

    constructor(id: String, name: String, email: String, phone: String) : this() {
        userID = id
        userName = name
        userEmail = email
        userPhone = phone
    }
}