"""Pruebas de la detección de la base de datos configurada."""

from __future__ import annotations

from types import SimpleNamespace

import pytest

from app.core import database


def _settings(url: str) -> SimpleNamespace:
    return SimpleNamespace(database_url=url, app_env="development")


def test_sin_url_no_esta_configurada(monkeypatch: pytest.MonkeyPatch) -> None:
    monkeypatch.setattr(database, "get_settings", lambda: _settings(""))

    assert database.is_database_configured() is False


def test_los_marcadores_de_env_example_no_cuentan(monkeypatch: pytest.MonkeyPatch) -> None:
    monkeypatch.setattr(
        database,
        "get_settings",
        lambda: _settings("postgresql+psycopg://USUARIO:CONTRASEÑA@HOST/BD?sslmode=require"),
    )

    assert database.is_database_configured() is False


def test_una_url_real_cuenta_como_configurada(monkeypatch: pytest.MonkeyPatch) -> None:
    monkeypatch.setattr(
        database,
        "get_settings",
        lambda: _settings(
            "postgresql+psycopg://lumi:secreto@ep-abc.neon.tech/lumi?sslmode=require"
        ),
    )

    assert database.is_database_configured() is True
