"""Pruebas del encabezado `X-Request-Id`."""

from __future__ import annotations

import uuid

from fastapi.testclient import TestClient

HEADER = "X-Request-Id"
VALID_ID = "3f1c9b7e-6a4d-4b1e-9f2a-0d5c8e7b1a23"


def _is_uuid(value: str) -> bool:
    try:
        uuid.UUID(value)
    except ValueError:
        return False
    return True


def test_genera_un_identificador_cuando_no_llega(client: TestClient) -> None:
    response = client.get("/api/v1/health")

    assert HEADER in response.headers
    assert _is_uuid(response.headers[HEADER])


def test_reutiliza_el_identificador_recibido(client: TestClient) -> None:
    response = client.get("/api/v1/health", headers={HEADER: VALID_ID})

    assert response.headers[HEADER] == VALID_ID


def test_reemplaza_los_identificadores_que_no_son_uuid(client: TestClient) -> None:
    response = client.get("/api/v1/health", headers={HEADER: "no-es-un-uuid"})

    assert response.headers[HEADER] != "no-es-un-uuid"
    assert _is_uuid(response.headers[HEADER])


def test_normaliza_un_uuid_sin_guiones(client: TestClient) -> None:
    raw = VALID_ID.replace("-", "")

    response = client.get("/api/v1/health", headers={HEADER: raw})

    assert response.headers[HEADER] == VALID_ID


def test_los_errores_llevan_el_mismo_identificador(client: TestClient) -> None:
    response = client.get("/api/v1/ruta-inexistente", headers={HEADER: VALID_ID})

    assert response.status_code == 404
    assert response.headers[HEADER] == VALID_ID
    assert response.json()["requestId"] == VALID_ID
