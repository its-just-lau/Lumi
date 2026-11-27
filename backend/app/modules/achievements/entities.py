"""Entidades del catálogo de logros y su desbloqueo por usuario."""

from __future__ import annotations

import uuid
from datetime import datetime

from sqlalchemy import DateTime, ForeignKey, Integer, String, Text
from sqlalchemy import Uuid as SAUuid
from sqlalchemy.orm import Mapped, mapped_column

from app.core.database import Base, TimestampMixin


class Achievement(Base, TimestampMixin):
    """Logro del catálogo global.

    `code` coincide con los códigos acordados con el equipo Android y `target`
    es la meta que se usa en `progress.target` al devolver el catálogo.
    """

    __tablename__ = "achievements"

    id: Mapped[uuid.UUID] = mapped_column(SAUuid, primary_key=True, default=uuid.uuid4)
    code: Mapped[str] = mapped_column(String(60), nullable=False, unique=True)
    title: Mapped[str] = mapped_column(String(120), nullable=False)
    description: Mapped[str] = mapped_column(Text, nullable=False)
    target: Mapped[int] = mapped_column(Integer, nullable=False, default=1, server_default="1")


class UserAchievement(Base):
    """Logro desbloqueado por un usuario."""

    __tablename__ = "user_achievements"

    user_id: Mapped[uuid.UUID] = mapped_column(
        SAUuid, ForeignKey("users.id", ondelete="CASCADE"), primary_key=True
    )
    achievement_id: Mapped[uuid.UUID] = mapped_column(
        SAUuid,
        ForeignKey("achievements.id", ondelete="CASCADE"),
        primary_key=True,
    )
    unlocked_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)
