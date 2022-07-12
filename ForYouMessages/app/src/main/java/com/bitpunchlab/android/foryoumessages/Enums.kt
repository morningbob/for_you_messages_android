package com.bitpunchlab.android.foryoumessages

enum class CreateAccountAppState {
    NORMAL,
    READY_REGISTER,
    AUTH_REGISTRATION_SUCCESS,
    REGISTER_ERROR,
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

