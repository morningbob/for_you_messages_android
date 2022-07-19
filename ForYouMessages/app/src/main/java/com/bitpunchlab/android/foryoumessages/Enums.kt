package com.bitpunchlab.android.foryoumessages

enum class CreateAccountAppState {
    NORMAL,
    READY_REGISTER,
    AUTH_REGISTRATION_SUCCESS,
    SAME_EMAIL_ERROR,
    SAME_PHONE_ERROR,
    REGISTRATION_ERROR,
    REGISTRATION_SUCCESS,
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
    REQUEST,
    CONTACT_NOT_FOUND,
    ASK_CONFIRMATION,
    CONFIRMED_REQUEST,
    SERVER_NOT_AVAILABLE,
    REQUEST_SENT,
    ACCEPTED_CONTACT,
    REJECTED_CONTACT,
}

