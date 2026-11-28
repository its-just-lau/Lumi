# Bitácora de uso de IA

Registro exigido por la rúbrica y por la sección 10 del plan. Cada entrada documenta fecha, herramienta, instrucción, resultado, corrección manual, errores detectados y revisor. Esta carpeta es la excepción versionada de `.gitignore`.

## 2026-11-26 · Fase 2: base de datos y primer despliegue

- **Herramienta:** OpenCode con el modelo DeepSeek V4.1 Flash Free (agente de código).
- **Instrucción (resumen):** aplicar las respuestas del equipo a las decisiones pendientes de la Fase 0 y ejecutar la Fase 2 del plan: PostgreSQL con SQLAlchemy y Alembic, entidades, migración inicial, semilla, `/health` real, pruebas de integración, Render, warm-up, diagrama ER y documentación.
- **Resultado:** se añadieron `app/core/database.py`, `app/core/security.py` y las entidades de auth, usuarios, académico, tareas, tienda y logros; la migración inicial `8eb960bc77f1`; `scripts/seed_demo.py`; `scripts/warmup.ps1`; el diagrama ER dentro de `backend/README.md`; pruebas unitarias y de integración; y el servicio PostgreSQL efímero en el CI. El código y las migraciones se verificaron contra PostgreSQL 17. El 2026-11-27 se añadió un entorno local con Docker Compose (PostgreSQL 17 + API + base `lumi_test`) y se revisó el esquema de referencia del equipo en `reportes/DB/`.
- **Corrección manual:** el usuario instaló Python 3.12, dejó `GEMINI_API_KEY` y `GOOGLE_WEB_CLIENT_ID` en `.env` y aportó las decisiones del equipo en `reportes/respuestas del equipo fase 0.md`. El agente no ejecutó commits, push, merges ni Pull Requests.
- **Errores detectados:**
  1. **Semilla con violación de clave foránea.** La primera versión insertaba semestres, materias y subtareas sin `flush` intermedio. Al no haber relaciones ORM entre módulos, SQLAlchemy no conocía el orden y PostgreSQL rechazó las filas hijas. Se detectó al ejecutar la semilla real y se corrigió con `session.flush()` después del usuario, los semestres, las materias y cada tarea.
  2. **Migración generada contra SQLite.** Al autogenerar la revisión con una base SQLite temporal, los valores por defecto salieron como `CURRENT_TIMESTAMP` y con paréntesis. Se reescribió la migración con `now()` y los `DEFAULT` de PostgreSQL, y se comprobó con `downgrade base`, `upgrade head` y `alembic check` que no hay diferencias.
  3. **`database: error` del plan no existe en el contrato.** F2-05 pedía reportar `error`, pero el enum `Health.database` solo admite `ok` o `down`. Se implementó `down` + `status: degraded` y se documentó la desviación.
  4. **Auto deploy desde `main`.** El plan original lo pedía, pero la regla de Git acordada mantiene `main` intacta hasta la entrega final. Se documentó que Render despliega desde `develop` y que se cambiará a `main` en la Fase 8.
  5. **Correo único sensible a mayúsculas.** La primera versión de `users.email` usaba `UNIQUE` directo, así que `Alumno@Lumi.mx` y `alumno@lumi.mx` podían coexistir. Se detectó al contrastar con la guía de base de datos del equipo y se corrigió con el índice único `uq_users_email_lower` sobre `lower(email)`; se verificó insertando un duplicado con otra capitalización.
- **Revisado por:** pendiente (Maik).
- **Commit o PR relacionado:** pendiente de la parada de Git de la Fase 2.

### Notas adicionales

- Se siguió la regla del contrato: las adiciones acordadas (`completedLessons`, esquemas de progreso, logros) se implementarán en sus fases y se formalizarán en la v1.5.0 de `api/openapi.yaml`.
- La verificación de integración se hizo con una instancia temporal de PostgreSQL 17 en el equipo local, con el contenedor `postgres:17` que el CI levanta en cada ejecución y con el entorno `docker compose` del repositorio.
- El esquema de referencia del equipo (`reportes/DB/`) usa PostgreSQL 16, `schema_migrations` propio y un servidor Node/TypeScript. Lumi mantiene PostgreSQL 17, Alembic y la separación del contrato: las tablas que faltan respecto a esa guía se crean en las fases 4, 5 y 6, no en la migración inicial.
- La contraseña del usuario demo nunca se escribe en el repositorio: la semilla la genera y la imprime una sola vez, o se pasa con `--password`/`SEED_DEMO_PASSWORD`.
