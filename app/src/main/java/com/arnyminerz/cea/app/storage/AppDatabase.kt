package com.arnyminerz.cea.app.storage

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.arnyminerz.cea.app.storage.converter.GenericConverters
import com.arnyminerz.cea.app.storage.converter.StringMapConverter
import com.arnyminerz.cea.app.storage.dao.InventoryItemDao
import com.arnyminerz.cea.app.storage.dao.SectionDao
import com.arnyminerz.cea.app.storage.entities.InventoryItemEntity
import com.arnyminerz.cea.app.storage.entities.SectionEntity

@Database(
    entities = [SectionEntity::class, InventoryItemEntity::class],
    version = 1,
)
@TypeConverters(GenericConverters::class, StringMapConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sectionDao(): SectionDao

    abstract fun itemsDao(): InventoryItemDao
}
