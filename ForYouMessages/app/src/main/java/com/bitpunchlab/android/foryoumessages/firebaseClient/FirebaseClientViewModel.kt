package com.bitpunchlab.android.foryoumessages.firebaseClient

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bitpunchlab.android.foryoumessages.models.User

class FirebaseClientViewModel(val activity: Activity) : ViewModel() {

    var _currentUser = MutableLiveData<User>()
    val currentUser get() = _currentUser

    var _userName = MutableLiveData<String>()
    val userName get() = _userName

    var _userEmail = MutableLiveData<String>()
    val userEmail get() = _userEmail

    var _userPhone = MutableLiveData<String>()
    val userPhone get() = _userPhone

    var _userPassword = MutableLiveData<String>()
    val userPassword get() = _userPassword

    var _userConfirmPassword = MutableLiveData<String>()
    val userConfirmPassword get() = _userConfirmPassword



}

class FirebaseClientViewModelFactory(private val activity: Activity)
    : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FirebaseClientViewModel::class.java)) {
            return FirebaseClientViewModel(activity) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}