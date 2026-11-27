"""Pruebas de las migraciones de Alembic."""

from __future__ import annotations

from sqlalchemy import create_engine, text

TABLAS_ESPERADAS = {
    "achievements",
    "password_resets",
    "refresh_tokens",
    "semesters",
    "store_items",
    "subjects",
    "subtasks",
    "tasks",
    "user_achievements",
    "user_preferences",
    "users",
}


def test_upgrade_head_crea_todas_las_tablas(esquema_migrado: str) -> None:
    engine = create_engine(esquema_migrado)
    try:
        with engine.connect() as connection:
            rows = connection.execute(
                text(
                    "SELECT tablename FROM pg_tables WHERE schemaname = 'public' ORDER BY tablename"
                )
            )
            tablas = {row[0] for row in rows}
    finally:
        engine.dispose()

    assert tablas >= TABLAS_ESPERADAS
    assert "alembic_version" in tablas


def test_los_enums_quedan_como_varchar_con_check(esquema_migrado: str) -> None:
    engine = create_engine(esquema_migrado)
    try:
        with engine.connect() as connection:
            tipo = connection.execute(
                text(
                    "SELECT data_type FROM information_schema.columns "
                    "WHERE table_name = 'tasks' AND column_name = 'status'"
                )
            ).scalar_one()
    finally:
        engine.dispose()

    assert tipo == "character varying"
