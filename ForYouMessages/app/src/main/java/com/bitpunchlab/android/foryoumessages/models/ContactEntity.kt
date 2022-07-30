package com.bitpunchlab.android.foryoumessages.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity
@Parcelize
data class ContactEntity (
        @PrimaryKey var contactEmail: String,
        var contactPhone: String,
        var contactName: String

        ) : Parcelable
