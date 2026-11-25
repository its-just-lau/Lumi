package com.lumi.app.core.di

import android.content.Context
import com.lumi.app.core.network.AuthService
import com.lumi.app.core.network.RetrofitClient
import com.lumi.app.core.network.GoogleCalendarService
import com.lumi.app.core.network.MaterialService
import com.lumi.app.core.network.ProgressService
import com.lumi.app.core.network.StoreService
import com.lumi.app.core.network.StudyService
import com.lumi.app.core.network.SubjectService
import com.lumi.app.core.network.TaskService
import com.lumi.app.core.network.UserService
import com.lumi.app.core.storage.SessionManager
import com.lumi.app.core.storage.db.CacheStore
import com.lumi.app.core.storage.db.LumiDatabase
import com.lumi.app.core.storage.TokenStorage
import com.lumi.app.data.repositories.AuthRepository
import com.lumi.app.data.repositories.GoogleCalendarRepository
import com.lumi.app.data.repositories.MaterialRepository
import com.lumi.app.data.repositories.ProgressRepository
import com.lumi.app.data.repositories.StoreRepository
import com.lumi.app.data.repositories.StudyRepository
import com.lumi.app.data.repositories.SubjectRepository
import com.lumi.app.data.repositories.TaskRepository
import com.lumi.app.data.repositories.UserRepository
import retrofit2.Retrofit

/**
 * Crea y comparte las dependencias de la app. Cada funcionalidad nueva agrega aquí
 * su servicio y su repositorio.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val tokenStorage = TokenStorage(appContext)
    val sessionManager = SessionManager(tokenStorage)

    // Caché local (Room) para consultar datos sin conexión
    private val database: LumiDatabase by lazy { LumiDatabase.create(appContext) }
    val cacheStore: CacheStore by lazy { CacheStore(database.cacheDao()) }

    // Cliente SIN token: solo para los endpoints de autenticación (auth).
    private val publicRetrofit: Retrofit by lazy {
        RetrofitClient.createPublicRetrofit()
    }

    val authService: AuthService by lazy {
        publicRetrofit.create(AuthService::class.java)
    }

    // Cliente CON token y renovación automática: para el resto del API.
    val retrofit: Retrofit by lazy {
        RetrofitClient.createAuthenticatedRetrofit(sessionManager, authService)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(authService, sessionManager, cacheStore)
    }

    val subjectService: SubjectService by lazy {
        retrofit.create(SubjectService::class.java)
    }

    val subjectRepository: SubjectRepository by lazy {
        SubjectRepository(subjectService, cacheStore)
    }

    val taskService: TaskService by lazy {
        retrofit.create(TaskService::class.java)
    }

    val taskRepository: TaskRepository by lazy {
        TaskRepository(taskService, cacheStore)
    }

    val studyService: StudyService by lazy {
        retrofit.create(StudyService::class.java)
    }

    val studyRepository: StudyRepository by lazy {
        StudyRepository(studyService)
    }

    val materialService: MaterialService by lazy {
        retrofit.create(MaterialService::class.java)
    }

    val materialRepository: MaterialRepository by lazy {
        MaterialRepository(materialService, cacheStore)
    }

    val progressService: ProgressService by lazy {
        retrofit.create(ProgressService::class.java)
    }

    val progressRepository: ProgressRepository by lazy {
        ProgressRepository(progressService)
    }

    val storeService: StoreService by lazy {
        retrofit.create(StoreService::class.java)
    }

    val storeRepository: StoreRepository by lazy {
        StoreRepository(storeService)
    }

    // Google Calendar usa su propio Retrofit: no lleva los tokens de Lumi
    val googleCalendarService: GoogleCalendarService by lazy {
        Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/calendar/v3/")
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(GoogleCalendarService::class.java)
    }

    val googleCalendarRepository: GoogleCalendarRepository by lazy {
        GoogleCalendarRepository(googleCalendarService)
    }

    val userService: UserService by lazy {
        retrofit.create(UserService::class.java)
    }

    val userRepository: UserRepository by lazy {
        UserRepository(userService)
    }
}
