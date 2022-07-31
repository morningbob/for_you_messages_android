package com.bitpunchlab.android.foryoumessages.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ContactList(
    @PrimaryKey(autoGenerate = true) var listId: Long = 0L,
    val userCreatorId: String,
    val listName: String
)
