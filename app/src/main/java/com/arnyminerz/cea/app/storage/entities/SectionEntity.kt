package com.arnyminerz.cea.app.storage.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.arnyminerz.cea.app.data.Section

@Entity(tableName = "sections")
data class SectionEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "display_name") val displayName: String,
) {
    companion object {
        fun fromSection(section: Section) =
            SectionEntity(section.id, section.displayName)
    }

    @Ignore
    val section: Section = Section(id, displayName)
}
