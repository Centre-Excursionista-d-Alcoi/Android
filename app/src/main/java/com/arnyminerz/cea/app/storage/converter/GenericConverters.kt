package com.arnyminerz.cea.app.storage.converter

import androidx.room.TypeConverter
import com.arnyminerz.cea.app.data.InventoryItem
import com.arnyminerz.cea.app.data.companion.JsonSerializable
import com.arnyminerz.cea.app.utils.jsonObject

class GenericConverters {
    @TypeConverter
    fun fromJson(value: JsonSerializable): String =
        value.toJson().toString()

    @TypeConverter
    fun toPrice(json: String): InventoryItem.Price =
        InventoryItem.Price.fromJson(json.jsonObject)
}