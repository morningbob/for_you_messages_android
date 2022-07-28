package com.bitpunchlab.android.foryoumessages.models

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class UserWithContacts (
    @Embedded val user: User,
    @Relation(
        parentColumn = "userID",
        entityColumn = "contactEmail",
        associateBy = Junction(UserContactEntityCrossRef::class)
    )
    val contacts: List<Contact>
        )
