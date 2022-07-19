package com.arnyminerz.cea.app.data.companion

import com.google.firebase.firestore.CollectionReference

/**
 * Informs that the class can be inserted into Firestore.
 * @author Arnau Mora
 * @since 20220719
 */
interface FirestoreSerializable {
    suspend fun toFirestoreMap(): Map<String, *>
}

suspend fun CollectionReference.addSerializable(obj: FirestoreSerializable) =
    add(obj.toFirestoreMap())
