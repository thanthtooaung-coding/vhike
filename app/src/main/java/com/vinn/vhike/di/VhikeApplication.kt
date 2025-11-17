package com.vinn.vhike.di

import android.app.Application
import com.google.android.libraries.places.api.Places
import com.vinn.vhike.R
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VhikeApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Places.initialize(applicationContext, getString(R.string.google_maps_key))
    }
}