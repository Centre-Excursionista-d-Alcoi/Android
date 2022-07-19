package com.arnyminerz.cea.app.storage

import android.content.Context
import androidx.room.Room

class Database private constructor(context: Context) {
    companion object {
        @Volatile
        private var INSTANCE: Database? = null

        const val DATABASE_NAME = "cea-database"

        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Database(context).also { INSTANCE = it }
            }
    }

    private val database = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java, DATABASE_NAME
    ).build()

    val sectionDao = database.sectionDao()

    val itemsDao = database.itemsDao()
}