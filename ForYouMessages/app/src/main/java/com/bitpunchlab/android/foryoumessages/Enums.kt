package com.bitpunchlab.android.foryoumessages

enum class CreateAccountAppState {
    NORMAL,
    READY_REGISTER,
    REGISTER_SUCCESS,
    REGISTER_ERROR,
    CREATED_AND_SAVED_USER,
    RESET,

}

enum class LoginAppState {
    NORMAL,
    LOGGED_IN,
    LOGIN_ERROR,
    LOGGED_OUT,
    RESET
}

