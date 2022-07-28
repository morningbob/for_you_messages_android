package com.bitpunchlab.android.foryoumessages.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bitpunchlab.android.foryoumessages.models.UserEntity

@Dao
interface UserDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: UserEntity)

    @Query("SELECT * FROM user_table WHERE :id == userID")
    fun getUser(id: String) : LiveData<UserEntity>

}