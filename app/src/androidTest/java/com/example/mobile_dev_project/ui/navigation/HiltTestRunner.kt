package com.example.mobile_dev_project.ui.navigation

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        // Use the special Hilt test application instead of your real one
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}