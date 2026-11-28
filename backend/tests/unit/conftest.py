"""Ajustes comunes de las pruebas unitarias.

Las pruebas unitarias no deben tocar PostgreSQL. Se fuerza que el estado de la
base se considere "no configurado" salvo que una prueba lo sustituya a propósito.
"""

from __future__ import annotations

from collections.abc import Iterator

import pytest


@pytest.fixture(autouse=True)
def sin_base_de_datos(monkeypatch: pytest.MonkeyPatch) -> Iterator[None]:
    """Evita que `/health` intente conectarse a la base en las pruebas unitarias."""
    monkeypatch.setattr("app.modules.system.controllers.is_database_configured", lambda: False)
    yield
