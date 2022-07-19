package com.arnyminerz.cea.app.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.arnyminerz.cea.app.storage.entities.InventoryItemEntity

@Dao
interface InventoryItemDao {
    @Insert
    suspend fun insertAll(vararg items: InventoryItemEntity)

    @Query("DELETE FROM inventory_items")
    suspend fun dispose()
}