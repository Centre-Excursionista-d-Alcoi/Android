package com.arnyminerz.cea.app.data.companion

import org.json.JSONObject

/**
 * Informs that the class can be converted into a [JSONObject].
 * @author Arnau Mora
 * @since 20220719
 */
interface JsonSerializable {
    /**
     * Serializes the class into a [JSONObject].
     * @author Arnau Mora
     * @since 20220719
     */
    fun toJson(): JSONObject
}