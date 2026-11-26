"""Esquemas del módulo System."""

from __future__ import annotations

from datetime import datetime
from typing import Literal

from pydantic import BaseModel, Field


class HealthResponse(BaseModel):
    """Estado del servicio. Corresponde al esquema `Health` de `api/openapi.yaml`."""

    status: Literal["ok", "degraded", "down"] = Field(description="Estado general del servicio.")
    version: str | None = Field(default=None, description="Versión del API.")
    database: Literal["ok", "down"] | None = Field(
        default=None, description="Estado de PostgreSQL. Nulo mientras no esté configurada."
    )
    queue: Literal["ok", "down", "not_configured"] | None = Field(
        default=None, description="Estado de la cola de materiales."
    )
    time: datetime | None = Field(default=None, description="Momento de la consulta en UTC.")
