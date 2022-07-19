package com.arnyminerz.cea.app.data

import android.os.Parcelable
import com.arnyminerz.cea.app.data.companion.FirestoreSerializable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class ReturnData(
    val returnedByUid: String,
    val timestamp: Date,
) : Parcelable, FirestoreSerializable {
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
                (obj["timestamp"] as Timestamp).toDate(),
            )
        }
    }

    override suspend fun toFirestoreMap(): Map<String, *> = mapOf(
        "returned_by" to returnedByUid,
        "timestamp" to Timestamp(timestamp),
    )
}
