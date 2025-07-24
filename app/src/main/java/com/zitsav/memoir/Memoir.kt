package com.zitsav.memoir

import android.app.Application

class Memoir : Application() {
    override fun onCreate() {
        super.onCreate()
        net.sqlcipher.database.SQLiteDatabase.loadLibs(this)
    }
}