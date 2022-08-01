package com.bitpunchlab.android.foryoumessages

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

enum class CreateAccountAppState {
    NORMAL,
    READY_REGISTER,
    AUTH_REGISTRATION_SUCCESS,
    SAME_EMAIL_ERROR,
    SAME_PHONE_ERROR,
    REGISTRATION_ERROR,
    REGISTRATION_SUCCESS,
    GET_USER_OBJECT_FAILURE,
    RESET,
    LOGGED_IN
}

enum class LoginAppState {
    NORMAL,
    LOGGED_IN,
    LOGIN_ERROR,
    LOGGED_OUT,
    RESET
}

enum class RequestContactAppState {
    NORMAL,
    CONTACT_NOT_FOUND,
    PHONE_NOT_FOUND,
    ASK_CONFIRMATION,
    CONFIRMED_REQUEST,
    SERVER_NOT_AVAILABLE,
    REQUEST_SENT,
    ACCEPTED_CONTACT,
    REJECTED_CONTACT,
}

enum class AcceptContactAppState {
    CONTACT_NOT_FOUND,
    ASK_CONFIRMATION,
    CONFIRMED_ACCEPTANCE,
    SERVER_NOT_AVAILABLE,
    ACCEPTED_CONTACT,
}

enum class RejectContactAppState {
    CONTACT_NOT_FOUND,
    ASK_CONFIRMATION,
    CONFIRMED_REJECTION,
    SERVER_NOT_AVAILABLE,
    REJECTED_CONTACT,
}

enum class DeleteContactAppState {
    CONTACT_NOT_FOUND,
    ASK_CONFIRMATION,
    CONFIRMED_DELETION,
    SERVER_NOT_AVAILABLE,
    DELETED_CONTACT,
}

@Parcelize
enum class ContactsList : Parcelable{
    REQUESTED_CONTACT,
    ACCEPTED_CONTACT,
    REJECTED_CONTACT,
    DELETED_CONTACT,
    INVITES,
    USER_CONTACTS
}



