package com.example

import android.util.Log
import com.datadog.android.Datadog
import com.datadog.android.rum.GlobalRumMonitor
import com.datadog.android.rum.RumActionType

object DatadogMonitor {
    fun trackClick(name: String, attributes: Map<String, Any> = emptyMap()) {
        try {
            Log.d("DatadogMonitor", "Tracked TAP action: $name with attributes: $attributes")
            if (Datadog.isInitialized() && GlobalRumMonitor.isRegistered()) {
                GlobalRumMonitor.get().addAction(
                    type = RumActionType.TAP,
                    name = name,
                    attributes = attributes
                )
            }
        } catch (e: Throwable) {
            Log.e("DatadogMonitor", "Error tracking action: ${e.message}", e)
        }
    }
}
