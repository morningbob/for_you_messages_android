package com.bitpunchlab.android.foryoumessages.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation

@Entity
data class UserWithContactListsAndContacts(
    @Embedded val user: UserEntity,
    @Relation(
        entity = ContactList::class,
        parentColumn = "userID",
        entityColumn = "userCreatorId"
    )
    val contactLists: List<ContactListWithContacts>
)
