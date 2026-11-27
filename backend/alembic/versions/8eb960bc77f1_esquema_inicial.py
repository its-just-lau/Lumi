"""Esquema inicial: autenticación, perfil y núcleo académico.

Crea las tablas necesarias para la Fase 2: cuentas, tokens, preferencias,
semestres, materias, tareas, subtareas, catálogo de tienda y logros.

Revision ID: 8eb960bc77f1
Revises:
Create Date: 2026-11-26 01:42:15.296593
"""

from __future__ import annotations

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op

revision: str = "8eb960bc77f1"
down_revision: str | Sequence[str] | None = None
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None

# Los enums se guardan como VARCHAR + CHECK para que las migraciones sean
# portables y no dependan de tipos nativos de PostgreSQL.
AUTH_PROVIDER = sa.Enum(
    "local",
    "google",
    name="auth_provider",
    native_enum=False,
    create_constraint=True,
    length=20,
)
STORE_ITEM_TYPE = sa.Enum(
    "avatar",
    "theme",
    "boost",
    "streak_freeze",
    "cosmetic",
    name="store_item_type",
    native_enum=False,
    create_constraint=True,
    length=20,
)
SUBJECT_STATUS = sa.Enum(
    "active",
    "archived",
    name="subject_status",
    native_enum=False,
    create_constraint=True,
    length=20,
)
TASK_PRIORITY = sa.Enum(
    "low",
    "medium",
    "high",
    name="task_priority",
    native_enum=False,
    create_constraint=True,
    length=20,
)
TASK_STATUS = sa.Enum(
    "pending",
    "completed",
    name="task_status",
    native_enum=False,
    create_constraint=True,
    length=20,
)


def _timestamps() -> list[sa.Column]:
    """Columnas comunes de creación y actualización."""
    return [
        sa.Column(
            "created_at",
            sa.DateTime(timezone=True),
            server_default=sa.text("now()"),
            nullable=False,
        ),
        sa.Column(
            "updated_at",
            sa.DateTime(timezone=True),
            server_default=sa.text("now()"),
            nullable=False,
        ),
    ]


