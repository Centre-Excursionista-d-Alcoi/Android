package com.arnyminerz.cea.app.data

import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.annotation.WorkerThread
import com.arnyminerz.cea.app.R
import com.arnyminerz.cea.app.annotation.Attribute
import com.arnyminerz.cea.app.data.companion.FirestoreDeserializer
import com.arnyminerz.cea.app.data.companion.JsonDeserializer
import com.arnyminerz.cea.app.data.companion.JsonSerializable
import com.arnyminerz.cea.app.provider.TranslationProvider
import com.arnyminerz.cea.app.utils.hoursDifference
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.parcelize.Parcelize
import org.json.JSONObject
import java.util.Date

@Parcelize
data class InventoryItem(
    val id: String,
    val displayName: String,
    val section: Section,
    val quantity: Long,
    val attributes: Map<@Attribute String, String>,
    val price: Price?,
) : Parcelable {
    companion object : FirestoreDeserializer<InventoryItem>() {
        @WorkerThread
        @Throws(ClassCastException::class, NullPointerException::class)
        override suspend fun fromDocument(document: DocumentSnapshot): InventoryItem =
            @Suppress("UNCHECKED_CAST")
            InventoryItem(
                document.id,
                document.getString("displayName")!!,
                document.getDocumentReference("section")!!.let { Section.fromReference(it) },
                document.getLong("quantity")!!,
                document.get("attributes") as Map<String, String>,
                document.get("price")?.let { price ->
                    price as Map<String, String>
                    val amount = price.getValue("amount")
                    val period = price.getValue("period")
                    Price(amount.toFloat(), PricingPeriod.valueOf(period.uppercase()))
                }
            )

        fun fromDocument(document: DocumentSnapshot, sections: List<Section>): InventoryItem =
            @Suppress("UNCHECKED_CAST")
            InventoryItem(
                document.id,
                document.getString("displayName")!!,
                document.getDocumentReference("section")!!
                    .let { section -> sections.find { it.id == section.id } }!!,
                document.getLong("quantity")!!,
                document.get("attributes") as Map<String, String>,
                document.get("price")?.let { price ->
                    price as Map<String, String>
                    val amount = price.getValue("amount")
                    val period = price.getValue("period")
                    Price(amount.toFloat(), PricingPeriod.valueOf(period.uppercase()))
                }
            )
    }

    val localizedDisplayName: String = TranslationProvider.getInstance().translate(displayName)

    enum class PricingPeriod(val hoursPerPeriod: Long, @StringRes val resourceString: Int) {
        NONE(0, R.string.renting_item_price_free),
        HOURLY(1, R.string.renting_item_price_hourly),
        DAILY(24, R.string.renting_item_price_daily),
        WEEKLY(24 * 7, R.string.renting_item_price_weekly),
        MONTHLY(24 * 30, R.string.renting_item_price_monthly),
        YEARLY(24 * 365, R.string.renting_item_price_yearly),
    }

    @Parcelize
    data class Price(val amount: Float, val period: PricingPeriod) : JsonSerializable, Parcelable {
        /**
         * Computes the price that will cost renting the item with this price for the set dates.
         * @author Arnau Mora
         * @since 20220719
         */
        fun calculatePrice(startDate: Date, endDate: Date): Float {
            val hours = hoursDifference(startDate, endDate).toFloat()
            return period.hoursPerPeriod * hours * amount
        }

        override fun toJson(): JSONObject = JSONObject().apply {
            put("amount", amount.toDouble())
            put("period", period.name)
        }

        companion object : JsonDeserializer<Price> {
            override fun fromJson(json: JSONObject): Price = Price(
                json.getDouble("amount").toFloat(),
                json.getString("period").let { PricingPeriod.valueOf(it) },
            )
        }
    }
}
