"""Fixtures compartidas por las pruebas unitarias."""

from __future__ import annotations

from collections.abc import Iterator

import pytest
from fastapi import FastAPI
from fastapi.testclient import TestClient

from app.main import create_app


@pytest.fixture
def app() -> FastAPI:
    """Aplicación nueva por prueba, para aislar middlewares y rutas temporales."""
    return create_app()


@pytest.fixture
def client(app: FastAPI) -> Iterator[TestClient]:
    """Cliente HTTP que ejecuta el ciclo de vida de la aplicación."""
    with TestClient(app) as test_client:
        yield test_client
