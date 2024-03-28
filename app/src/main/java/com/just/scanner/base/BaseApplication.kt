package com.just.scanner.base

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.DatabaseReference

@SuppressLint("StaticFieldLeak")
class BaseApplication : Application() {

    private lateinit var mDatabaseRef: DatabaseReference
    private val PROD_VERSION = "prod_v1"

    companion object {
        private lateinit var context: Context

        fun getContext(): Context {
            return context;
        }

        fun setContext(con: Context) {
            context = con
        }
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        setContext(this)
        //disable night mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//        FirebaseApp.initializeApp(this)
//        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)

    }


}