package com.lumi.app.core.i18n

import android.content.Context
import androidx.annotation.StringRes

/**
 * Acceso a textos traducidos desde código que no es composable
 * (ViewModels, repositorios, manejo de errores).
 *
 * Se inicializa en [com.lumi.app.LumiApp]. En pruebas unitarias se puede
 * sustituir con [testResolver].
 */
object AppText {

    @Volatile
    private var appContext: Context? = null

    /** Solo para pruebas unitarias: resuelve textos sin Android. */
    @Volatile
    var testResolver: ((Int, Array<out Any>) -> String)? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun get(@StringRes id: Int, vararg args: Any): String {
        testResolver?.let { return it(id, args) }
        val context = appContext ?: return ""
        return if (args.isEmpty()) context.getString(id) else context.getString(id, *args)
    }
}
