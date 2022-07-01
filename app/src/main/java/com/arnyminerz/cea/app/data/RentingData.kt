package com.arnyminerz.cea.app.data

import java.util.Date

data class RentingData(
    val categoryDisplayName: String,
    val item: InventoryItem,
    val date: Date,
    val rentingAmount: UInt,
)
