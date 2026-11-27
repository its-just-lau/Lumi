"""Entidades del catálogo de la tienda."""

from __future__ import annotations

import uuid
from enum import StrEnum

from sqlalchemy import JSON, Boolean, Integer, String, Text, text
from sqlalchemy import Uuid as SAUuid
from sqlalchemy.orm import Mapped, mapped_column

from app.core.database import Base, TimestampMixin, enum_type


class StoreItemType(StrEnum):
    """Tipos de artículo que admite el contrato."""

    AVATAR = "avatar"
    THEME = "theme"
    BOOST = "boost"
    STREAK_FREEZE = "streak_freeze"
    COSMETIC = "cosmetic"


class StoreItem(Base, TimestampMixin):
    """Artículo del catálogo de la tienda.

    `code` es un identificador estable para que el script de semilla pueda
    actualizar el catálogo sin duplicarlo entre ejecuciones.
    """

    __tablename__ = "store_items"

    id: Mapped[uuid.UUID] = mapped_column(SAUuid, primary_key=True, default=uuid.uuid4)
    code: Mapped[str] = mapped_column(String(60), nullable=False, unique=True)
    name: Mapped[str] = mapped_column(String(120), nullable=False)
    description: Mapped[str | None] = mapped_column(Text)
    price_gems: Mapped[int] = mapped_column(Integer, nullable=False)
    type: Mapped[StoreItemType] = mapped_column(
        enum_type(StoreItemType, "store_item_type", 20), nullable=False
    )
    effects: Mapped[dict[str, object]] = mapped_column(
        JSON, nullable=False, default=dict, server_default=text("'{}'::json")
    )
    active: Mapped[bool] = mapped_column(
        Boolean, nullable=False, default=True, server_default=text("true")
    )
