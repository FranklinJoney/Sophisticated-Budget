package com.example

import android.app.Application
import com.datadog.android.Datadog
import com.datadog.android.core.configuration.Configuration
import com.datadog.android.privacy.TrackingConsent
import com.datadog.android.rum.Rum
import com.datadog.android.rum.RumConfiguration

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val applicationId = "d8f9d937-1d85-4d97-b147-79c5da41066b"
        val clientToken = "pub93eb165bd67d014bf7cd5d0affcd4bef"
        val environmentName = "dev"
        val appVariantName = "qa"

        // Configure Datadog
        val configuration = Configuration.Builder(
            clientToken = clientToken,
            env = environmentName,
            variant = appVariantName
        ).build()

        // Initialize Datadog with Context
        Datadog.initialize(this, configuration, TrackingConsent.GRANTED)

        // Configure and enable RUM
        val rumConfig = RumConfiguration.Builder(applicationId)
            .trackUserInteractions()
            .trackLongTasks()
            .build()
        Rum.enable(rumConfig)
    }
}
