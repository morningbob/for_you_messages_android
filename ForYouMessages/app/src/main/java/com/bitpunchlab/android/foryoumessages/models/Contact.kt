package com.bitpunchlab.android.foryoumessages.models

class Contact {

    var contactID : String = ""
    var contactName : String = ""
    var contactPhone : String = ""

    constructor()

    constructor(id: String, name: String, phone: String) : this() {
        contactID = id
        contactName = name
        contactPhone = phone
    }
}