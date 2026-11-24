import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

// URL base del API. Se lee de local.properties (que no se sube a Git) para que cada
// integrante use su propia dirección sin editar código. Ejemplo:
//   lumi.baseUrl.debug=http://192.168.1.2:4010/
//   lumi.baseUrl.release=https://mi-api.ejemplo.com/api/v1/
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

fun apiBaseUrl(key: String, default: String): String {
    val value = localProperties.getProperty(key) ?: default
    return if (value.endsWith("/")) value else "$value/"
}

android {
    namespace = "com.lumi.app"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.lumi.app"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ID de cliente "Web" de Google Cloud (para el inicio de sesión con Google).
        // Se lee de local.properties:  lumi.googleWebClientId=1234-abc.apps.googleusercontent.com
        buildConfigField(
            "String",
            "GOOGLE_WEB_CLIENT_ID",
            "\"${localProperties.getProperty("lumi.googleWebClientId") ?: ""}\""
        )
    }

    buildTypes {
        debug {
            buildConfigField(
                "String",
                "BASE_URL",
                "\"${apiBaseUrl("lumi.baseUrl.debug", "http://192.168.1.2:4010/")}\""
            )
        }
        release {
            optimization {
                enable = false
            }
            buildConfigField(
                "String",
                "BASE_URL",
                "\"${apiBaseUrl("lumi.baseUrl.release", "https://CAMBIAR-URL-DE-LA-NUBE/api/v1/")}\""
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)

    // Retrofit: comunicación con el backend (incluye OkHttp)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    // Room: caché local para usar la app sin conexión
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    // Inicio de sesión con Google (Credential Manager)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // Google Calendar: autorización OAuth del usuario (Google Identity Services)
    implementation(libs.play.services.auth)

    // Corrutinas para operaciones asíncronas
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
