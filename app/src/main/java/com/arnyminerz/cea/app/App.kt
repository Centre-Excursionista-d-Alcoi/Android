package com.arnyminerz.cea.app

import android.app.Application
import com.arnyminerz.cea.app.provider.TranslationProvider
import timber.log.Timber

class App : Application() {
    private lateinit var translationProvider: TranslationProvider

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        translationProvider = TranslationProvider.getInstance()
    }

    override fun onTerminate() {
        super.onTerminate()
        translationProvider.close()
    }
}