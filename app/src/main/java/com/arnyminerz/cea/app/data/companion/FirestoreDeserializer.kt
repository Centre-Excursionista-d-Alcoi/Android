package com.arnyminerz.cea.app.data.companion

import androidx.annotation.WorkerThread
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.tasks.await

abstract class FirestoreDeserializer<T> {
    /**
     * Converts the [document] into the target class.
     * @author Arnau Mora
     * @since 20220719
     * @throws ClassCastException When a field in the document doesn't have the correct type.
     * @throws NullPointerException When the document's structure is not correct.
     */
    @WorkerThread
    @Throws(ClassCastException::class, NullPointerException::class)
    abstract suspend fun fromDocument(document: DocumentSnapshot): T

    /**
     * Instantiates a new [T] from a reference to the document that describes it.
     * @author Arnau Mora
     * @since 20220719
     * @param reference The reference to the [T]'s document in Firestore.
     * @throws NullPointerException When the document that targets the [reference]'s structure
     * is not correct.
     * @throws ClassCastException When a field in the document doesn't have the correct type.
     */
    @WorkerThread
    @Throws(ClassCastException::class, NullPointerException::class)
    suspend fun fromReference(reference: DocumentReference): T =
        reference
            .get()
            .await()
            .let { fromDocument(it) }
}