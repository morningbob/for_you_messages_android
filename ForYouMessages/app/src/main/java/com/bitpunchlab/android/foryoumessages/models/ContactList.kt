package com.bitpunchlab.android.foryoumessages.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ContactList(
    @PrimaryKey(autoGenerate = true) val listId: Long,
    val userCreatorId: String,
    val listName: String
)
