package com.lumi.app

import android.app.Application
import com.lumi.app.core.di.AppContainer

class LumiApp : Application() {

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        com.lumi.app.core.i18n.AppText.init(this)
    }

    companion object {
        /** Contenedor de dependencias de toda la app (inyección manual). */
        lateinit var container: AppContainer
            private set
    }
}
