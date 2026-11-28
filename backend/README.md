# Backend de Lumi

API REST del proyecto Lumi. Implementa el contrato `api/openapi.yaml` (v1.4.0) bajo el prefijo `/api/v1`. Esta guía se actualiza en cada fase del plan (`PLAN_EJECUTOR_LUMI.md`).

> **Estado actual: Fase 2.** El API ya tiene PostgreSQL con SQLAlchemy 2.0 y Alembic, una migración inicial reproducible, datos de demostración, `/health` con estado real de la base y un servicio preparado para Render. La autenticación llega en la Fase 3 y la IA en la Fase 6.
>
> **URL pública de desarrollo:** pendiente de crear el servicio en Render (F2-07). Cuando exista, se anota aquí y en `AGENTS.md`.

## Requisitos

- Python 3.12 (local, CI y Render usan la misma versión).
- PowerShell en Windows.
- Git.
- PostgreSQL 17 (Neon, una instancia local o Docker).

## Arranque en Windows (PowerShell)

```powershell
# Desde la raíz del repositorio
cd backend
py -3.12 -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt -r requirements-dev.txt

# Copiar variables de entorno (nunca subir el .env real)
Copy-Item .env.example .env
# Editar .env y colocar DATABASE_URL real

# Crear el esquema y cargar la demo
alembic upgrade head
python scripts/seed_demo.py

# Levantar el servidor de desarrollo
uvicorn app.main:app --reload
```

Abre:

- Swagger: `http://localhost:8000/docs`
- Especificación generada: `http://localhost:8000/openapi.json`
- Salud: `http://localhost:8000/api/v1/health`

Respuesta esperada con la base configurada y disponible:

```json
{
  "status": "ok",
  "version": "0.1.0",
  "database": "ok",
  "queue": "not_configured",
  "time": "2026-11-26T15:04:05Z"
}
```

Si `DATABASE_URL` está vacía, `database` se omite. Si la base no responde, `/health` devuelve `200` con `status: degraded` y `database: down` para que el monitoreo lo distinga. El contrato `Health` solo admite `ok` o `down` en ese campo (no existe `not_configured` para la base).

## Base de datos

El backend usa **SQLAlchemy 2.0** con el driver **psycopg 3** y migraciones con **Alembic**. La URL se lee de `DATABASE_URL`; en producción debe incluir `sslmode=require`.

### Opción A: Neon (recomendada)

