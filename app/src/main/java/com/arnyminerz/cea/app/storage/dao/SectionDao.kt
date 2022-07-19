package com.arnyminerz.cea.app.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.arnyminerz.cea.app.storage.entities.SectionEntity
import com.arnyminerz.cea.app.storage.entities.SectionWithItems

@Dao
interface SectionDao {
    @Query("SELECT * FROM sections")
    suspend fun getAll(): List<SectionEntity>

    @Insert
    suspend fun insertAll(vararg sections: SectionEntity)

    @Transaction
    @Query("SELECT * FROM sections")
    suspend fun getSectionsWithItems(): List<SectionWithItems>

    @Query("DELETE FROM sections")
    suspend fun dispose()
}