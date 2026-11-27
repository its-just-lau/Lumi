"""Prueba de `/health` con PostgreSQL real."""

from __future__ import annotations

from fastapi.testclient import TestClient

from app.main import create_app


def test_health_reporta_database_ok(esquema_migrado: str) -> None:
    with TestClient(create_app()) as client:
        response = client.get("/api/v1/health")

    assert response.status_code == 200
    body = response.json()
    assert body["status"] == "ok"
    assert body["database"] == "ok"
