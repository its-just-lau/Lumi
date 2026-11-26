"""Rutas del módulo System."""

from __future__ import annotations

from datetime import UTC, datetime

from fastapi import APIRouter

from app import __version__
from app.modules.system.schemas import HealthResponse

router = APIRouter(tags=["System"])


@router.get(
    "/health",
    response_model=HealthResponse,
    response_model_exclude_none=True,
    summary="Estado del servicio",
    description=(
        "Sin autenticación. Sirve para monitoreo y para calentar el servicio antes de "
        "una demo. PostgreSQL y la cola se reportan al configurarse en las fases 2 y 6."
    ),
    operation_id="getHealth",
)
async def get_health() -> HealthResponse:
    """Devuelve el estado del API.

    La base de datos se omite mientras no esté configurada (Fase 2) porque el
    contrato solo admite `ok` o `down` para ese campo. La cola sí admite
    `not_configured`.
    """
    return HealthResponse(
        status="ok",
        version=__version__,
        queue="not_configured",
        time=datetime.now(UTC),
    )
