package com.arnyminerz.cea.app.storage.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.arnyminerz.cea.app.annotation.Attribute
import com.arnyminerz.cea.app.data.InventoryItem
import com.arnyminerz.cea.app.data.Section

@Entity(tableName = "inventory_items")
data class InventoryItemEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "display_name") val displayName: String,
    @ColumnInfo(name = "section_id") val sectionId: String,
    @ColumnInfo(name = "quantity") val quantity: Long,
    @ColumnInfo(name = "attributes") val attributes: Map<@Attribute String, String>,
    val price: InventoryItem.Price?,
) {
    companion object {
        fun valueOf(item: InventoryItem): InventoryItemEntity =
            InventoryItemEntity(
                item.id,
                item.displayName,
                item.section.id,
                item.quantity,
                item.attributes,
                item.price,
            )
    }

    fun toInventoryItem(section: Section) =
        InventoryItem(id, displayName, section, quantity, attributes, price)
}