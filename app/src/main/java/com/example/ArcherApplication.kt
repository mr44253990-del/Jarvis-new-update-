package com.example

import android.app.Application
import com.example.data.local.ArcherDatabase
import com.example.data.preference.AppPreferences

class ArcherApplication : Application() {
    val database: ArcherDatabase by lazy { ArcherDatabase.getInstance(this) }
    val preferences: AppPreferences by lazy { AppPreferences(this) }

    override fun onCreate() {
        super.onCreate()
    }
}
