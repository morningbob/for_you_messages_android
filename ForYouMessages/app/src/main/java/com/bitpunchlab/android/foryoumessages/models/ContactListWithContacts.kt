package com.bitpunchlab.android.foryoumessages.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Junction
import androidx.room.Relation

@Entity
data class ContactListWithContacts(
    @Embedded val contactList: ContactList,
    @Relation(
        parentColumn = "listId",
        entityColumn = "contactEmail",
        associateBy = Junction(ContactListContactCrossRef::class)
    )
    var contacts: List<ContactEntity>
)
