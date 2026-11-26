"""Pruebas de `GET /api/v1/health`."""

from __future__ import annotations

from datetime import datetime

from fastapi.testclient import TestClient

from app import __version__


def test_devuelve_200_con_estado_ok(client: TestClient) -> None:
    response = client.get("/api/v1/health")

    assert response.status_code == 200
    body = response.json()
    assert body["status"] == "ok"
    assert body["version"] == __version__
    assert body["queue"] == "not_configured"


def test_omite_la_base_de_datos_mientras_no_este_configurada(client: TestClient) -> None:
    body = client.get("/api/v1/health").json()

    # `Health.database` solo admite `ok` o `down`; en la Fase 1 no hay base de datos.
    assert "database" not in body


def test_incluye_la_hora_en_utc(client: TestClient) -> None:
    body = client.get("/api/v1/health").json()

    moment = datetime.fromisoformat(body["time"].replace("Z", "+00:00"))
    assert moment.tzinfo is not None
    assert moment.utcoffset() is not None
    assert moment.utcoffset().total_seconds() == 0


def test_es_publico_y_no_exige_autenticacion(client: TestClient) -> None:
    assert client.get("/api/v1/health").status_code == 200


def test_swagger_y_especificacion_estan_disponibles(client: TestClient) -> None:
    docs = client.get("/docs")
    assert docs.status_code == 200
    assert "text/html" in docs.headers["content-type"]

    schema = client.get("/openapi.json")
    assert schema.status_code == 200

    operation = schema.json()["paths"]["/api/v1/health"]["get"]
    assert operation["operationId"] == "getHealth"
