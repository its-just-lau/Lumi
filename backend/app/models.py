"""Registro central de todas las entidades.

Alembic y el script de semilla importan este módulo para que `Base.metadata`
contenga todas las tablas, sin depender de qué controladores estén cargados.
"""

from __future__ import annotations

from app.modules.academics.entities import Semester, Subject
from app.modules.achievements.entities import Achievement, UserAchievement
from app.modules.auth.entities import PasswordReset, RefreshToken, User
from app.modules.store.entities import StoreItem
from app.modules.tasks.entities import Subtask, Task
from app.modules.users.entities import UserPreference

__all__ = [
    "Achievement",
    "PasswordReset",
    "RefreshToken",
    "Semester",
    "StoreItem",
    "Subject",
    "Subtask",
    "Task",
    "User",
    "UserAchievement",
    "UserPreference",
]
