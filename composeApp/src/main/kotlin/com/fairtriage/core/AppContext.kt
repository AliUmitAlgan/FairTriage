package com.fairtriage.core

import android.content.Context

object AppContext {
    lateinit var applicationContext: Context
        private set

    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }
}
