package com.example

import android.util.Log

object DatadogMonitor {
    fun trackClick(name: String, attributes: Map<String, Any> = emptyMap()) {
        try {
            Log.d("DatadogMonitor", "Tracked TAP action: $name with attributes: $attributes")
        } catch (e: Throwable) {
            Log.e("DatadogMonitor", "Error tracking action: ${e.message}", e)
        }
    }
}
