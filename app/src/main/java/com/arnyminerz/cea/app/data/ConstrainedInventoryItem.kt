package com.arnyminerz.cea.app.data

import com.arnyminerz.cea.app.utils.append

/**
 * Describes an [InventoryItem] with constraints on available amounts. This is performed by fetching
 * the server before initializing, and getting all the already made and not returned rents.
 */
data class ConstrainedInventoryItem(
    val inventoryItem: InventoryItem,
    val availableAmount: Long,
)

fun Collection<ConstrainedInventoryItem>.divideInSections(): Map<Section, List<ConstrainedInventoryItem>> {
    val map = hashMapOf<Section, List<ConstrainedInventoryItem>>()
    forEach { item ->
        val section = item.inventoryItem.section
        if (map.containsKey(section))
            map[section] = map.getValue(section).toMutableList().append(item)
        else
            map[section] = listOf(item)
    }
    return map
}
