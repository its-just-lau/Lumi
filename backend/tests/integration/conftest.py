"""Fixtures de las pruebas de integración con PostgreSQL.

Estas pruebas solo corren si `TEST_DATABASE_URL` apunta a una base **dedicada a
pruebas**. El fixture borra y recrea todo el esquema al empezar, así que nunca
debe apuntar a la base de desarrollo o producción.

En local se puede usar la instancia de PostgreSQL instalada en Windows o una
base de Neon exclusiva para pruebas. En GitHub Actions el workflow de backend
levanta un contenedor `postgres:17` y define esta variable.
"""

from __future__ import annotations

import os
from pathlib import Path

import pytest
from alembic import command
from alembic.config import Config

from app.core.config import get_settings
from app.core.database import reset_engine

BACKEND_DIR = Path(__file__).resolve().parents[2]


@pytest.fixture(scope="session")
def database_url() -> str:
    """URL de la base de pruebas; omite el módulo si no está definida."""
    url = os.getenv("TEST_DATABASE_URL")
    if not url:
        pytest.skip(
            "TEST_DATABASE_URL no está configurada: se omiten las pruebas de "
            "integración con PostgreSQL."
        )
    return url


@pytest.fixture(scope="session", autouse=True)
def esquema_migrado(database_url: str) -> str:
    """Recrea el esquema con Alembic antes de las pruebas de integración."""
    os.environ["DATABASE_URL"] = database_url
    get_settings.cache_clear()
    reset_engine()

    config = Config(str(BACKEND_DIR / "alembic.ini"))
    command.downgrade(config, "base")
    command.upgrade(config, "head")

    yield database_url

    os.environ.pop("DATABASE_URL", None)
    get_settings.cache_clear()
    reset_engine()
