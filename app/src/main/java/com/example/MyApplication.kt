package com.example

import android.app.Application

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Datadog initialization is disabled because its dynamic receiver registration 
        // causes unrecoverable SecurityExceptions on Android 14+ (Target SDK 36).
    }
}
