package com.arnyminerz.cea.app.provider

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.Locale

class TranslationProvider private constructor() {
    companion object {
        @Volatile
        private var INSTANCE: TranslationProvider? = null

        fun getInstance(): TranslationProvider =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: TranslationProvider().also { INSTANCE = it }
            }
    }

    private val translationClient = Translation.getClient(
        TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.CATALAN)
            .setTargetLanguage(
                TranslateLanguage.getAllLanguages()
                    .find { it == Locale.getDefault().language }!!,
            )
            .build()
    )

    private suspend fun downloadModel() =
        translationClient
            .also { Timber.d("Downloading translation model...") }
            .downloadModelIfNeeded(
                DownloadConditions.Builder()
                    .requireWifi()
                    .build()
            )
            .addOnSuccessListener {
                Timber.i("Downloaded translation model.")
            }
            .addOnFailureListener { e -> Timber.e(e, "Could not download translation model") }
            .await()

    fun translate(text: String): String =
        if (Locale.getDefault().language == "ca")
            text
        else runBlocking {
            downloadModel()
            translationClient.translate(text).await()
        }

    fun close() = translationClient.close()
}