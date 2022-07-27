package com.bitpunchlab.android.foryoumessages


val ContactsTypeTitleMap = HashMap<ContactsList, String>().apply {
    this[ContactsList.REQUESTED_CONTACT] = "Requests made"
    this[ContactsList.ACCEPTED_CONTACT] = "Users accepted your requests"
    this[ContactsList.REJECTED_CONTACT] = "Users rejected your requests"
    this[ContactsList.DELETED_CONTACT] = "Users deleted your contact"
}