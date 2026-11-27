"""Entidades del núcleo académico: semestres y materias."""

from __future__ import annotations

import uuid
from datetime import date
from enum import StrEnum

from sqlalchemy import Date, ForeignKey, String, Text
from sqlalchemy import Uuid as SAUuid
from sqlalchemy.orm import Mapped, mapped_column

from app.core.database import (
    Base,
    SoftDeleteMixin,
    TimestampMixin,
    VersionMixin,
    enum_type,
)


class SubjectStatus(StrEnum):
    ACTIVE = "active"
    ARCHIVED = "archived"


class Semester(Base, TimestampMixin, VersionMixin, SoftDeleteMixin):
    __tablename__ = "semesters"

    id: Mapped[uuid.UUID] = mapped_column(SAUuid, primary_key=True, default=uuid.uuid4)
    user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True
    )
    name: Mapped[str] = mapped_column(String(120), nullable=False)
    start_date: Mapped[date | None] = mapped_column(Date)
    end_date: Mapped[date | None] = mapped_column(Date)


class Subject(Base, TimestampMixin, VersionMixin, SoftDeleteMixin):
    __tablename__ = "subjects"

    id: Mapped[uuid.UUID] = mapped_column(SAUuid, primary_key=True, default=uuid.uuid4)
    user_id: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True
    )
    semester_id: Mapped[uuid.UUID | None] = mapped_column(
        ForeignKey("semesters.id", ondelete="SET NULL"), index=True
    )
    name: Mapped[str] = mapped_column(String(120), nullable=False)
    description: Mapped[str | None] = mapped_column(Text)
    color: Mapped[str] = mapped_column(String(9), nullable=False, default="#6C5CE7")
    status: Mapped[SubjectStatus] = mapped_column(
        enum_type(SubjectStatus, "subject_status", 20),
        nullable=False,
        default=SubjectStatus.ACTIVE,
        server_default=SubjectStatus.ACTIVE.value,
    )
