package com.arnyminerz.cea.app.data

import java.util.Date

data class ReturnData(
    val returnedByUid: String,
    val timestamp: Date,
) {
    companion object {
        /**
         * Converts an object into [ReturnData].
         * @author Arnau Mora
         * @since 20220719
         * @param obj Must be a [Map<String, *>], otherwise [ClassCastException] will be thrown.
         * @throws ClassCastException If a field in the document is not correct.
         */
        @Throws(ClassCastException::class)
        fun fromObject(obj: Any): ReturnData {
            obj as Map<*, *>
            return ReturnData(
                obj["returned_by"] as String,
                obj["timestamp"] as Date,
            )
        }
    }
}
