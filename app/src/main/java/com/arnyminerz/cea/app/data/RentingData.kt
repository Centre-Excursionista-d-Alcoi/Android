package com.arnyminerz.cea.app.data

import android.os.Parcelable
import com.arnyminerz.cea.app.data.companion.FirestoreDeserializer
import com.arnyminerz.cea.app.data.companion.FirestoreSerializable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import kotlinx.parcelize.Parcelize
import java.util.Date

/**
 * Defines a renting event, this is, when an user has rented an [InventoryItem], this describes what
 * has been lent.
 *
 * [startDate] and [endDate] get used to calculate the cost of the renting.
 * @author Arnau Mora
 * @since 20220719
 * @param timestamp The moment when the rent was registered.
 * @param item The item that has been lent.
 * @param amount The amount of items taken.
 * @param startDate The date in which the item will start being used.
 * @param endDate The date in which the item will stop being used.
 * @param returned If not null, notifies that the item has already been returned.
 */
@Parcelize
data class RentingData(
    val id: String,
    val timestamp: Date,
    val userUid: String,
    val item: InventoryItem,
    @androidx.annotation.IntRange(from = 0)
    val amount: Long,
    val startDate: Date?,
    val endDate: Date?,
    val returned: ReturnData?,
) : FirestoreSerializable, Parcelable {
    companion object : FirestoreDeserializer<RentingData>() {
        override suspend fun fromDocument(document: DocumentSnapshot): RentingData = RentingData(
            document.id,
            document.getDate("timestamp")!!,
            document.getString("user")!!,
            document.getDocumentReference("item")!!.let { InventoryItem.fromReference(it) },
            document.getLong("amount")!!,
            document.getDate("start_date"),
            document.getDate("end_date"),
            document.get("returned")?.let { ReturnData.fromObject(it) },
        )
    }

    /**
     * Whether or not the item has already been returned.
     * @author Arnau Mora
     * @since 20220719
     */
    val hasBeenReturned = returned != null

    /**
     * Calculates the cost of the renting between the given dates. If the item doesn't have any price,
     * [startDate] is null, or [endDate] is null, [cost] will also be null.
     * @author Arnau Mora
     * @since 20220719
     * @see InventoryItem.Price.calculatePrice
     */
    val cost = startDate?.let { s -> endDate?.let { e -> item.price?.calculatePrice(s, e) } }

    override suspend fun toFirestoreMap() = mapOf(
        "timestamp" to Timestamp(timestamp),
        "user" to userUid,
        "item" to Firebase.firestore.collection("inventory").document(item.id).get()
            .await().reference,
        "amount" to amount,
        "start_date" to startDate,
        "end_date" to endDate,
        "returned" to returned?.let { ret ->
            mapOf("returned_by" to ret.returnedByUid, "timestamp" to Timestamp(ret.timestamp))
        },
    )
}
