package com.bitpunchlab.android.foryoumessages.firebaseClient

import android.app.Activity
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.bitpunchlab.android.foryoumessages.ContactsList
import com.bitpunchlab.android.foryoumessages.CreateAccountAppState
import com.bitpunchlab.android.foryoumessages.LoginAppState
import com.bitpunchlab.android.foryoumessages.RequestContactAppState
import com.bitpunchlab.android.foryoumessages.models.Contact
import com.bitpunchlab.android.foryoumessages.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import java.util.*

import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class FirebaseClientViewModel(val activity: Activity) : ViewModel() {

    var _currentUserObject = MutableLiveData<User>()
    val currentUserObject get() = _currentUserObject

    var _currentUserContact = MutableLiveData<Contact>()
    val currentUserContact get() = _currentUserContact



    // these variables relates to login and create account interface's edittext fields
    // and the errors associated with them.

    var _userName = MutableLiveData<String>("")
    val userName get() = _userName

    var _userEmail = MutableLiveData<String>("")
    val userEmail get() = _userEmail

    var _userPhone = MutableLiveData<String>("")
    val userPhone get() = _userPhone

    var _userPassword = MutableLiveData<String>("")
    val userPassword get() = _userPassword

    var _userConfirmPassword = MutableLiveData<String>("")
    val userConfirmPassword get() = _userConfirmPassword

    var _nameError = MutableLiveData<String>("")
    val nameError get() = _nameError

    var _emailError = MutableLiveData<String>("")
    val emailError get() = _emailError

    var _phoneError = MutableLiveData<String>("")
    val phoneError get() = _phoneError

    var _passwordError = MutableLiveData<String>("")
    val passwordError get() = _passwordError

    var _confirmPasswordError = MutableLiveData<String>("")
    val confirmPasswordError get() = _confirmPasswordError

    var _allValid = MutableLiveData<ArrayList<Int>>()
    val allValid get() = _allValid

    var auth : FirebaseAuth = FirebaseAuth.getInstance()

    var _createAccountAppState = MutableLiveData<CreateAccountAppState>(CreateAccountAppState.NORMAL)
    val createAccountAppState get() = _createAccountAppState

    var _loginAppState = MutableLiveData<LoginAppState>(LoginAppState.NORMAL)
    val loginAppState get() = _loginAppState

    var _requestContactAppState = MutableLiveData<RequestContactAppState>()
    val requestContactAppState get() = _requestContactAppState

    private val database = Firebase.firestore

    var foundPhone = MutableLiveData<Int>(0)
    var foundEmail = MutableLiveData<Int>(0)

    var _loggedIn = MutableLiveData<Boolean>(false)
    val loggedIn get() = _loggedIn

    var coroutineScope = CoroutineScope(Dispatchers.IO)

    var appInviteeContact : Contact? = null
    var appInviterContac : Contact? = null

    var _searchPhoneResult = MutableLiveData<Int>(0)
    val searchPhoneResult get() = _searchPhoneResult

    var _searchEmailResult = MutableLiveData<Int>(0)
    val searchEmailResult get() = _searchEmailResult

    var _userContacts = MutableLiveData<List<Contact>>()
    val userContacts get() = _userContacts

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

    // should be at least 10 number and max 15, I think
    private val phoneValid: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        addSource(userPhone) { phone ->
            if (phone.isNullOrEmpty()) {
                phoneError.value = "Phone number must not be empty."
                value = false
            } else if (phone.count() < 10 || phone.count() > 13)
                phoneError.value = "Phone number should be at least 10 number and maximum 13 number."
            else if (!isPhoneValid(phone)) {
                phoneError.value = "Phone number is not valid."
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

    var readyRegisterLiveData = MediatorLiveData<Boolean>()
    var readyLoginLiveData = MediatorLiveData<Boolean>()
    var readyRegisterAuth = MediatorLiveData<Boolean>()

    private var authStateListener = FirebaseAuth.AuthStateListener { auth ->
        if (auth.currentUser != null) {
            loginAppState.value = LoginAppState.LOGGED_IN
            // here, whether we're just authenticated, just registered as new user,
            // or just start the app, that we logged in previously.
            // we retrieve the user object and contact object for later uses.
            coroutineScope.launch {
                // we get these objects, later we use them to initiate actions
                // related to contacts
                currentUserObject.postValue(retrieveUserObject(auth.uid!!))
                currentUserContact.postValue(retrieveContactByEmail(auth.currentUser!!.email!!))
            }
        } else {

        }
    }

    init {
        auth.addAuthStateListener(authStateListener)
        _allValid.value = arrayListOf(0,0,0,0,0)


        // we'll set allValid a 1 entry if the field is valid
        // we also check if other fields are also valid by checking if the allValid arraylist sum to 5
        // if this is the case, we set this readyRegisterLiveData to true, that is ready to register the user
        // this approach minimize the times the program test all 5 fields
        readyRegisterLiveData.addSource(nameValid) { valid ->
            readyRegisterLiveData.value = if (valid) {
                allValid.value?.set(0, 1)
                // check other fields validity
                checkIfAllValid()
            } else {
                allValid.value?.set(0, 0)
                false
            }
        }
        readyRegisterLiveData.addSource(emailValid) { valid ->
            if (valid) {
                allValid.value?.set(1, 1)
                // check other fields validity
                readyRegisterLiveData.value = checkIfAllValid()
            } else {
                allValid.value?.set(1, 0)
                readyRegisterLiveData.value = false
            }
        }
        readyRegisterLiveData.addSource(phoneValid) { valid ->
            if (valid) {
                allValid.value?.set(2, 1)
                // check other fields validity
                readyRegisterLiveData.value = checkIfAllValid()
            } else {
                allValid.value?.set(2, 0)
                readyRegisterLiveData.value = false
            }
        }
        readyRegisterLiveData.addSource(passwordValid) { valid ->
            if (valid) {
                allValid.value?.set(3, 1)
                // check other fields validity
                readyRegisterLiveData.value = checkIfAllValid()
            } else {
                allValid.value?.set(3, 0)
                readyRegisterLiveData.value = false
            }
        }
        readyRegisterLiveData.addSource(confirmPasswordValid) { valid ->
            if (valid) {
                allValid.value?.set(4, 1)
                // check other fields validity
                readyRegisterLiveData.value = checkIfAllValid()
            } else {
                allValid.value?.set(4, 0)
                readyRegisterLiveData.value = false
            }
        }

        readyLoginLiveData.addSource(emailValid) { valid ->
            if (valid) {
                // check other fields validity
                readyLoginLiveData.value = passwordValid.value
            } else {
                readyLoginLiveData.value = false
            }
        }

        readyLoginLiveData.addSource(passwordValid) { valid ->
            if (valid) {
                //allValid.value?.set(1, 1)
                // check other fields validity
                readyLoginLiveData.value = emailValid.value
            } else {
                //allValid.value?.set(1, 0)
                readyLoginLiveData.value = false
            }
        }
        /*
        // these mediator live data are for registration, but I put them here
        // since I don't want to observe again every time user click send button
        readyRegisterAuth.addSource(foundPhone) { found ->
            readyRegisterAuth.value = found == 1 && foundEmail.value!! == 1
            Log.i("ready register auth", "true from phone")
            Log.i("found phone ${foundPhone.value.toString()}", "found email ${foundEmail.value.toString()}")
        }
        readyRegisterAuth.addSource(foundEmail) { found ->
            readyRegisterAuth.value = found == 1 && foundPhone.value!! == 1
            Log.i("ready register auth", "true from email")
            Log.i("found phone ${foundPhone.value.toString()}", "found email ${foundEmail.value.toString()}")
        }


        // here we start to observe foundPhone
        foundPhone.observe(activity as LifecycleOwner, Observer { found ->
            when (found) {
                1 -> Log.i("phone", "not exist")
                2 -> Log.i("phone", "already exists")
                3 -> Log.i("phone", "there is error searching")
                else -> Log.i("phone", "waiting for result")
            }

        })
        foundEmail.observe(activity as LifecycleOwner, Observer { found ->
            when (found) {
                1 -> Log.i("email", "not exist")
                2 -> Log.i("email", "already exists")
                3 -> Log.i("email", "there is error searching")
                else -> Log.i("email", "waiting for result")
            }

        })

        readyRegisterAuth.observe(activity as LifecycleOwner, Observer { value ->
            value?.let {
                if (value) {
                    Log.i("ready proceed?", "proceed")
                    //registerUser()
                }
            }
        })

        createAccountAppState.observe(activity as LifecycleOwner, Observer { appState ->
            when (appState) {
                CreateAccountAppState.REGISTRATION_SUCCESS -> {
                    coroutineScope.launch {
                        // we get these objects, later we use them to initiate actions
                        // related to contacts
                        currentUserObject.postValue(retrieveUserObject(auth.uid!!))
                        currentUserContact.postValue(retrieveContactByEmail(auth.currentUser!!.email!!))
                    }
                }
                else -> 0
            }
        })

         */

        requestContactAppState.observe(activity as LifecycleOwner, Observer { appState ->
            when (appState) {
                RequestContactAppState.CONFIRMED_REQUEST -> {
                    coroutineScope.launch {
                        if (triggerCloudFunction(
                            //inviterEmail = auth.currentUser!!.email!!,
                            //inviteeEmail = inviteeContact!!.contactEmail,
                            inviterContact = currentUserContact.value!!,
                            inviteeContact = appInviteeContact!!,
                            requestName = "requestContact"
                        )) {
                            requestContactAppState.postValue(RequestContactAppState.REQUEST_SENT)
                        } else {
                            requestContactAppState.postValue(RequestContactAppState.SERVER_NOT_AVAILABLE)
                        }
                    }
                }
                else -> 0
            }
        })
    }



    private fun isEmailValid(email: String) : Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isPhoneValid(phone: String) : Boolean {
        return PhoneNumberUtils.isGlobalPhoneNumber(phone)
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

    // in this method, we first check if the email or the phone already exist
    // before we register the user
    fun prepareToRegisterNewUser() {
        // we reset them 0 here, if it is found, then value is 2
        // not found, value is 1, if error, value is 3
        // when both values are 1, proceed to register

        Log.i("prepare", "start searching")

        val shouldRegister = MediatorLiveData<Boolean>()
        shouldRegister.addSource(searchPhoneResult) { result ->
            shouldRegister.value = result == 1 && searchEmailResult.value!! == 1
        }
        shouldRegister.addSource(searchEmailResult) { result ->
            shouldRegister.value = result == 1 && searchPhoneResult.value!! == 1
        }
        shouldRegister.observe(activity as LifecycleOwner, Observer { readyRegister ->
            if (readyRegister) {
                registerUser()
            }
        })

        coroutineScope.launch {
            searchPhoneResult.postValue(searchPhone(userPhone.value!!))
            searchEmailResult.postValue(searchEmail(userEmail.value!!))
        }

    }

    private fun registerUser() {
        // remove shouldRegisterObsever
        // we already tested user email and password are valid
        auth.createUserWithEmailAndPassword(userEmail.value!!, userPassword.value!!)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    // alert success
                    Log.i("register user", "success")
                    createAccountAppState.postValue(CreateAccountAppState.AUTH_REGISTRATION_SUCCESS)
                } else {
                    Log.i("register user", "there is error")
                    //Log.i("error", task.result.toString())
                    // either server down, or email already exists
                    createAccountAppState.postValue(CreateAccountAppState.REGISTRATION_ERROR)
                }
            }
    }

    fun resetAllFields() {
        userName.value = ""
        userEmail.value = ""
        userPhone.value = ""
        userPassword.value = ""
        userConfirmPassword.value = ""
        nameError.value = ""
        emailError.value = ""
        phoneError.value = ""
        passwordError.value = ""
        confirmPasswordError.value = ""
        currentUserContact.value = null
        currentUserObject.value = null
    }

    fun createAndSaveNewUser() {
        saveUserInDatabase(createUser(userName.value!!, userEmail.value!!, userPhone.value!!))
    }

    private fun createUser(name: String, email: String, phone: String) : User {
        return User(id = auth.uid!!, name = name, email = email, phone = phone)
    }

    private fun saveUserInDatabase(newUser: User) {
        database.collection("users")
            .document(newUser.userID)
            .set(newUser, SetOptions.merge())
            .addOnSuccessListener { docRef ->
                Log.i("save user in database", "success")
                // now we can save the user's phone number in phone list
                saveContactInDatabase(newUser)
            }
            .addOnFailureListener { e ->
                Log.i("error adding user", e.message.toString())
                createAccountAppState.postValue(CreateAccountAppState.REGISTRATION_ERROR)
            }
    }

    private fun saveContactInDatabase(user: User) {
        val contactData = hashMapOf<String, String>(
            "contactName" to user.userName,
            "contactPhone" to user.userPhone,
            "contactEmail" to user.userEmail
        )

        database.collection("contacts")
            .document(user.userEmail)
            .set(contactData)
            .addOnSuccessListener { docRef ->
                Log.i("save contact in database", "success")
                createAccountAppState.postValue(CreateAccountAppState.REGISTRATION_SUCCESS)
            }
            .addOnFailureListener { e ->
                Log.i("error adding contact", e.message.toString())
                createAccountAppState.postValue(CreateAccountAppState.REGISTRATION_ERROR)
            }
    }

    fun authenticateUser() {
        auth.signInWithEmailAndPassword(userEmail.value!!, userPassword.value!!)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    Log.i("signIn user", "success")

                } else {
                    Log.i("signIn user", "error")
                    loginAppState.postValue(LoginAppState.LOGIN_ERROR)
                }
            }
    }

    private suspend fun retrieveUserObject(userID: String) : User =
        suspendCancellableCoroutine<User> {  cancellableContinuation ->
            Log.i("retrieve user", "start running coroutine")
            val userRef = database.collection("users").document(userID)
            userRef.get()
                .addOnSuccessListener { document ->
                    if (!document.exists()) {
                        Log.i("retrieve user", "can't find the user")
                        cancellableContinuation.resume(User()) {}
                        // post an error state here
                        //requestContactAppState.postValue(RequestContactAppState.CONTACT_NOT_FOUND)
                        createAccountAppState.postValue(CreateAccountAppState.GET_USER_OBJECT_FAILURE)
                    } else {
                        Log.i("retrieve user", "found the user")
                        //document.map { doc ->
                            cancellableContinuation.resume(document.toObject(User::class.java)!!) {}
                        //}
                    }
                }
                .addOnFailureListener {
                    Log.i("retrieve user", "error")
                    cancellableContinuation.resume(User()) {}
                    //requestContactAppState.postValue(RequestContactAppState.SERVER_NOT_AVAILABLE)
                    createAccountAppState.postValue(CreateAccountAppState.GET_USER_OBJECT_FAILURE)
                }
    }

    fun logoutUser() {
        auth.signOut()
        loginAppState.value = LoginAppState.LOGGED_OUT
    }

    // everytime we sign in the app, we retrieved user objects, which has the updated contacts

    // before we can issue a request, we need to identify the target user
    // we use phone number as the unique identifier.
    // so, the user can input the phone number of the target user to search
    // if he has an account in the database
    // we'll be searching for the phone number list in the database
    // if yes, we retrieve the target user object in the server only,
    // and add the phone number in the invites list.

    // issue request, need to be accepted by the party
    // here we put the user's contact id into the target's invites list
    // when the target is online, the app will check his invites list and notify him

    //@OptIn(InternalCoroutinesApi::class)
    private suspend fun searchPhone(phone: String) : Int =
        suspendCancellableCoroutine<Int> { cancellableContinuation ->
            foundPhone.postValue(0)
            val contactsRef = database.collection("contacts")
            contactsRef
                .whereEqualTo("contactPhone", phone)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        Log.i("search phone", "success but no document found")
                        //foundPhone.postValue(1)
                        cancellableContinuation.resume(1) {}
                    }
                    for (document in documents) {
                        // so when there is a document, that means there is a phone number matched
                        // the loop should run only once
                        //foundPhone.postValue(2)
                        Log.i("search phone", "exists")
                        createAccountAppState.postValue(CreateAccountAppState.SAME_PHONE_ERROR)
                        cancellableContinuation.resume(2) {}
                    }

                }
                .addOnFailureListener { e ->
                    Log.i("error searching phone", e.message.toString())
                    //foundPhone.postValue(3)
                    cancellableContinuation.resume(3) {}
                }
        }

    private suspend fun searchEmail(email: String) : Int =
        suspendCancellableCoroutine<Int> { cancellableContinuation ->
            foundEmail.postValue(0)
            val contactsRef = database.collection("contacts")
            contactsRef
                .whereEqualTo("contactEmail", email)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        Log.i("search email", "success but no document found")
                        //foundEmail.postValue(1)
                        cancellableContinuation.resume(1) {}
                    }
                    for (document in documents) {
                        // so when there is a document, that means there is a phone number matched
                        // the loop should run only once
                        //foundEmail.postValue(2)
                        Log.i("search email", "exists")
                        createAccountAppState.postValue(CreateAccountAppState.SAME_EMAIL_ERROR)
                        cancellableContinuation.resume(2) {}
                    }
                }
                .addOnFailureListener { e ->
                    Log.i("error searching email", e.message.toString())
                    //foundEmail.postValue(3)
                    cancellableContinuation.resume(3) {}
                }
        }



    fun clearSameEmail() {
        userEmail.value = ""
    }

    fun clearSamePhone() {
        userPhone.value = ""
    }

    fun requestContact(phone: String) {
        coroutineScope.launch {
            val result = searchPhone(phone)
            if (result == 2 && currentUserContact.value != null) {
                Log.i("inside coroutine, test search phone exists or not", "exist")
                appInviteeContact = retrieveContactByPhone(phone)
                //appInviterContact = retrieveContactByEmail(auth!!.currentUser!!.email!!)
                //appInviterContact = currentUserContact.value
                Log.i("after retrieved contact", "contact name: ${appInviteeContact!!.contactName}")
                if (appInviteeContact != null && !appInviteeContact!!.contactName.isNullOrEmpty()
                    && currentUserContact.value != null && !currentUserContact.value!!.contactName.isNullOrEmpty()) {
                    Log.i("invitee contact", appInviteeContact.toString())
                    Log.i("invitee contact", "name: ${appInviteeContact!!.contactName}")
                    Log.i("test search phone exists", "got back contact")
                    // can show an alert to user to confirm the request, with the name of
                    // the contact
                    requestContactAppState.postValue(RequestContactAppState.ASK_CONFIRMATION)
                } else  {
                    //requestContactAppState.postValue(RequestContactAppState.CONTACT_NOT_FOUND)
                }
            }
        }

    }

    // there should be a list of accepted request
    // in the list, we add the contact to contact list and save to database
    // notify user the request was accepted
    fun addContact() {

    }

    // add to the contact list of the user
    // here we need to update the original user's accepted request
    // and notify the original user
    // we also add the user's contact into the original user's contact list


    // here we need to update the original user's requestedContacts list, delete it
    // and notify the original user
    private suspend fun retrieveContactByPhone(phone: String) : Contact =
        suspendCancellableCoroutine<Contact> {  cancellableContinuation ->
            Log.i("retrieve contact", "start running coroutine")
            val contactsRef = database.collection("contacts")

            contactsRef
                .whereEqualTo("contactPhone", phone)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        Log.i("retrieve contact", "can't find the contact")
                        cancellableContinuation.resume(Contact()) {}
                        requestContactAppState.postValue(RequestContactAppState.CONTACT_NOT_FOUND)
                    } else {
                        Log.i("retrieve contact", "found the contact")
                        documents.map { doc ->
                            cancellableContinuation.resume(doc.toObject(Contact::class.java)) {}
                        }
                    }
                }
                .addOnFailureListener {
                    Log.i("retrieve contact", "error")
                    cancellableContinuation.resume(Contact()) {}
                    requestContactAppState.postValue(RequestContactAppState.SERVER_NOT_AVAILABLE)
                }
            //Log.i("retrieve contact", "about to stop running coroutine")
    }

    private suspend fun retrieveContactByEmail(email: String) : Contact =
        suspendCancellableCoroutine<Contact> {  cancellableContinuation ->
            Log.i("retrieve contact", "start running coroutine")
            val contactsRef = database.collection("contacts")

            contactsRef
                .whereEqualTo("contactEmail", email)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        Log.i("retrieve contact", "can't find the contact")
                        cancellableContinuation.resume(Contact()) {}
                        requestContactAppState.postValue(RequestContactAppState.CONTACT_NOT_FOUND)
                    } else {
                        Log.i("retrieve contact", "found the contact")
                        documents.map { doc ->
                            cancellableContinuation.resume(doc.toObject(Contact::class.java)) {}
                        }
                    }
                }
                .addOnFailureListener {
                    Log.i("retrieve contact", "error")
                    cancellableContinuation.resume(Contact()) {}
                    requestContactAppState.postValue(RequestContactAppState.SERVER_NOT_AVAILABLE)
                }
            //Log.i("retrieve contact", "about to stop running coroutine")
        }

    private suspend fun triggerCloudFunction(inviterContact: Contact, inviteeContact: Contact,
        requestName: String) : Boolean =
        suspendCancellableCoroutine<Boolean> { cancellableContinuation ->
            Log.i("trigger request", "triggered")
            val docData = hashMapOf<String, Contact>(
                "inviterContact" to inviterContact,
                "inviteeContact" to inviteeContact,

            )
            val requestRef = database.collection(requestName)
            requestRef
                .document(UUID.randomUUID().toString())
                .set(docData)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.i("request contact", "writing doc succeeded")
                        //requestContactAppState.postValue(RequestContactAppState.REQUEST_SENT)
                        cancellableContinuation.resume(true) {}
                    } else {
                        Log.i("request contact", "writing doc failed")
                        //requestContactAppState.postValue(RequestContactAppState.SERVER_NOT_AVAILABLE)
                        cancellableContinuation.resume(false) {}
                    }
                }
        }


    private fun processAcceptInviteDatabase(inviterContact: Contact, inviteeContact: Contact) {
        //suspendCancellableCoroutine<Contact> { cancellableContinuation ->
        coroutineScope.launch {
            if (triggerCloudFunction(
                    inviterContact = inviterContact,
                    inviteeContact = inviteeContact, requestName = "acceptContact"
                )
            ) {
                requestContactAppState.postValue(RequestContactAppState.ACCEPTED_CONTACT)
            } else {
                requestContactAppState.postValue(RequestContactAppState.SERVER_NOT_AVAILABLE)
            }
        }
        //}
    }

    private fun rejectInvite(inviterContact: Contact, inviteeContact: Contact) {
        coroutineScope.launch {
            if (triggerCloudFunction(
                    inviterContact = inviterContact,
                    inviteeContact = inviteeContact, requestName = "rejectContact"
                )
            ) {
                requestContactAppState.postValue(RequestContactAppState.REJECTED_CONTACT)
            } else {
                requestContactAppState.postValue(RequestContactAppState.SERVER_NOT_AVAILABLE)
            }
        }
    }

    // in delete case, inviter is the original user
    // invitee is the target user
    private fun deleteContact(targetContact: Contact, userContact: Contact) {
        coroutineScope.launch {
            if (triggerCloudFunction(
                    inviterContact = userContact,
                    inviteeContact = targetContact,
                    requestName = "rejectContact"
                )
            ) {
                requestContactAppState.postValue(RequestContactAppState.REJECTED_CONTACT)
            } else {
                requestContactAppState.postValue(RequestContactAppState.SERVER_NOT_AVAILABLE)
            }
        }
    }

    fun retrieveContacts(contactKind: ContactsList) {
        var user : User? = null
        //var contacts : List<Contact> = emptyList()
        //var retrievingContacts = HashMap<String, Contact>()

        coroutineScope.launch {
            user = retrieveUser()
            val retrievingContacts = when (contactKind) {
                ContactsList.REQUESTED_CONTACT -> user!!.requestedContacts
                ContactsList.ACCEPTED_CONTACT -> user!!.acceptedContacts
                ContactsList.REJECTED_CONTACT -> user!!.rejectedContacts
                ContactsList.INVITES -> user!!.invites
                ContactsList.USER_CONTACTS -> user!!.contacts
            }
            if (user != null && user!!.userName != "") {
                userContacts.postValue(parseContactsHashmap(retrievingContacts))
            }
        }
    }

    private suspend fun retrieveUser() : User =
        suspendCancellableCoroutine<User> { cancellableContinuation ->
            //var currentUser: User? = null
            database.collection("users")
                .whereEqualTo("userEmail", auth!!.currentUser!!.email)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        Log.i("current user", "error, can't find the user's object")
                        cancellableContinuation.resume(User()) {}
                    } else {
                        documents.map { doc ->
                            //currentUser = doc.toObject(User::class.java)
                            Log.i("current user", "found the user")
                            cancellableContinuation.resume(doc.toObject(User::class.java)) {}
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.i("current user", "there is error")
                    cancellableContinuation.resume(User()) {}
                }
        }


    private fun parseContactsHashmap(contactsMap: HashMap<String, Contact>) : List<Contact> {
        val contacts : ArrayList<Contact> = ArrayList<Contact>()
        for (contact in contactsMap.values) {
            contacts.add(contact)
        }
        return contacts
    }

    fun acceptInvite(chosenContact: Contact) {
        // check both contacts exist before proceeding
        if (currentUserContact.value != null) {
            processAcceptInviteDatabase(inviteeContact = currentUserContact.value!!,
                inviterContact = chosenContact)
        } else {
            Log.i("accept invite", "didn't find the user object.")
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