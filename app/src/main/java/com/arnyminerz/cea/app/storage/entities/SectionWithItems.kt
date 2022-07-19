package com.arnyminerz.cea.app.storage.entities

import androidx.room.Embedded
import androidx.room.Relation
import com.arnyminerz.cea.app.data.InventoryItem
import com.arnyminerz.cea.app.data.Section

data class SectionWithItems(
    @Embedded val section: SectionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "section_id"
    )
    val items: List<InventoryItemEntity>,
) {
    fun toPair(): Pair<Section, List<InventoryItem>> =
        section.section to items.map { it.toInventoryItem(section.section) }
}
