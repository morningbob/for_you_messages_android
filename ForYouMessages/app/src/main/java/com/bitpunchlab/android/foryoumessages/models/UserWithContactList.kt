package com.bitpunchlab.android.foryoumessages.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation

@Entity
class UserWithContactList(
    @Embedded val user: UserEntity,
    @Relation(
        parentColumn = "userID",
        entityColumn = "userCreatorId"
    )
    val contactLists: List<List<Contact>>
)
