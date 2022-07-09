package com.bitpunchlab.android.foryoumessages.models

class User {
    // the id will the the id from Firebase Auth
    var userID : String = ""
    var userName : String = ""
    var userEmail : String = ""
    var userPhone : String = ""
    var invites = HashMap<String, String>()

    constructor()

    constructor(id: String, name: String, email: String, phone: String) : this() {
        userID = id
        userName = name
        userEmail = email
        userPhone = phone
    }
}