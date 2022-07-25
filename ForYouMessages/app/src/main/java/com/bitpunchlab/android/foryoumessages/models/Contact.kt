package com.bitpunchlab.android.foryoumessages.models


class Contact  {

    var contactEmail : String = ""
    var contactName : String = ""
    var contactPhone : String = ""

    constructor() //: this()

    constructor(email: String, name: String, phone: String) : this() {
        //contactID = id
        contactEmail = email
        contactName = name
        contactPhone = phone
    }
}