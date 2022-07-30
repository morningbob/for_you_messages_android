package com.bitpunchlab.android.foryoumessages.models

import androidx.room.Entity

@Entity(primaryKeys = ["listId", "contactEmail"])
data class ContactListContactCrossRef(
    val listId: Long,
    val contactEmail: String
)
