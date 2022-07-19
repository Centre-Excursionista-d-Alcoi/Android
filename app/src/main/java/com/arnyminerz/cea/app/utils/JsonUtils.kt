package com.arnyminerz.cea.app.utils

import org.json.JSONObject

/**
 * Initializes a [JSONObject] with this [String].
 * @author Arnau Mora
 * @since 20220719
 */
val String.jsonObject: JSONObject
    get() = JSONObject(this)

/**
 * Converts a [Map] with [String] as key into a [JSONObject].
 * @author Arnau Mora
 * @since 20220719
 */
val <T> Map<String, T>.jsonObject: JSONObject
    get() = JSONObject().apply {
        for (k in this@jsonObject.keys)
            put(k, this@jsonObject.getValue(k))
    }

/**
 * Converts a [JSONObject] into a [Map] with [String] as key.
 * @author Arnau Mora
 * @since 20220719
 */
fun <T> JSONObject.toMap(): Map<String, T> {
    val map = mutableMapOf<String, T>()
    @Suppress("UNCHECKED_CAST")
    for (k in this.keys())
        map[k] = get(k) as T
    return map
}
