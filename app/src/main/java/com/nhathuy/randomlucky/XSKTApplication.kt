package com.nhathuy.randomlucky

import android.app.Application
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class XSKTApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize AdMob
        MobileAds.initialize(this) { initializationStatus ->
            // AdMob SDK initialized
        }
    }
}
