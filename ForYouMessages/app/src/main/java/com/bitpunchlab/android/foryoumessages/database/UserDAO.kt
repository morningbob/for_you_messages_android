package com.bitpunchlab.android.foryoumessages.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.bitpunchlab.android.foryoumessages.models.*

@Dao
interface UserDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: UserEntity)

    @Query("SELECT * FROM user_table WHERE :id == userID")
    fun getUser(id: String) : LiveData<UserEntity>

    @Transaction
    @Query("SELECT * FROM user_table WHERE :id == userID LIMIT 1" )
    fun getUserWithContactListsAndContacts(id: String): LiveData<UserWithContactListsAndContacts>

    @Transaction
    @Query("SELECT * FROM user_table WHERE :id == userID LIMIT 1")
    fun getUserFirebase(id: String) : UserWithContactListsAndContacts

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertContactListContactCrossRefs(vararg crossRef: ContactListContactCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertContacts(vararg contacts: ContactEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertContactList(vararg contactList: ContactList)
}