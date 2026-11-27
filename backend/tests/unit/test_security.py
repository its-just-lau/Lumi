"""Pruebas del hash de contraseñas."""

from __future__ import annotations

from app.core.security import hash_password, verify_password


def test_argon2_genera_un_hash_distinto_de_la_contrasena() -> None:
    password = "Clave-de-prueba-123"
    password_hash = hash_password(password)

    assert password_hash != password
    assert password_hash.startswith("$argon2id$")


def test_verify_acepta_la_contrasena_correcta() -> None:
    password_hash = hash_password("Clave-de-prueba-123")

    assert verify_password("Clave-de-prueba-123", password_hash) is True


def test_verify_rechaza_la_contrasena_incorrecta() -> None:
    password_hash = hash_password("Clave-de-prueba-123")

    assert verify_password("otra-clave", password_hash) is False


def test_verify_rechaza_un_hash_invalido() -> None:
    assert verify_password("Clave-de-prueba-123", "no-es-un-hash") is False
