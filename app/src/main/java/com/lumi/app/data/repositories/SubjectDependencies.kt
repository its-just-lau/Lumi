package com.lumi.app.data.repositories

import com.lumi.app.LumiApp

/** Se conserva por compatibilidad: ahora el repositorio sale del AppContainer. */
object SubjectDependencies {

    val repository: SubjectRepository
        get() = LumiApp.container.subjectRepository
}
