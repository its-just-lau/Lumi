# Bitácora de uso de IA

Registro exigido por la rúbrica y por la sección 10 del plan. Cada entrada documenta fecha, herramienta, instrucción, resultado, corrección manual, errores detectados y revisor. Esta carpeta es la excepción versionada de `.gitignore`.

## 2026-11-25 · Fase 1: esqueleto del API, Swagger y CI

- **Herramienta:** OpenCode con el modelo DeepSeek V4.1 Flash Free (agente de código).
- **Instrucción (resumen):** «Arranca la Fase 1» del plan: crear `backend/` con FastAPI, `/health`, Swagger, formato de error, `X-Request-Id`, `Accept-Language`, CORS, pruebas y CI, usando Python 3.12.
- **Resultado:** se creó el esqueleto completo en `backend/` (12 tareas F1-01 a F1-12), con 19 pruebas unitarias, configuración de ruff y pytest, workflow `.github/workflows/backend-ci.yml` y el esqueleto de `scripts/diff_openapi.py`.
- **Corrección manual:** las cifras y nombres del contrato se verificaron contra `api/openapi.yaml` antes de escribir los esquemas; el agente no ejecutó commits, push ni Pull Requests (protocolo de Git del plan).
- **Error detectado:** el plan (F1-05) pedía responder `api: ok` y `database: not_configured`. Al comparar con el esquema `Health` del contrato, se vio que no existe el campo `api` y que `database` solo admite `ok` o `down`. El agente había planteado copiar el plan tal cual, lo que habría producido una respuesta inválida frente al contrato. Se detectó al revisar `openapi.yaml` y se corrigió devolviendo `status: ok`, `version`, `queue: not_configured` y `time`, omitiendo `database` hasta la Fase 2. La decisión quedó documentada en `backend/README.md` y en una prueba unitaria.
- **Revisado por:** pendiente (Maik).
- **Commit o PR relacionado:** pendiente de la parada de Git de la Fase 1.

### Notas adicionales

- La verificación local se hizo con Python 3.13 porque la máquina no tenía 3.12 instalado; el proyecto queda fijado a 3.12 en CI y en la documentación, y el usuario debe crear el `.venv` con 3.12.
- El `TestClient` de Starlette emite una advertencia de obsolescencia con `httpx` clásico; se adoptó `httpx2`, el cliente que la propia librería recomienda, y las pruebas quedan sin advertencias.
