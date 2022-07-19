package com.arnyminerz.cea.app.provider

import android.content.Context
import androidx.annotation.WorkerThread
import com.arnyminerz.cea.app.data.ConstrainedInventoryItem
import com.arnyminerz.cea.app.data.InventoryItem
import com.arnyminerz.cea.app.data.RentingData
import com.arnyminerz.cea.app.data.ReturnData
import com.arnyminerz.cea.app.data.Section
import com.arnyminerz.cea.app.data.companion.addSerializable
import com.arnyminerz.cea.app.storage.Database
import com.arnyminerz.cea.app.storage.entities.InventoryItemEntity
import com.arnyminerz.cea.app.storage.entities.SectionEntity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class RentingDataProvider private constructor(context: Context) {
    companion object {
        @Volatile
        private var INSTANCE: RentingDataProvider? = null

        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: RentingDataProvider(context).also { INSTANCE = it }
            }
    }

    private val firestore = Firebase.firestore

    private val database = Database.getInstance(context)

    /**
     * Gets all the sections available from the Firestore database.
     * @author Arnau Mora
     * @since 20220719
     * @return A list of [Section]s.
     * @throws FirebaseFirestoreException When there has been an exception while fetching the data.
     * @throws NullPointerException When an element in the database doesn't have the correct structure.
     * @throws ClassCastException When a field in the document doesn't have the correct type.
     */
    @WorkerThread
    @Throws(
        FirebaseFirestoreException::class,
        NullPointerException::class,
        ClassCastException::class,
    )
    private suspend fun fetchSectionsFromFirebase() =
        firestore.collection("sections")
            .get()
            .await()
            .map { Section.fromDocument(it) }

    /**
     * Gets all the inventory items available from the Firestore database.
     * @author Arnau Mora
     * @since 20220719
     * @param sections A list of the available sections. Those are used for initializing the
     * [InventoryItem] without having to fetch again and again all the sections from the server.
     * @return A list of [InventoryItem]s.
     * @throws FirebaseFirestoreException When there has been an exception while fetching the data.
     * @throws NullPointerException When an element in the database doesn't have the correct structure.
     * @throws ClassCastException When a field in the document doesn't have the correct type.
     */
    @WorkerThread
    @Throws(
        FirebaseFirestoreException::class,
        NullPointerException::class,
        ClassCastException::class,
    )
    private suspend fun fetchItemsFromFirebase(sections: List<Section>) =
        firestore.collection("inventory")
            .get()
            .await()
            .documents
            .map { InventoryItem.fromDocument(it, sections) }

    /**
     * Fetches all the registered renting from Firestore.
     * @author Arnau Mora
     * @since 20220719
     * @throws FirebaseFirestoreException When there has been an exception while fetching the data.
     * @throws NullPointerException When an element in the database doesn't have the correct structure.
     * @throws ClassCastException When a field in the document doesn't have the correct type.
     */
    @WorkerThread
    @Throws(
        FirebaseFirestoreException::class,
        NullPointerException::class,
        ClassCastException::class,
    )
    private suspend fun fetchRentingFromFirebase() =
        firestore.collection("renting")
            .get()
            .await()
            .documents
            .map { RentingData.fromDocument(it) }

    /**
     * Fetches all the items available in the inventory.
     * @author Arnau Mora
     * @since 20220719
     */
    @WorkerThread
    suspend fun fetchInventory(): Map<Section, List<InventoryItem>> =
        database
            // Get the DAO for fetching from the Room database
            .sectionDao
            // Fetch all the stored sections related with their items
            .getSectionsWithItems()
            // Convert to map of sections and items
            .associate { it.toPair() }
            // Return the result if it's not empty
            .takeIf { it.isNotEmpty() }
            ?:
            // If it's empty, fetch the data from Firebase
            fetchSectionsFromFirebase().let { sections ->
                sections
                    // Convert the loaded data to Section entities
                    .map { SectionEntity.fromSection(it) }
                    // Store all the new entities in the database
                    .also { database.sectionDao.insertAll(*it.toTypedArray()) }
                // Fetch all the inventory items from Firebase, using the loaded sections
                var items: List<InventoryItem>
                fetchItemsFromFirebase(sections)
                    // Store the loaded items
                    .also { items = it }
                    // Convert the loaded data to InventoryItem entities
                    .map { InventoryItemEntity.valueOf(it) }
                    // Store all the new entities in the database
                    .also { database.itemsDao.insertAll(*it.toTypedArray()) }
                sections.associateWith { section -> items.filter { section.id == it.section.id } }
            }

    @WorkerThread
    suspend fun fetchRentingData(): List<ConstrainedInventoryItem> {
        val inventory = fetchInventory()
        val rentingData = fetchRentingFromFirebase()

        val itemsAmount = hashMapOf<String, Long>()
        rentingData
            .filter { !it.hasBeenReturned }
            .forEach { data ->
                val itemId = data.item.id
                val amount = data.amount
                val countAmount = itemsAmount[itemId] ?: 0
                itemsAmount[itemId] = countAmount + amount
            }

        return inventory
            .values
            .flatten()
            .map { inventoryItem ->
                ConstrainedInventoryItem(
                    inventoryItem,
                    inventoryItem.quantity - (itemsAmount[inventoryItem.id] ?: 0),
                )
            }
    }

    /**
     * Gets the renting data for a specific user from Firebase.
     * @author Arnau Mora
     * @since 20220719
     * @param userUid The uid of the user to fetch the data from.
     */
    @WorkerThread
    suspend fun fetchUserRentingData(userUid: String): List<RentingData> =
        firestore.collection("renting")
            .whereEqualTo("user", userUid)
            .get()
            .await()
            .documents
            .map { RentingData.fromDocument(it) }

    /**
     * Notifies the server that an item has been returned.
     * @author Arnau Mora
     * @since 20220720
     * @param rentingData The information about the renting to return.
     */
    @WorkerThread
    suspend fun returnRent(
        rentingData: RentingData,
        userUid: String = Firebase.auth.currentUser!!.uid
    ) =
        firestore.collection("renting")
            .document(rentingData.id)
            .update(
                mapOf(
                    "returned" to mapOf(
                        "returned_by" to userUid,
                        "timestamp" to FieldValue.serverTimestamp(),
                    )
                )
            )
            .await()
            .let { rentingData.copy(returned = ReturnData(userUid, Calendar.getInstance().time)) }

    /**
     * Deletes all stored data from the database, so it has to be loaded again from the server when
     * requested.
     * @author Arnau Mora
     * @since 20220719
     */
    @WorkerThread
    suspend fun dispose() {
        database.sectionDao.dispose()
        database.itemsDao.dispose()
    }

    @WorkerThread
    suspend fun rent(items: List<RentingData>) =
        firestore.collection("renting")
            .let { coll -> items.map { coll.addSerializable(it).await() } }
}