package com.arnyminerz.cea.app.storage.converter

import androidx.room.TypeConverter
import com.arnyminerz.cea.app.utils.jsonObject
import com.arnyminerz.cea.app.utils.toMap

abstract class MapConverter<T> {
    @TypeConverter
    fun fromStringMap(value: Map<String, T>): String =
        value.jsonObject.toString()

    @TypeConverter
    fun toStringMap(value: String): Map<String, T> =
        value.jsonObject.toMap()
}