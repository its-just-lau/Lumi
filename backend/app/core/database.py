"""Motor, sesiones y base declarativa de SQLAlchemy."""

from __future__ import annotations

import logging
import uuid
from collections.abc import Iterator
from datetime import datetime
from enum import StrEnum

from sqlalchemy import DateTime, Integer, MetaData, create_engine, func, text
from sqlalchemy import Enum as SAEnum
from sqlalchemy.engine import Engine
from sqlalchemy.exc import SQLAlchemyError
from sqlalchemy.orm import DeclarativeBase, Mapped, Session, mapped_column, sessionmaker

from app.core.config import get_settings

LOGGER = logging.getLogger("lumi.database")

# Nombres deterministas para que Alembic detecte cambios y las migraciones sean
# reproducibles entre entornos.
NAMING_CONVENTION = {
    "ix": "ix_%(column_0_label)s",
    "uq": "uq_%(table_name)s_%(column_0_name)s",
    "ck": "ck_%(table_name)s_%(constraint_name)s",
    "fk": "fk_%(table_name)s_%(column_0_name)s_%(referred_table_name)s",
    "pk": "pk_%(table_name)s",
}


class Base(DeclarativeBase):
    """Base declarativa común a todas las entidades."""

    metadata = MetaData(naming_convention=NAMING_CONVENTION)


class TimestampMixin:
    """Fechas de creación y actualización gestionadas por la base de datos."""

    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), nullable=False
    )
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        server_default=func.now(),
        onupdate=func.now(),
        nullable=False,
    )


class VersionMixin:
    """Versión lógica que sube en cada cambio (para `If-Match` y `ETag`)."""

    version: Mapped[int] = mapped_column(Integer, nullable=False, default=1, server_default="1")


class SoftDeleteMixin:
    """Borrado lógico: la fila se conserva con `deleted_at`."""

    deleted_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))


def enum_type(enum_class: type[StrEnum], name: str, length: int = 30) -> SAEnum:
    """Enumera en `VARCHAR + CHECK` para que las migraciones sean portables.

    Se guarda el valor (`local`, `completed`...) y no el nombre del miembro, que es
    lo que expone el contrato.
    """
    return SAEnum(
        enum_class,
        name=name,
        native_enum=False,
        length=length,
        create_constraint=True,
        values_callable=lambda cls: [member.value for member in cls],
    )


_engine: Engine | None = None
_session_factory: sessionmaker[Session] | None = None

# Marcadores de `.env.example`: mientras sigan en la URL, la base se considera
# no configurada y `/health` no intenta conectarse a un host inexistente.
_PLACEHOLDERS = ("://USUARIO:", "@HOST/", "CONTRASEÑA", "CONTRASENA")


def is_database_configured() -> bool:
    """Indica si hay una `DATABASE_URL` real en el entorno."""
    url = get_settings().database_url
    return bool(url) and not any(token in url for token in _PLACEHOLDERS)


def get_engine() -> Engine:
    """Crea el motor una sola vez por proceso."""
    global _engine
    if _engine is None:
        settings = get_settings()
        url = settings.database_url
        if not is_database_configured():
            raise RuntimeError(
                "DATABASE_URL no está configurada o sigue con los marcadores de .env.example"
            )
        if settings.app_env == "production" and not any(
            mode in url for mode in ("sslmode=require", "sslmode=verify-ca", "sslmode=verify-full")
        ):
            raise RuntimeError(
                "DATABASE_URL debe exigir SSL en producción "
                "(sslmode=require, verify-ca o verify-full)"
            )
        _engine = create_engine(
            url,
            pool_pre_ping=True,
            pool_size=5,
            max_overflow=5,
            pool_recycle=1800,
        )
    return _engine


def get_session_factory() -> sessionmaker[Session]:
    """Fábrica de sesiones ligada al motor actual."""
    global _session_factory
    if _session_factory is None:
        _session_factory = sessionmaker(bind=get_engine(), expire_on_commit=False, autoflush=False)
    return _session_factory


def get_session() -> Iterator[Session]:
    """Dependencia de FastAPI: abre una sesión y la cierra al terminar."""
    with get_session_factory()() as session:
        yield session


def reset_engine() -> None:
    """Libera el motor y la fábrica (pruebas y cambios de configuración)."""
    global _engine, _session_factory
    if _engine is not None:
        _engine.dispose()
    _engine = None
    _session_factory = None


def check_database() -> bool:
    """Comprueba la conexión con un `SELECT 1`."""
    if not is_database_configured():
        return False
    try:
        with get_engine().connect() as connection:
            connection.execute(text("SELECT 1"))
    except SQLAlchemyError:
        LOGGER.exception("No se pudo consultar la base de datos")
        return False
    return True


def new_uuid() -> uuid.UUID:
    """Identificador por defecto de las entidades."""
    return uuid.uuid4()
