package com.bitpunchlab.android.foryoumessages.firebaseClient

import android.app.Activity
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.*
import com.bitpunchlab.android.foryoumessages.CreateAccountAppState
import com.bitpunchlab.android.foryoumessages.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.regex.Pattern

class FirebaseClientViewModel(val activity: Activity) : ViewModel() {

    var _currentUser = MutableLiveData<User>()
    val currentUser get() = _currentUser

    // these variables relates to login and create account interface's edittext fields
    // and the errors associated with them.

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

    var _nameError = MutableLiveData<String>()
    val nameError get() = _nameError

    var _emailError = MutableLiveData<String>()
    val emailError get() = _emailError

    var _phoneError = MutableLiveData<String>()
    val phoneError get() = _phoneError

    var _passwordError = MutableLiveData<String>()
    val passwordError get() = _passwordError

    var _confirmPasswordError = MutableLiveData<String>()
    val confirmPasswordError get() = _confirmPasswordError

    var _allValid = MutableLiveData<ArrayList<Int>>()
    val allValid get() = _allValid

    var auth : FirebaseAuth = FirebaseAuth.getInstance()

    var _registerSuccess = MutableLiveData<Boolean>()
    val registerSuccess get() = _registerSuccess

    var _createAccountAppState = MutableLiveData<CreateAccountAppState>(CreateAccountAppState.NORMAL)
    val createAccountAppState get() = _createAccountAppState

    private var database : DatabaseReference = Firebase.database.reference

    init {
        _allValid.value = arrayListOf(0,0,0,0,0)

        // we create a user object and save in firebase database
        registerSuccess.observe(activity.applicationContext as LifecycleOwner, Observer { success ->
            if (success) {

            }
        })

        createAccountAppState.observe(activity.applicationContext as LifecycleOwner, Observer { appState ->
            when (appState) {
                CreateAccountAppState.REGISTER_SUCCESS -> {
                    // now, we can save the user in database
                    createAndSaveNewUser(userName.value!!, userEmail.value!!, userPhone.value!!)
                }
                else -> {

                }
            }
        })
    }

    // whenever user is filling in one field, that field checks for its validity.
    // only if it is valid will the ready to create live data check if all fields are valid

    private val nameValid: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        addSource(userName) { name ->
            if (name.isNullOrEmpty()) {
                nameError.value = "Name must not be empty."
                value = false
            } else {
                value = true
                nameError.value = ""
            }
        }
    }

    val emailValid: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        addSource(userEmail) { email ->
            if (!email.isNullOrEmpty()) {
                if (!isEmailValid(email)) {
                    emailError.value = "Please enter a valid email."
                    value = false
                } else {
                    value = true
                    emailError.value = ""
                }
            } else {
                value = false
            }
            Log.i("email valid? ", value.toString())
        }
    }

    private val phoneValid: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        addSource(userPhone) { phone ->
            if (phone.isNullOrEmpty()) {
                phoneError.value = "Phone number must not be empty."
                value = false
            } else {
                value = true
                phoneError.value = ""
            }
        }
    }

    private val passwordValid: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        addSource(userPassword) { password ->
            if (!password.isNullOrEmpty()) {
                if (isPasswordContainSpace(password)) {
                    passwordError.value = "Password cannot has space."
                    value = false
                } else if (password.count() < 8) {
                    passwordError.value = "Password should be at least 8 characters."
                    value = false
                } else if (!isPasswordValid(password)) {
                    passwordError.value = "Password can only be composed of letters and numbers."
                    value = false
                } else {
                    passwordError.value = ""
                    value = true
                }
            } else {
                value = false
            }
            Log.i("password valid? ", value.toString())
        }
    }

    // we'll set allValid a 1 entry if the field is valid
    // we also check if other fields are also valid by checking if the allValid arraylist sum to 5
    // if this is the case, we set this readyRegisterLiveData to true, that is ready to register the user
    // this approach minimize the times the program test all 5 fields
    var readyRegisterLiveData: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        addSource(nameValid) { valid ->
            if (valid) {
                allValid.value?.set(0, 1)
                // check other fields validity
                value = checkIfAllValid()
            } else {
                allValid.value?.set(0, 0)
            }
        }
        addSource(emailValid) { valid ->
            if (valid) {
                allValid.value?.set(1, 1)
                // check other fields validity
                value = checkIfAllValid()
            } else {
                allValid.value?.set(1, 0)
            }
        }
        addSource(phoneValid) { valid ->
            if (valid) {
                allValid.value?.set(2, 1)
                // check other fields validity
                value = checkIfAllValid()
            } else {
                allValid.value?.set(2, 0)
            }
        }
        addSource(passwordValid) { valid ->
            if (valid) {
                allValid.value?.set(3, 1)
                // check other fields validity
                value = checkIfAllValid()
            } else {
                allValid.value?.set(3, 0)
            }
        }
        addSource(confirmPasswordValid) { valid ->
            if (valid) {
                allValid.value?.set(4, 1)
                // check other fields validity
                value = checkIfAllValid()
            } else {
                allValid.value?.set(4, 0)
            }
        }
    }

    private val confirmPasswordValid: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        addSource(userConfirmPassword) { confirmPassword ->
            if (!confirmPassword.isNullOrEmpty()) {
                if (!isConfirmPasswordValid(userPassword.value!!, confirmPassword)) {
                    confirmPasswordError.value = "Passwords must be the same."
                    value = false
                } else {
                    confirmPasswordError.value = ""
                    value = true
                }
            } else {
                value = false
            }
            Log.i("confirm valid? ", value.toString())
        }
    }

    private fun isEmailValid(email: String) : Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isPasswordValid(password: String) : Boolean {
        val passwordPattern = Pattern.compile("^[A-Za-z0-9]{8,20}$")
        return passwordPattern.matcher(password).matches()
    }

    private fun isPasswordContainSpace(password: String) : Boolean {
        return password.contains(" ")
    }

    private fun isConfirmPasswordValid(password: String, confirmPassword: String) : Boolean {
        //Log.i("confirming password: ", "password: $password, confirm: $confirmPassword")
        return password == confirmPassword
    }

    private fun checkIfAllValid() : Boolean {
        return allValid.value?.sum() == 5
    }

    private var authStateListener = FirebaseAuth.AuthStateListener { auth ->

    }

    fun registerNewUser() {
        // we already tested user email and password are valid
        auth.createUserWithEmailAndPassword(userEmail.value!!, userPassword.value!!)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    // alert success
                    Log.i("register user", "success")
                    //registerSuccess.postValue(true)
                    createAccountAppState.postValue(CreateAccountAppState.REGISTER_SUCCESS)
                } else {
                    Log.i("register user", "there is error")
                    //registerSuccess.postValue(false)
                }
            }
    }

    fun resetAllFields() {
        userName.value = ""
        userEmail.value = ""
        userPhone.value = ""
        userPassword.value = ""
        userConfirmPassword.value = ""
    }

    private fun createAndSaveNewUser(name: String, email: String, phone: String) {

        saveUserInDatabase(createUser(name, email, phone))
        //database.child("users")
    }

    private fun createUser(name: String, email: String, phone: String) : User {
        return User(id = auth.uid!!, name = name, email = email, phone = phone)
    }

    private fun saveUserInDatabase(newUser: User) {
        database.child("users").child(newUser.userID).setValue(newUser)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {

                }
            }
    }

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