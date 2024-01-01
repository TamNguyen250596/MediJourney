package com.example.medijourney.common.helpers

import android.app.Application
import android.content.Context
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.managers.InternationManager
import java.util.Locale

class MediJourney: Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        private var instance: MediJourney? = null

        fun getAppContext(): Context {
            return instance!!.applicationContext
        }
    }
}