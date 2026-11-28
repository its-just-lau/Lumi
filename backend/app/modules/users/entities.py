"""Entidades del perfil y las preferencias del usuario."""

from __future__ import annotations

import uuid

from sqlalchemy import JSON, Boolean, ForeignKey, Integer, String, text
from sqlalchemy import Uuid as SAUuid
from sqlalchemy.orm import Mapped, mapped_column

from app.core.database import Base, TimestampMixin, VersionMixin


class UserPreference(Base, TimestampMixin, VersionMixin):
    __tablename__ = "user_preferences"

    user_id: Mapped[uuid.UUID] = mapped_column(
        SAUuid, ForeignKey("users.id", ondelete="CASCADE"), primary_key=True
    )
    selected_goals: Mapped[list[str]] = mapped_column(
        JSON, nullable=False, default=list, server_default=text("'[]'::json")
    )
    daily_goal_minutes: Mapped[int] = mapped_column(
        Integer, nullable=False, default=30, server_default="30"
    )
    locale: Mapped[str] = mapped_column(String(5), nullable=False, default="es")
    notifications_enabled: Mapped[bool] = mapped_column(
        Boolean, nullable=False, default=True, server_default=text("true")
    )
