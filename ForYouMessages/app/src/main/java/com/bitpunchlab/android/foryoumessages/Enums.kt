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

