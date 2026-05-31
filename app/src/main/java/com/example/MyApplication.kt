package com.example

import android.app.Application
import com.datadog.android.Datadog
import com.datadog.android.DatadogSite
import com.datadog.android.core.configuration.Configuration
import com.datadog.android.privacy.TrackingConsent
import com.datadog.android.rum.Rum
import com.datadog.android.rum.RumConfiguration

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Setup a global Uncaught Exception Handler to capture and print any startup or runtime failures loudly
        val originalHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            android.util.Log.e("MyApplication", "!!! CRITICAL UNCAUGHT EXCEPTION !!! Thread: ${thread.name}", throwable)
            originalHandler?.uncaughtException(thread, throwable)
        }

        if (!isRunningTest()) {
            initDatadog()
        }
    }

    private fun isRunningTest(): Boolean {
        return try {
            Class.forName("org.robolectric.Robolectric")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }

    private fun initDatadog() {
        val applicationId = "d8f9d937-1d85-4d97-b147-79c5da41066b"
        val clientToken = "pub93eb165bd67d014bf7cd5d0affcd4bef"
        val environmentName = "dev"
        val appVariantName = "qa"

        try {
            // Configure Datadog
            val configuration = Configuration.Builder(
                clientToken = clientToken,
                env = environmentName,
                variant = appVariantName
            )
            .useSite(DatadogSite.US5)
            .build()

            // Initialize Datadog with Context
            Datadog.initialize(this, configuration, TrackingConsent.GRANTED)

            // Configure and enable RUM defensively (using standard custom tracking to avoid window listener conflicts)
            val rumConfig = RumConfiguration.Builder(applicationId)
                .build()
            Rum.enable(rumConfig)
        } catch (e: Throwable) {
            android.util.Log.e("MyApplication", "Failed to initialize Datadog", e)
        }
    }
}
