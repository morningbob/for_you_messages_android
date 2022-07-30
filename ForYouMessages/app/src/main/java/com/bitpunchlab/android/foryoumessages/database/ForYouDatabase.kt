package com.bitpunchlab.android.foryoumessages.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bitpunchlab.android.foryoumessages.models.*
import kotlinx.coroutines.InternalCoroutinesApi

@Database(entities = [UserEntity::class, ContactEntity::class, ContactList::class,
                     ContactListWithContacts::class, ContactListContactCrossRef::class,
                     UserWithContactListsAndContacts::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ForYouDatabase : RoomDatabase() {
    abstract val userDAO: UserDAO

    companion object {
        @Volatile
        private var INSTANCE: ForYouDatabase? = null

        @InternalCoroutinesApi
        fun getInstance(context: Context?): ForYouDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context!!.applicationContext,
                        ForYouDatabase::class.java,
                        "foryou_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()

                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}