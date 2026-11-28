"""Entorno de Alembic para Lumi.

La URL de PostgreSQL se lee de `DATABASE_URL` (backend/.env o variable de
entorno). No se guarda en `alembic.ini` para no versionar credenciales.
"""

from __future__ import annotations

import sys
from logging.config import fileConfig
from pathlib import Path

from alembic import context
from sqlalchemy import create_engine, pool

# Permite ejecutar `alembic` desde backend/ sin instalar el proyecto.
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from app import models  # noqa: F401  (registra todas las tablas)
from app.core.config import get_settings
from app.core.database import Base

config = context.config

if config.config_file_name is not None:
    fileConfig(config.config_file_name, disable_existing_loggers=False)

target_metadata = Base.metadata


def _database_url() -> str:
    """Devuelve la URL configurada o falla con un mensaje claro."""
    url = get_settings().database_url
    if not url:
        raise RuntimeError(
            "DATABASE_URL no está configurada. Copia backend/.env.example a "
            "backend/.env y coloca la cadena de conexión de PostgreSQL."
        )
    return url


def run_migrations_offline() -> None:
    """Genera el SQL sin conectarse a la base (útil para revisar la migración)."""
    context.configure(
        url=_database_url(),
        target_metadata=target_metadata,
        literal_binds=True,
        dialect_opts={"paramstyle": "named"},
        compare_type=True,
    )
    with context.begin_transaction():
        context.run_migrations()


def run_migrations_online() -> None:
    """Aplica las migraciones contra la base configurada."""
    connectable = create_engine(_database_url(), poolclass=pool.NullPool)
    with connectable.connect() as connection:
        context.configure(
            connection=connection,
            target_metadata=target_metadata,
            compare_type=True,
        )
        with context.begin_transaction():
            context.run_migrations()


if context.is_offline_mode():
    run_migrations_offline()
else:
    run_migrations_online()
