package com.example.agriculture_management.app

import android.app.Application

class AgricultureApplication : Application() {
    lateinit var graph: CompositionRoot
        private set

    override fun onCreate() {
        super.onCreate()
        graph = CompositionRoot.create(this)
    }
}
