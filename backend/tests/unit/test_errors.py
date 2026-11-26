"""Pruebas del formato de error del contrato."""

from __future__ import annotations

from fastapi import APIRouter
from fastapi.testclient import TestClient

from app.main import create_app


def _client_with(router: APIRouter) -> TestClient:
    """App de prueba con un router temporal para provocar errores concretos."""
    app = create_app()
    app.include_router(router, prefix="/api/v1")
    return TestClient(app)


def test_ruta_inexistente_devuelve_not_found(client: TestClient) -> None:
    response = client.get("/api/v1/no-existe")

    assert response.status_code == 404
    body = response.json()
    assert body["code"] == "NOT_FOUND"
    assert set(body) == {"code", "message", "requestId"}


def test_metodo_no_permitido(client: TestClient) -> None:
    response = client.post("/api/v1/health")

    assert response.status_code == 405
    assert response.json()["code"] == "METHOD_NOT_ALLOWED"


def test_error_de_validacion_incluye_detalles() -> None:
    router = APIRouter()

    @router.get("/prueba-validacion")
    async def prueba_validacion(limit: int) -> dict[str, int]:
        return {"limit": limit}

    response = _client_with(router).get("/api/v1/prueba-validacion?limit=abc")

    assert response.status_code == 422
    body = response.json()
    assert body["code"] == "VALIDATION_ERROR"
    assert body["details"]["errors"][0]["field"] == "query.limit"


def test_error_inesperado_no_filtra_la_traza() -> None:
    router = APIRouter()

    @router.get("/prueba-error")
    async def prueba_error() -> None:
        raise RuntimeError("fallo interno de prueba")

    response = _client_with(router).get("/api/v1/prueba-error")

    assert response.status_code == 500
    assert response.json()["code"] == "INTERNAL_ERROR"
    assert "RuntimeError" not in response.text
    assert "Traceback" not in response.text
