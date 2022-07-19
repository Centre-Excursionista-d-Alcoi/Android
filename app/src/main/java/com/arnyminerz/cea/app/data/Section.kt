package com.arnyminerz.cea.app.data

import com.arnyminerz.cea.app.data.companion.FirestoreDeserializer
import com.google.firebase.firestore.DocumentSnapshot

/**
 * Represents a section of the club. Used by [InventoryItem]s to reference where they are stored and
 * related.
 * @author Arnau Mora
 * @since 20220719
 * @param id The ID of the section in the db.
 * @param displayName The name of the section to display to the user.
 */
data class Section(
    val id: String,
    val displayName: String,
) {
    companion object : FirestoreDeserializer<Section>() {
        override suspend fun fromDocument(document: DocumentSnapshot): Section =
            Section(
                document.id,
                document.getString("displayName")!!,
            )
    }
}
