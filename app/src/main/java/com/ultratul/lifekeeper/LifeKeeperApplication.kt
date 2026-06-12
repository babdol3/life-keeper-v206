package com.ultratul.lifekeeper

import android.app.Application
import com.ultratul.lifekeeper.notification.NotificationHelper

class LifeKeeperApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
    }
}
