package com.bitpunchlab.android.foryoumessages.models

import androidx.room.Entity

@Entity(primaryKeys = ["userID", "contactEmail"])
data class UserContactEntityCrossRef (
    val userID: String,
    val contactEmail: String
        )
