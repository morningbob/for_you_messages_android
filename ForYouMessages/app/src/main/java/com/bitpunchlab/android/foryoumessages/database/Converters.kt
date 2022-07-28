package com.bitpunchlab.android.foryoumessages.database

import androidx.room.TypeConverter
import com.bitpunchlab.android.foryoumessages.models.Contact
import com.google.common.reflect.TypeToken
import com.google.gson.Gson

class Converters {
    @TypeConverter
    fun contactsFromString(contactsString: String) : List<Contact> {
        val objectType = object : TypeToken<List<Contact>>() { }.type
        return Gson().fromJson<List<Contact>>(contactsString, objectType)
    }

    @TypeConverter
    fun contactsToString(contacts: List<Contact>) : String {
        return Gson().toJson(contacts)
    }
}