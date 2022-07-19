package com.bitpunchlab.android.foryoumessages.models

class User {
    // the id will the the id from Firebase Auth
    var userID : String = ""
    var userName : String = ""
    var userEmail : String = ""
    var userPhone : String = ""
    var contacts = HashMap<String, Contact>()
    // we keep the record of the requests user issued
    val requestedContacts = HashMap<String, String>()
    // the list of users who accepted the user's request
    val acceptedContacts = HashMap<String, String>()
    // the list of invites the user got
    val invites = HashMap<String, String>()
    val rejectedContacts = HashMap<String, String>()
    val deletedContacts = HashMap<String, String>()

    constructor()

    constructor(id: String, name: String, email: String, phone: String) : this() {
        userID = id
        userName = name
        userEmail = email
        userPhone = phone
    }
}