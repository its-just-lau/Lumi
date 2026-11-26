# Backend de Lumi

API REST del proyecto Lumi. Implementa el contrato `api/openapi.yaml` (v1.4.0) bajo el prefijo `/api/v1`. Esta guía se actualiza en cada fase del plan (`PLAN_EJECUTOR_LUMI.md`).

> **Estado actual: Fase 1.** El esqueleto de FastAPI ya funciona: `GET /api/v1/health`, Swagger en `/docs`, formato de error del contrato, `X-Request-Id`, `Accept-Language`, CORS y CI. La base de datos, la cola, la autenticación y la IA llegan en las fases 2 a 7.

## Requisitos

- Python 3.12 (local, CI y Render usan la misma versión).
- PowerShell en Windows.
- Git.

## Arranque en Windows (PowerShell)

```powershell
# Desde la raíz del repositorio
cd backend
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt -r requirements-dev.txt

# Copiar variables de entorno (nunca subir el .env real)
Copy-Item .env.example .env

# Levantar el servidor de desarrollo
uvicorn app.main:app --reload
```

Si `python` no apunta a 3.12, usa `py -3.12 -m venv .venv`.

Luego abre:

- Swagger: `http://localhost:8000/docs`
- Especificación generada: `http://localhost:8000/openapi.json`
- Salud: `http://localhost:8000/api/v1/health`

Respuesta esperada de `/api/v1/health` en la Fase 1:

```json
{
  "status": "ok",
  "version": "0.1.0",
  "queue": "not_configured",
  "time": "2026-11-25T15:04:05Z"
}
```

`database` se omite a propósito: el esquema `Health` del contrato solo admite `ok` o `down` para ese campo, y en la Fase 1 todavía no hay base de datos. Se añadirá en la Fase 2.

## Pruebas y calidad

```powershell
cd backend
pytest
ruff check .
ruff format --check .
```

- Las pruebas usan `pytest` y el `TestClient` de Starlette (con `httpx2`, que es el cliente que Starlette recomienda ahora).
- `ruff` aplica lint y formato con la configuración de `pyproject.toml`.
- El workflow `.github/workflows/backend-ci.yml` ejecuta `ruff check`, `ruff format --check` y `pytest` en cada push y en cada Pull Request, con Python 3.12.

### Comparación con el contrato

```powershell
python scripts/diff_openapi.py          # informa diferencias y sale con 0
python scripts/diff_openapi.py --strict # sale con 1 si hay diferencias
```

En la Fase 1 compara rutas, métodos y `operationId`; la comparación de esquemas se completa en las fases 4 y 8. Al terminar la Fase 1 informa 72 operaciones en el contrato y 1 implementada (`GET /health`).

## Estructura actual

```text
backend/
├── README.md
├── .env.example
├── pyproject.toml              # configuración de ruff y pytest
├── requirements.txt
├── requirements-dev.txt
├── scripts/
│   └── diff_openapi.py
├── tests/
│   └── unit/                   # health, errores, X-Request-Id, Accept-Language
└── app/
    ├── __init__.py             # versión del API
    ├── main.py                 # app, middlewares, routers, lifespan
    ├── core/
    │   ├── config.py           # variables de entorno con pydantic-settings
    │   ├── errors.py           # ApiError y formato { code, message, details?, requestId }
    │   ├── i18n.py             # mensajes es/en y negociación de Accept-Language
    │   ├── logging.py          # logs con request_id
    │   └── request_id.py       # middleware X-Request-Id
    ├── shared/                 # paginación, ETag, fechas UTC (fases posteriores)
    └── modules/
        └── system/             # controllers y schemas de /health
```

Cuando existan módulos de dominio se añadirán `services.py`, `repositories.py` y `entities.py`, respetando **Controllers → Services → Repositories → Entities**. Las integraciones externas se llaman solo desde Services.

## Convenciones implementadas

- **Errores:** siempre `{ code, message, details?, requestId }`. `message` se traduce según `Accept-Language` (`es` por defecto, `en` disponible). Las excepciones no controladas se convierten en `INTERNAL_ERROR` sin filtrar trazas.
- **`X-Request-Id`:** se reutiliza si llega un UUID válido; si no, se genera uno. Viaja en la respuesta y en los logs de cada petición.
- **CORS:** se configura con `CORS_ORIGINS` (lista separada por comas) y expone `X-Request-Id` y `ETag`.
- **Middleware:** CORS queda en el exterior para que los errores también lleven sus encabezados, y el manejador de errores va dentro del de `X-Request-Id`.
- **Swagger:** es *code-first*; la fuente de verdad sigue siendo `api/openapi.yaml`.

## Variables de entorno

El archivo real `backend/.env` está ignorado por Git. Se versiona `.env.example` con marcadores, sin secretos. En la Fase 1 solo se leen estas:

| Variable | Uso |
|---|---|
| `APP_ENV` | Entorno (`development`, `production`). |
| `APP_BASE_URL` | URL pública del API. |
| `CORS_ORIGINS` | Orígenes permitidos, separados por coma. |

El resto de variables de `.env.example` (base de datos, cola, JWT, Google, Gemini, recordatorios, FCM) se activan en las fases 2, 3, 6 y 7.

## Despliegue

- El API se despliega en Render desde la rama `main`.
- Las variables de entorno se configuran en el panel de Render, no en el repositorio.
- El despliegue automático se condiciona a que el CI esté en verde (Fase 8).

## Solución de problemas

- **`python` abre otra versión:** usa `py -3.12 -m venv .venv`.
- **`Activate.ps1` bloqueado:** ejecuta `Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass`.
- **`/docs` no carga:** confirma que `uvicorn` se lanzó desde `backend/`, no desde la raíz.
- **Swagger sin `/api/v1`:** las rutas se montan con el prefijo; si no aparece, revisa `app.include_router(..., prefix=API_PREFIX)` en `app/main.py`.

## Documentación relacionada

- `README.md` (raíz): visión general del repositorio y arranque de la app Android.
- `AGENTS.md`: comandos y convenciones para agentes de IA.
- `PLAN_EJECUTOR_LUMI.md`: fases, criterios de aceptación y estado del proyecto.
- `docs/ia/`: bitácora de uso de IA (excepción versionada).
