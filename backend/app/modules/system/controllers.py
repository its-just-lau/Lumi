"""Rutas del módulo System."""

from __future__ import annotations

from datetime import UTC, datetime

from fastapi import APIRouter

from app import __version__
from app.core.database import check_database, is_database_configured
from app.modules.system.schemas import HealthResponse

router = APIRouter(tags=["System"])


@router.get(
    "/health",
    response_model=HealthResponse,
    response_model_exclude_none=True,
    summary="Estado del servicio",
    description=(
        "Sin autenticación. Sirve para monitoreo y para calentar el servicio antes de "
        "una demo. PostgreSQL se consulta de verdad en la Fase 2; la cola se reporta "
        "al configurarse en la Fase 6."
    ),
    operation_id="getHealth",
)
async def get_health() -> HealthResponse:
    """Devuelve el estado del API y de sus dependencias.

    `database` se omite mientras `DATABASE_URL` no esté configurada, porque el
    esquema `Health` del contrato solo admite `ok` o `down` para ese campo (no
    existe `not_configured`). Cuando la base responde, el estado general sigue
    siendo `ok`; si no responde, se marca `degraded` y `database: down`.
    """
    database: str | None = None
    status: str = "ok"

    if is_database_configured():
        if check_database():
            database = "ok"
        else:
            database = "down"
            status = "degraded"

    return HealthResponse(
        status=status,
        version=__version__,
        database=database,
        queue="not_configured",
        time=datetime.now(UTC),
    )
