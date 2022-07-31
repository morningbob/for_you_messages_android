package com.bitpunchlab.android.foryoumessages.models

import androidx.room.*

//@SuppressWarnings(RoomWarnings.PRIMARY_KEY_FROM_EMBEDDED_IS_DROPPED)
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
