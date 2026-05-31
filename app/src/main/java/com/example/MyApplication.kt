package com.example

import android.app.Application

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Setup a global Uncaught Exception Handler to capture and print any startup or runtime failures loudly
        val originalHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            android.util.Log.e("MyApplication", "!!! CRITICAL UNCAUGHT EXCEPTION !!! Thread: ${thread.name}", throwable)
            originalHandler?.uncaughtException(thread, throwable)
        }
    }
}