def upgrade() -> None:
    op.create_table(
        "achievements",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("code", sa.String(length=60), nullable=False),
        sa.Column("title", sa.String(length=120), nullable=False),
        sa.Column("description", sa.Text(), nullable=False),
        sa.Column("target", sa.Integer(), server_default="1", nullable=False),
        *_timestamps(),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_achievements")),
        sa.UniqueConstraint("code", name=op.f("uq_achievements_code")),
    )
    op.create_table(
        "store_items",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("code", sa.String(length=60), nullable=False),
        sa.Column("name", sa.String(length=120), nullable=False),
        sa.Column("description", sa.Text(), nullable=True),
        sa.Column("price_gems", sa.Integer(), nullable=False),
        sa.Column("type", STORE_ITEM_TYPE, nullable=False),
        sa.Column(
            "effects",
            sa.JSON(),
            server_default=sa.text("'{}'::json"),
            nullable=False,
        ),
        sa.Column("active", sa.Boolean(), server_default=sa.text("true"), nullable=False),
        *_timestamps(),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_store_items")),
        sa.UniqueConstraint("code", name=op.f("uq_store_items_code")),
    )
    op.create_table(
        "users",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("name", sa.String(length=120), nullable=False),
        sa.Column("email", sa.String(length=320), nullable=False),
        sa.Column("password_hash", sa.String(length=255), nullable=True),
        sa.Column(
            "auth_provider",
            AUTH_PROVIDER,
            server_default="local",
            nullable=False,
        ),
        sa.Column("locale", sa.String(length=5), nullable=False),
        sa.Column("time_zone", sa.String(length=64), nullable=False),
        *_timestamps(),
        sa.Column("version", sa.Integer(), server_default="1", nullable=False),
        sa.Column("deleted_at", sa.DateTime(timezone=True), nullable=True),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_users")),
    )
    op.create_index(
        "uq_users_email_lower",
        "users",
        [sa.text("lower(email)")],
        unique=True,
    )
    op.create_table(
        "password_resets",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("user_id", sa.Uuid(), nullable=False),
        sa.Column("token_hash", sa.String(length=128), nullable=False),
        sa.Column("expires_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("used_at", sa.DateTime(timezone=True), nullable=True),
        *_timestamps(),
        sa.ForeignKeyConstraint(
            ["user_id"],
            ["users.id"],
            name=op.f("fk_password_resets_user_id_users"),
            ondelete="CASCADE",
        ),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_password_resets")),
        sa.UniqueConstraint("token_hash", name=op.f("uq_password_resets_token_hash")),
    )
    op.create_index(
        op.f("ix_password_resets_user_id"),
        "password_resets",
        ["user_id"],
        unique=False,
    )
    op.create_table(
        "refresh_tokens",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("user_id", sa.Uuid(), nullable=False),
        sa.Column("token_hash", sa.String(length=128), nullable=False),
        sa.Column("expires_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("revoked_at", sa.DateTime(timezone=True), nullable=True),
        sa.Column("replaced_by_id", sa.Uuid(), nullable=True),
        *_timestamps(),
        sa.ForeignKeyConstraint(
            ["replaced_by_id"],
            ["refresh_tokens.id"],
            name=op.f("fk_refresh_tokens_replaced_by_id_refresh_tokens"),
            ondelete="SET NULL",
        ),
        sa.ForeignKeyConstraint(
            ["user_id"],
            ["users.id"],
            name=op.f("fk_refresh_tokens_user_id_users"),
            ondelete="CASCADE",
        ),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_refresh_tokens")),
        sa.UniqueConstraint("token_hash", name=op.f("uq_refresh_tokens_token_hash")),
    )
    op.create_index(
        op.f("ix_refresh_tokens_user_id"),
        "refresh_tokens",
        ["user_id"],
        unique=False,
    )
    op.create_table(
        "semesters",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("user_id", sa.Uuid(), nullable=False),
        sa.Column("name", sa.String(length=120), nullable=False),
        sa.Column("start_date", sa.Date(), nullable=True),
        sa.Column("end_date", sa.Date(), nullable=True),
        *_timestamps(),
        sa.Column("version", sa.Integer(), server_default="1", nullable=False),
        sa.Column("deleted_at", sa.DateTime(timezone=True), nullable=True),
        sa.ForeignKeyConstraint(
            ["user_id"],
            ["users.id"],
            name=op.f("fk_semesters_user_id_users"),
            ondelete="CASCADE",
        ),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_semesters")),
    )
    op.create_index(op.f("ix_semesters_user_id"), "semesters", ["user_id"], unique=False)
    op.create_table(
        "user_achievements",
        sa.Column("user_id", sa.Uuid(), nullable=False),
        sa.Column("achievement_id", sa.Uuid(), nullable=False),
        sa.Column("unlocked_at", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(
            ["achievement_id"],
            ["achievements.id"],
            name=op.f("fk_user_achievements_achievement_id_achievements"),
            ondelete="CASCADE",
        ),
        sa.ForeignKeyConstraint(
            ["user_id"],
            ["users.id"],
            name=op.f("fk_user_achievements_user_id_users"),
            ondelete="CASCADE",
        ),
        sa.PrimaryKeyConstraint(
            "user_id",
            "achievement_id",
            name=op.f("pk_user_achievements"),
        ),
    )
    op.create_table(
        "user_preferences",
        sa.Column("user_id", sa.Uuid(), nullable=False),
        sa.Column(
            "selected_goals",
            sa.JSON(),
            server_default=sa.text("'[]'::json"),
            nullable=False,
        ),
        sa.Column(
            "daily_goal_minutes",
            sa.Integer(),
            server_default="30",
            nullable=False,
        ),
        sa.Column("locale", sa.String(length=5), nullable=False),
        sa.Column(
            "notifications_enabled",
            sa.Boolean(),
            server_default=sa.text("true"),
            nullable=False,
        ),
        *_timestamps(),
        sa.Column("version", sa.Integer(), server_default="1", nullable=False),
        sa.ForeignKeyConstraint(
            ["user_id"],
            ["users.id"],
            name=op.f("fk_user_preferences_user_id_users"),
            ondelete="CASCADE",
        ),
        sa.PrimaryKeyConstraint("user_id", name=op.f("pk_user_preferences")),
    )
    op.create_table(
        "subjects",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("user_id", sa.Uuid(), nullable=False),
        sa.Column("semester_id", sa.Uuid(), nullable=True),
        sa.Column("name", sa.String(length=120), nullable=False),
        sa.Column("description", sa.Text(), nullable=True),
        sa.Column("color", sa.String(length=9), nullable=False),
        sa.Column(
            "status",
            SUBJECT_STATUS,
            server_default="active",
            nullable=False,
        ),
        *_timestamps(),
        sa.Column("version", sa.Integer(), server_default="1", nullable=False),
        sa.Column("deleted_at", sa.DateTime(timezone=True), nullable=True),
        sa.ForeignKeyConstraint(
            ["semester_id"],
            ["semesters.id"],
            name=op.f("fk_subjects_semester_id_semesters"),
            ondelete="SET NULL",
        ),
        sa.ForeignKeyConstraint(
            ["user_id"],
            ["users.id"],
            name=op.f("fk_subjects_user_id_users"),
            ondelete="CASCADE",
        ),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_subjects")),
    )
    op.create_index(op.f("ix_subjects_semester_id"), "subjects", ["semester_id"], unique=False)
    op.create_index(op.f("ix_subjects_user_id"), "subjects", ["user_id"], unique=False)
    op.create_table(
        "tasks",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("user_id", sa.Uuid(), nullable=False),
        sa.Column("subject_id", sa.Uuid(), nullable=True),
        sa.Column("title", sa.String(length=200), nullable=False),
        sa.Column("description", sa.Text(), nullable=True),
        sa.Column("due_date", sa.Date(), nullable=True),
        sa.Column("due_time", sa.Time(), nullable=True),
        sa.Column("all_day", sa.Boolean(), server_default=sa.text("false"), nullable=False),
        sa.Column("time_zone", sa.String(length=64), nullable=False),
        sa.Column(
            "priority",
            TASK_PRIORITY,
            server_default="medium",
            nullable=False,
        ),
        sa.Column(
            "status",
            TASK_STATUS,
            server_default="pending",
            nullable=False,
        ),
        sa.Column("completed_at", sa.DateTime(timezone=True), nullable=True),
        sa.Column("rewarded_at", sa.DateTime(timezone=True), nullable=True),
        *_timestamps(),
        sa.Column("version", sa.Integer(), server_default="1", nullable=False),
        sa.Column("deleted_at", sa.DateTime(timezone=True), nullable=True),
        sa.ForeignKeyConstraint(
            ["subject_id"],
            ["subjects.id"],
            name=op.f("fk_tasks_subject_id_subjects"),
            ondelete="SET NULL",
        ),
        sa.ForeignKeyConstraint(
            ["user_id"],
            ["users.id"],
            name=op.f("fk_tasks_user_id_users"),
            ondelete="CASCADE",
        ),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_tasks")),
    )
    op.create_index(op.f("ix_tasks_subject_id"), "tasks", ["subject_id"], unique=False)
    op.create_index(op.f("ix_tasks_user_id"), "tasks", ["user_id"], unique=False)
    op.create_table(
        "subtasks",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("task_id", sa.Uuid(), nullable=False),
        sa.Column("title", sa.String(length=200), nullable=False),
        sa.Column(
            "completed",
            sa.Boolean(),
            server_default=sa.text("false"),
            nullable=False,
        ),
        *_timestamps(),
        sa.Column("version", sa.Integer(), server_default="1", nullable=False),
        sa.Column("deleted_at", sa.DateTime(timezone=True), nullable=True),
        sa.ForeignKeyConstraint(
            ["task_id"],
            ["tasks.id"],
            name=op.f("fk_subtasks_task_id_tasks"),
            ondelete="CASCADE",
        ),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_subtasks")),
    )
    op.create_index(op.f("ix_subtasks_task_id"), "subtasks", ["task_id"], unique=False)


def downgrade() -> None:
    op.drop_index(op.f("ix_subtasks_task_id"), table_name="subtasks")
    op.drop_table("subtasks")
    op.drop_index(op.f("ix_tasks_user_id"), table_name="tasks")
    op.drop_index(op.f("ix_tasks_subject_id"), table_name="tasks")
    op.drop_table("tasks")
    op.drop_index(op.f("ix_subjects_user_id"), table_name="subjects")
    op.drop_index(op.f("ix_subjects_semester_id"), table_name="subjects")
    op.drop_table("subjects")
    op.drop_table("user_preferences")
    op.drop_table("user_achievements")
    op.drop_index(op.f("ix_semesters_user_id"), table_name="semesters")
    op.drop_table("semesters")
    op.drop_index(op.f("ix_refresh_tokens_user_id"), table_name="refresh_tokens")
    op.drop_table("refresh_tokens")
    op.drop_index(op.f("ix_password_resets_user_id"), table_name="password_resets")
    op.drop_table("password_resets")
    op.drop_table("users")
    op.drop_table("store_items")
    op.drop_table("achievements")
