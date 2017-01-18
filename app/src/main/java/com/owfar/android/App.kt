package com.owfar.android

import android.app.Application
import android.content.Context
import android.support.multidex.MultiDex
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.owfar.android.connectivity.ConnectivityManager
import com.owfar.android.data.DataManager
import com.owfar.android.media.MediaHelper
import com.owfar.android.settings.CurrentUserSettings
import com.owfar.android.ui.registration.RegistrationActivity
import io.realm.Realm
import io.realm.RealmConfiguration

class App : Application(), Thread.UncaughtExceptionHandler {

    private var defaultHandler: Thread.UncaughtExceptionHandler? = null

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)

    }

    override fun onCreate() {
        super.onCreate()
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)

        FacebookSdk.sdkInitialize(this)
        AppEventsLogger.activateApp(this)
        CurrentUserSettings.init(this)
        ConnectivityManager.init(this)
        MediaHelper.init(this)

        Realm.init(this)
        Realm.setDefaultConfiguration(RealmConfiguration
                .Builder()
                .schemaVersion(12)
                .deleteRealmIfMigrationNeeded()
                .build())
        DataManager.init(this)
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        throwable.printStackTrace()
        when {
            throwable.contain(InvalidTokenException::class.java) -> RegistrationActivity.start(this)
            else -> defaultHandler?.uncaughtException(thread, throwable)
        }
        System.exit(1)
    }
}

fun Throwable.contain(clazz: Class<out Throwable>): Boolean = let {
    if (clazz.isInstance(this)) true
    else cause?.contain(clazz) ?: false
}