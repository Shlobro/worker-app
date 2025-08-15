package com.example.workertracking

import android.app.Application
import com.example.workertracking.di.AppContainer

class WorkerTrackingApplication : Application() {
    lateinit var container: AppContainer
    
    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}