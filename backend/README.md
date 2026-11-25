# Backend de Lumi

API REST del proyecto Lumi. Este documento es el esqueleto de la guía del backend y se completa en cada fase del plan (`PLAN_EJECUTOR_LUMI.md`).

> **Estado actual: Fase 0.** El backend todavía no tiene código ejecutable. La Fase 1 crea el esqueleto de FastAPI, el endpoint `/health`, Swagger y el workflow de CI. Los comandos de arranque de esta guía se habilitan al terminar esa fase.

## Contrato

- `api/openapi.yaml` (v1.4.0) es la fuente de verdad del contrato REST.
- El API se publica bajo el prefijo `/api/v1`.
- Swagger se mostrará en `/docs` y la especificación generada en `/openapi.json`.
- La app Android usa Prism en `http://localhost:4010/` como mock de desarrollo; el backend debe cumplir el mismo contrato.

## Stack previsto

- Python 3.12 + FastAPI + Pydantic v2.
- SQLAlchemy 2.0 + Alembic sobre PostgreSQL (Neon).
- MongoDB Atlas para contenido generado y archivos (GridFS).
- RabbitMQ (CloudAMQP) para la cola de procesamiento de materiales.
- Gemini mediante la interfaz `AiProvider`.
- pytest + httpx para pruebas; ruff para lint y formato.
- Render para el despliegue del API y GitHub Actions para CI/CD.

Las versiones exactas se fijan en `requirements.txt` y `requirements-dev.txt` durante la Fase 1.

## Estructura prevista

```text
backend/
├── README.md
├── .env.example              # variables sin valores reales
├── requirements.txt
├── requirements-dev.txt
├── alembic.ini
├── migrations/
├── scripts/
│   ├── seed_demo.py
│   ├── diff_openapi.py
│   └── warmup.ps1
├── tests/
│   ├── unit/
│   ├── integration/
│   └── contract/
└── app/
    ├── main.py
    ├── core/
    ├── shared/
    ├── modules/
    ├── integrations/
    └── workers/
```

La arquitectura sigue capas estrictas: **Controllers → Services → Repositories → Entities**, con **Schemas** como DTOs de entrada y salida. Las integraciones externas se llaman solo desde Services.

## Arranque en Windows (PowerShell)

Estos comandos se completan en la Fase 1:

```powershell
# Desde la raíz del repositorio
cd backend
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt -r requirements-dev.txt

# Copiar variables de entorno (nunca subir el .env real)
Copy-Item .env.example .env
# Editar .env con los valores locales

# Levantar el servidor de desarrollo
uvicorn app.main:app --reload
```

Luego abrir:

- Swagger: `http://localhost:8000/docs`
- Especificación: `http://localhost:8000/openapi.json`
- Salud: `http://localhost:8000/api/v1/health`

## Variables de entorno

El archivo real `backend/.env` está ignorado por Git. En la Fase 1 se versiona `backend/.env.example` con marcadores, sin secretos. Variables previstas:

| Variable | Uso |
|---|---|
| `APP_ENV` | Entorno (`development`, `production`). |
| `APP_BASE_URL` | URL pública del API. |
| `CORS_ORIGINS` | Orígenes permitidos, separados por coma. |
| `DATABASE_URL` | Cadena PostgreSQL con SSL (Neon). |
| `MONGODB_URI` / `MONGODB_DB` | Conexión y base documental. |
| `RABBITMQ_URL` | Cadena AMQP del broker. |
| `WORKER_ENABLED` | Activa el consumidor de la cola en el mismo servicio. |
| `JWT_SECRET` | Firma de tokens; se cambia en producción. |
| `JWT_ACCESS_MINUTES` / `JWT_REFRESH_DAYS` | Vigencias de tokens. |
| `GOOGLE_WEB_CLIENT_ID` | ID público de cliente Web para Google Sign-In. |
| `GEMINI_API_KEY` / `GEMINI_MODEL` | Proveedor de IA. |
| `AI_DAILY_LIMIT_PER_USER` | Límite diario de procesamiento por usuario. |
| `REMINDER_CRON_TOKEN` | Protección del endpoint interno de recordatorios (opcional). |
| `FCM_SERVICE_ACCOUNT_JSON_PATH` | Ruta local a la cuenta de servicio de FCM (opcional). |

Nunca se escriben claves reales en el repositorio, en `README.md` ni en `AGENTS.md`.

## Pruebas y calidad

Se habilitan en la Fase 1:

```powershell
cd backend
pytest
ruff check .
ruff format --check .
```

El workflow `.github/workflows/backend-ci.yml` ejecutará lint, pruebas y, más adelante, la prueba de contrato con Schemathesis.

## Despliegue

- El API se despliega en Render desde la rama `main`.
- Las variables de entorno se configuran en el panel de Render, no en el repositorio.
- El despliegue automático se condiciona a que el CI esté en verde (Fase 8).

## Documentación relacionada

- `README.md` (raíz): visión general del repositorio y arranque de la app Android.
- `AGENTS.md`: comandos y convenciones para agentes de IA.
- `PLAN_EJECUTOR_LUMI.md`: fases, criterios de aceptación y estado del proyecto.
- `docs/ia/`: bitácora de uso de IA (excepción versionada).