1. Crear un proyecto en [neon.tech](https://neon.tech).
2. Copiar la cadena de conexión del panel (incluye `sslmode=require`).
3. Adaptarla al driver de Lumi:

   ```text
   postgresql+psycopg://USUARIO:CONTRASENA@HOST/BD?sslmode=require
   ```

4. Pegarla en `backend/.env`.

> Para Alembic conviene la cadena **directa** de Neon (el host sin `-pooler`). Si se usa la cadena agrupada, añade `prepare_threshold=0` a la URL para evitar problemas de sentencias preparadas con PgBouncer.

### Opción B: PostgreSQL local en Windows

Con el servicio de PostgreSQL 17 instalado:

```powershell
# Crear una base dedicada al desarrollo
& "C:\Program Files\PostgreSQL\17\bin\createdb.exe" -U postgres lumi_dev
```

```text
DATABASE_URL=postgresql+psycopg://postgres:TU_CLAVE@localhost:5432/lumi_dev
```

### Opción C: Docker (entorno completo)

Con Docker Desktop arrancado, desde `backend/`:

```powershell
docker compose up --build
```

Levanta PostgreSQL 17 en `localhost:5433` (usuario y base `lumi`, contraseña `lumi`) y el API en `http://localhost:8000`. Se usa el puerto 5433 para no chocar con un PostgreSQL instalado en Windows. Al arrancar, el contenedor del API aplica `alembic upgrade head`. La base de pruebas `lumi_test` se crea sola en el primer arranque.

```powershell
# Cargar la demo (idempotente)
docker compose exec api python scripts/seed_demo.py --password "LumiDemo2026!"

# Pruebas de integración contra la base dedicada
docker compose exec -e TEST_DATABASE_URL="postgresql+psycopg://lumi:lumi@db:5432/lumi_test" api python -m pytest

# Detener sin borrar datos, o borrarlo todo
docker compose down
docker compose down -v
```

La contraseña `lumi` y la del usuario demo son solo para el entorno local; nunca se usan en Neon ni en Render.

### Migraciones

```powershell
cd backend
alembic upgrade head          # aplica todo el esquema desde cero
alembic current               # revisión aplicada
alembic check                 # detecta si los modelos y la base difieren
alembic downgrade base        # borra el esquema (solo desarrollo)
alembic revision --autogenerate -m "describe el cambio"
```

La migración inicial está en `alembic/versions/8eb960bc77f1_esquema_inicial.py`. Crea las tablas de autenticación, perfil, núcleo académico, tienda y logros. El correo de `users` es único sin distinguir mayúsculas mediante el índice `uq_users_email_lower`.

### Modelo de datos

Diagrama de las tablas de la Fase 2. Las tablas de sesiones de estudio, monedero, materiales, cuestionarios y notificaciones se agregan en las fases 5, 6 y 7 con sus propias migraciones.

```mermaid
erDiagram
    users ||--o{ refresh_tokens : "emite"
    users ||--o{ password_resets : "solicita"
    users ||--|| user_preferences : "configura"
    users ||--o{ semesters : "organiza"
    users ||--o{ subjects : "cursa"
    users ||--o{ tasks : "crea"
    users ||--o{ user_achievements : "desbloquea"
    semesters ||--o{ subjects : "agrupa"
    subjects ||--o{ tasks : "clasifica"
    tasks ||--o{ subtasks : "contiene"
    achievements ||--o{ user_achievements : "se obtiene"

    users {
        uuid id PK
        varchar email UK
        varchar password_hash
        varchar auth_provider
        varchar locale
        varchar time_zone
        integer version
        timestamptz deleted_at
    }
    refresh_tokens {
        uuid id PK
        uuid user_id FK
        varchar token_hash UK
        timestamptz expires_at
        timestamptz revoked_at
        uuid replaced_by_id FK
    }
    password_resets {
        uuid id PK
        uuid user_id FK
        varchar token_hash UK
        timestamptz expires_at
        timestamptz used_at
    }
    user_preferences {
        uuid user_id PK
        json selected_goals
        integer daily_goal_minutes
        varchar locale
        boolean notifications_enabled
        integer version
    }
    semesters {
        uuid id PK
        uuid user_id FK
        varchar name
        date start_date
        date end_date
        integer version
        timestamptz deleted_at
    }
    subjects {
        uuid id PK
        uuid user_id FK
        uuid semester_id FK
        varchar name
        varchar color
        varchar status
        integer version
        timestamptz deleted_at
    }
    tasks {
        uuid id PK
        uuid user_id FK
        uuid subject_id FK
        varchar title
        date due_date
        time due_time
        boolean all_day
        varchar priority
        varchar status
        timestamptz completed_at
        timestamptz rewarded_at
        integer version
        timestamptz deleted_at
    }
    subtasks {
        uuid id PK
        uuid task_id FK
        varchar title
        boolean completed
        integer version
        timestamptz deleted_at
    }
    achievements {
        uuid id PK
        varchar code UK
        varchar title
        text description
        integer target
    }
    user_achievements {
        uuid user_id PK
        uuid achievement_id PK
        timestamptz unlocked_at
    }
    store_items {
        uuid id PK
        varchar code UK
        varchar name
        integer price_gems
        varchar type
        json effects
        boolean active
    }
```

Reglas comunes:

- `id` es UUID generado por el servidor; la semilla usa UUID v5 estables.
- `version` sube en cada cambio y alimenta el `ETag`/`If-Match` de la Fase 4.
- `deleted_at` implementa el borrado lógico; las lecturas sincronizables pueden devolver filas borradas.
- `created_at` y `updated_at` son `timestamptz` con `now()` de PostgreSQL.
- Los enums se guardan como `VARCHAR + CHECK` para que las migraciones sean portables.

### Datos de demostración

```powershell
python scripts/seed_demo.py
# o con una contraseña conocida:
python scripts/seed_demo.py --password "MiClaveSegura"
```

El script es idempotente y usa UUID estables. Crea el usuario `demo@lumi.app`, dos semestres, cuatro materias, ocho tareas con subtareas, cuatro artículos de tienda y nueve logros. Si el usuario no existe, genera una contraseña y la imprime **una sola vez**; no se guarda en el repositorio. También acepta `SEED_DEMO_PASSWORD` como variable de entorno.

## Pruebas y calidad

```powershell
cd backend
pytest                          # unitarias; las de integración se omiten sin TEST_DATABASE_URL
ruff check .
ruff format --check .
```

### Pruebas de integración con PostgreSQL

Usan `TEST_DATABASE_URL` y **borran todo el esquema** al empezar. Nunca apuntes esa variable a la base de desarrollo o producción.

```powershell
& "C:\Program Files\PostgreSQL\17\bin\createdb.exe" -U postgres lumi_test
$env:TEST_DATABASE_URL = "postgresql+psycopg://postgres:TU_CLAVE@localhost:5432/lumi_test"
pytest tests/integration
```

El workflow `.github/workflows/backend-ci.yml` levanta un contenedor `postgres:17`, define `TEST_DATABASE_URL` y ejecuta toda la suite con Python 3.12.

### Comparación con el contrato

```powershell
python scripts/diff_openapi.py          # informa diferencias y sale con 0
python scripts/diff_openapi.py --strict # sale con 1 si hay diferencias
```

Compara rutas, métodos y `operationId`; los esquemas se comparan en las fases 4 y 8. Al cerrar la Fase 2 informa 72 operaciones en el contrato y 1 implementada (`GET /health`).

## Estructura actual

```text
backend/
├── README.md
├── .env.example
├── alembic.ini
├── alembic/
│   ├── env.py                  # lee DATABASE_URL y registra los modelos
│   └── versions/               # migración inicial
├── docs/
│   └── modelo-datos.md         # diagrama ER en Mermaid
├── pyproject.toml              # configuración de ruff y pytest
├── requirements.txt
├── requirements-dev.txt
├── scripts/
│   ├── diff_openapi.py
│   ├── seed_demo.py
│   └── warmup.ps1
├── tests/
│   ├── unit/                   # salud, errores, request id, idioma, seguridad, semilla
│   └── integration/            # migraciones, semilla y health con PostgreSQL
└── app/
    ├── __init__.py             # versión del API
    ├── models.py               # registro central de entidades para Alembic
    ├── main.py                 # app, middlewares, routers, lifespan
    ├── core/
    │   ├── config.py           # variables de entorno con pydantic-settings
    │   ├── database.py         # motor, sesiones, Base y chequeo de salud
    │   ├── errors.py           # ApiError y formato { code, message, details?, requestId }
    │   ├── i18n.py             # mensajes es/en y negociación de Accept-Language
    │   ├── logging.py          # logs con request_id
    │   ├── request_id.py       # middleware X-Request-Id
    │   └── security.py         # argon2id
    ├── shared/                 # paginación, ETag, fechas UTC (fases posteriores)
    └── modules/
        ├── academics/          # semestres y materias
        ├── achievements/       # catálogo de logros
        ├── auth/               # usuarios, refresh tokens, resets
        ├── store/              # catálogo de la tienda
        ├── system/             # controllers y schemas de /health
        ├── tasks/              # tareas y subtareas
        └── users/              # preferencias
```

Los controladores llaman a servicios, los servicios a repositorios y los repositorios a entidades: **Controllers → Services → Repositories → Entities**. Las integraciones externas se llaman solo desde servicios.

## Convenciones implementadas

- **Errores:** siempre `{ code, message, details?, requestId }`. `message` se traduce según `Accept-Language` (`es` por defecto, `en` disponible). Las excepciones no controladas se convierten en `INTERNAL_ERROR` sin filtrar trazas.
- **`X-Request-Id`:** se reutiliza si llega un UUID válido; si no, se genera uno. Viaja en la respuesta y en los logs de cada petición.
- **CORS:** se configura con `CORS_ORIGINS` (lista separada por comas) y expone `X-Request-Id` y `ETag`.
- **Middleware:** CORS queda en el exterior para que los errores también lleven sus encabezados, y el manejador de errores va dentro del de `X-Request-Id`.
- **Swagger:** es *code-first*; la fuente de verdad sigue siendo `api/openapi.yaml`.
- **Base de datos:** borrado lógico con `deleted_at`, `version` para `ETag`/`If-Match`, enums como `VARCHAR + CHECK` y UUID como clave primaria.

## Variables de entorno

El archivo real `backend/.env` está ignorado por Git. Se versiona `.env.example` con marcadores, sin secretos.

| Variable | Fase | Uso |
|---|---|---|
| `APP_ENV` | 1 | Entorno (`development`, `production`). |
| `APP_BASE_URL` | 1 | URL pública del API. |
| `CORS_ORIGINS` | 1 | Orígenes permitidos, separados por coma. |
| `DATABASE_URL` | 2 | PostgreSQL con `postgresql+psycopg://` y `sslmode=require` en producción. |
| `MONGODB_URI`, `MONGODB_DB` | 6 | MongoDB Atlas para contenido generado. |
| `RABBITMQ_URL`, `WORKER_ENABLED` | 6 | Cola de procesamiento de materiales. |
| `JWT_SECRET`, `JWT_ACCESS_MINUTES`, `JWT_REFRESH_DAYS` | 3 | Autenticación. |
| `GOOGLE_WEB_CLIENT_ID` | 3 | Verificación de ID tokens de Google. |
| `GEMINI_API_KEY`, `GEMINI_MODEL`, `AI_DAILY_LIMIT_PER_USER` | 6 | Generación con IA. |
| `REMINDER_CRON_TOKEN`, `FCM_SERVICE_ACCOUNT_JSON_PATH` | 7 | Recordatorios opcionales. |

En Render, estas variables se configuran en el panel del servicio, nunca en el repositorio.

## Despliegue en Render

**Opción rápida (blueprint):** en Render, `New > Blueprint`, conectar el repositorio y aplicar el `render.yaml` de la raíz. Crea el servicio `lumi-api` desde `develop`, con el build, el arranque, el health check y el auto deploy ya configurados. Solo hay que completar `DATABASE_URL` con la cadena de Neon.

**Opción manual:**

1. Crear un **Web Service** conectado al repositorio de GitHub.
2. **Root Directory:** `backend` (el repositorio también contiene la app Android).
3. Runtime: **Python 3.12** (`PYTHON_VERSION=3.12.10` en las variables de entorno).
4. Build command:

   ```text
   pip install -r requirements.txt && alembic upgrade head
   ```

5. Start command:

   ```text
   uvicorn app.main:app --host 0.0.0.0 --port $PORT
   ```

6. Health check path: `/api/v1/health`.
7. Variables del panel: `APP_ENV=production`, `APP_BASE_URL=https://<servicio>.onrender.com`, `CORS_ORIGINS=https://<servicio>.onrender.com`, `DATABASE_URL` de Neon, `PYTHON_VERSION=3.12.10` y, en su fase, las claves restantes.
8. Auto deploy: **desde `develop` mientras el proyecto está en desarrollo**. El plan original decía `main`, pero la regla de Git acordada mantiene `main` intacta hasta la entrega final; en la Fase 8 se cambia a `main`.

### Calentar el servicio

El plan gratuito de Render duerme el servicio tras un periodo de inactividad. Antes de una demo:

```powershell
cd backend
.\scripts\warmup.ps1 -BaseUrl "https://<servicio>.onrender.com"
```

El script reintenta `GET /api/v1/health`, muestra el tiempo de respuesta y el JSON de estado.

## Solución de problemas

- **`python` abre otra versión:** usa `py -3.12 -m venv .venv`.
- **`Activate.ps1` bloqueado:** ejecuta `Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass`.
- **`alembic` no encuentra la base:** revisa `DATABASE_URL` en `backend/.env` y ejecuta desde `backend/`.
- **`alembic upgrade head` falla con `sslmode`:** la URL de Neon debe conservar `?sslmode=require`.
- **La semilla dice que faltan tablas:** ejecuta antes `alembic upgrade head`.
- **`/health` marca `database: down`:** prueba la conexión con `psql` o con `alembic current`; revisa que Neon no esté pausado.
- **`/docs` no carga:** confirma que `uvicorn` se lanzó desde `backend/`, no desde la raíz.
- **Swagger sin `/api/v1`:** las rutas se montan con el prefijo; si no aparece, revisa `app.include_router(..., prefix=API_PREFIX)` en `app/main.py`.

## Documentación relacionada

- `README.md` (raíz): visión general del repositorio y arranque de la app Android.
- `AGENTS.md`: comandos y convenciones para agentes de IA.
- `PLAN_EJECUTOR_LUMI.md`: fases, criterios de aceptación y estado del proyecto.
- `docs/ia/`: bitácora de uso de IA (excepción versionada).
