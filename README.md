# Lumi

Lumi es una aplicación Android para organizar el estudio: materias y semestres, tareas, sesiones de estudio, materiales con contenido generado por IA, progreso, recompensas y una tienda con gemas. El repositorio incluye la app Android y, a partir de la Fase 1 del plan, el backend REST que implementa el contrato `api/openapi.yaml` (v1.4.0).

## Estado del repositorio

- **App Android:** funcional y en desarrollo activo. No se modifica como parte del plan del backend.
- **Backend:** en construcción. La Fase 1 deja el esqueleto de FastAPI funcionando (`/api/v1/health`, Swagger, errores del contrato y CI); las fases 2 a 9 añaden base de datos, autenticación, dominio y despliegue.
- **Contrato:** `api/openapi.yaml` es la fuente de verdad. El backend debe cumplirlo y la app lo consume mediante Retrofit.

## Organización

```text
Lumi/
├── app/                  # App Android (Kotlin + Jetpack Compose)
├── api/
│   └── openapi.yaml      # Contrato REST v1.4.0
├── backend/              # API FastAPI (se implementa por fases)
├── docs/                 # Guías de Google, uso de IA y documentación interna
├── reportes/             # Reportes de análisis (ignorado por Git)
├── gradle/               # Catálogo de versiones y wrapper
├── .github/workflows/    # CI de Android y, más adelante, del backend
├── AGENTS.md             # Guía local para agentes de IA (ignorada por Git)
└── PLAN_EJECUTOR_LUMI.md # Plan de trabajo local (ignorado por Git)
```

## App Android

### Requisitos

- JDK 21 para lanzar Gradle en CI. En local, el wrapper solicita Java 25 para el daemon y lo aprovisiona automáticamente.
- Android SDK instalado. `local.properties` debe incluir `sdk.dir`.
- El wrapper de Gradle incluido (`gradlew.bat` en Windows, `./gradlew` en macOS/Linux).

### Configuración local

`local.properties` está ignorado por Git y se lee en tiempo de configuración. Claves usadas por `app/build.gradle.kts`:

```properties
sdk.dir=C\:\\ruta\\al\\Android\\Sdk
lumi.baseUrl.debug=http://192.168.1.2:4010/
lumi.baseUrl.release=https://tu-servidor.example/api/v1/
lumi.googleWebClientId=tu-client-id.apps.googleusercontent.com
```

- `lumi.baseUrl.debug` apunta por defecto al mock de Prism en el puerto `4010`.
- La barra final se agrega automáticamente; las rutas de Retrofit son relativas.
- La URL de release debe usar HTTPS. El build de debug permite HTTP en claro.
- Si no hay `local.properties` (por ejemplo en CI), se usan los valores por defecto y Google Sign-In queda sin configurar.

### Comandos

```powershell
# Pruebas unitarias
.\gradlew.bat testDebugUnitTest

# Una prueba específica
.\gradlew.bat testDebugUnitTest --tests "com.lumi.app.features.store.StoreClassificationTest"

# Lint
.\gradlew.bat lintDebug

# APK de debug
.\gradlew.bat assembleDebug
```

El CI de Android (`.github/workflows/android-ci.yml`) ejecuta, en este orden: `testDebugUnitTest`, `lintDebug` y `assembleDebug`, con JDK 21.

### Arquitectura de la app

- `MainActivity` → `AppNavigation`; la inyección de dependencias es manual en `LumiApp.container` (`core/di/AppContainer.kt`).
- Pantallas en `features/<feature>/`, cada una con su ViewModel y su fábrica.
- Red en `core/network`: Retrofit público para autenticación y Retrofit autenticado con `AuthInterceptor` y `TokenAuthenticator`.
- Caché en Room (`core/storage/db`) con estrategia network-first; solo se usa la caché ante `IOException`.
- El idioma por defecto de la interfaz es español; los textos viven en `values/strings.xml` y `values-en/strings.xml`.

Las convenciones detalladas para contribuir están en `AGENTS.md`.

## Backend

La guía completa está en [`backend/README.md`](backend/README.md). Arranque rápido en Windows (Python 3.12):

```powershell
cd backend
py -3.12 -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt -r requirements-dev.txt
Copy-Item .env.example .env
uvicorn app.main:app --reload
```

- Swagger: `http://localhost:8000/docs`
- Salud: `http://localhost:8000/api/v1/health`
- Pruebas: `pytest` y `ruff check .` desde `backend/`

Resumen del stack:

- Python 3.12 + FastAPI + Pydantic v2.
- PostgreSQL en Neon (SQLAlchemy + Alembic).
- MongoDB Atlas para contenido generado y archivos.
- RabbitMQ en CloudAMQP para la cola de procesamiento.
- Gemini detrás de la interfaz `AiProvider`.
- Render para el despliegue y GitHub Actions para CI/CD.

## Contrato y documentación

- `api/openapi.yaml`: contrato REST v1.4.0, con 72 operaciones y 58 esquemas.
- `docs/google-login.md`: configuración de Google Sign-In.
- `docs/google-calendar.md`: integración con Google Calendar.
- `docs/uso-de-ia.md`: restricciones del proyecto sobre contenido generado por IA.
- `docs/ia/`: bitácora versionada de uso de IA (excepción justificada en `.gitignore`).

## Git y colaboración

- Ramas personales por integrante; los cambios llegan a `develop` mediante Pull Request al cerrar cada fase, y a `main` en un único Pull Request al terminar todas las fases.
- Convención de commits: Conventional Commits con alcance, por ejemplo `feat(auth): registro e inicio de sesión con JWT`.
- `main` y `develop` requieren PR y CI en verde.
- Los secretos, `local.properties`, los archivos `.env` reales y los reportes de `reportes/` nunca se versionan.

El protocolo completo, las fases y el estado del proyecto están en `PLAN_EJECUTOR_LUMI.md`.
