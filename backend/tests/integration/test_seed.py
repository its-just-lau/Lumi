"""Pruebas de la semilla contra PostgreSQL real."""

from __future__ import annotations

from scripts.seed_demo import seed
from sqlalchemy import create_engine, select, text

from app.core.database import get_session_factory, reset_engine
from app.core.security import verify_password
from app.modules.auth.entities import User

CORREO = "demo.integracion@lumi.app"
CLAVE = "Clave-de-prueba-123"


def _contar(engine_url: str) -> dict[str, int]:
    engine = create_engine(engine_url)
    consultas = {
        "users": "SELECT count(*) FROM users",
        "semesters": "SELECT count(*) FROM semesters",
        "subjects": "SELECT count(*) FROM subjects",
        "tasks": "SELECT count(*) FROM tasks",
        "subtasks": "SELECT count(*) FROM subtasks",
        "store_items": "SELECT count(*) FROM store_items",
        "achievements": "SELECT count(*) FROM achievements",
        "user_achievements": "SELECT count(*) FROM user_achievements",
    }
    try:
        with engine.connect() as connection:
            return {
                nombre: connection.execute(text(sql)).scalar_one()
                for nombre, sql in consultas.items()
            }
    finally:
        engine.dispose()


def test_la_semilla_es_idempotente(esquema_migrado: str) -> None:
    creado = seed(CORREO, "Demo Integración", CLAVE, None)
    assert creado is True
    primeros = _contar(esquema_migrado)
    assert primeros == {
        "users": 1,
        "semesters": 2,
        "subjects": 4,
        "tasks": 8,
        "subtasks": 11,
        "store_items": 4,
        "achievements": 9,
        "user_achievements": 2,
    }

    repetido = seed(CORREO, "Demo Integración", None, None)
    assert repetido is False
    assert _contar(esquema_migrado) == primeros


def test_la_contrasena_guardada_es_verificable(esquema_migrado: str) -> None:
    seed(CORREO, "Demo Integración", CLAVE, None)

    reset_engine()
    with get_session_factory()() as session:
        user = session.scalar(select(User).where(User.email == CORREO))

    assert user is not None
    assert user.password_hash is not None
    assert user.password_hash != CLAVE
    assert verify_password(CLAVE, user.password_hash) is True
