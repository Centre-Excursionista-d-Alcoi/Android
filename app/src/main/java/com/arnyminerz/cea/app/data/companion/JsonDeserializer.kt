package com.arnyminerz.cea.app.data.companion

import org.json.JSONObject

/**
 * Used by companion objects to determine that the class can be instantiated from a [JSONObject].
 * @author Arnau Mora
 * @since 20220719
 * @param T The target type that the class converts into.
 */
interface JsonDeserializer<T> {
    /**
     * Converts a [JSONObject] to [T].
     * @author Arnau Mora
     * @since 20220719
     */
    fun fromJson(json: JSONObject): T
}