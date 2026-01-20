package com.example.mobile_dev_project.utils

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Process-wide entry point for Hilt.
 * Hilt builds and owns all singletons (DB, DAOs, Repositories) for the app.
 */
@HiltAndroidApp
class ReaderApplication : Application()