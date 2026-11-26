"""Pruebas de la negociación de `Accept-Language`."""

from __future__ import annotations

from fastapi.testclient import TestClient

SPANISH_MESSAGE = "El recurso solicitado no existe."
ENGLISH_MESSAGE = "The requested resource does not exist."


def _message(client: TestClient, accept_language: str | None = None) -> str:
    headers = {"Accept-Language": accept_language} if accept_language else {}
    return client.get("/api/v1/ruta-inexistente", headers=headers).json()["message"]


def test_espanol_por_defecto(client: TestClient) -> None:
    assert _message(client) == SPANISH_MESSAGE


def test_ingles_cuando_se_pide(client: TestClient) -> None:
    assert _message(client, "en") == ENGLISH_MESSAGE


def test_negocia_con_pesos(client: TestClient) -> None:
    assert _message(client, "en-US,en;q=0.9") == ENGLISH_MESSAGE


def test_un_idioma_no_soportado_cae_en_espanol(client: TestClient) -> None:
    assert _message(client, "fr-FR,fr;q=0.9") == SPANISH_MESSAGE


def test_un_idioma_con_q_cero_se_descarta(client: TestClient) -> None:
    assert _message(client, "en;q=0, es;q=0.5") == SPANISH_MESSAGE
