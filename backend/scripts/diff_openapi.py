"""Compara las operaciones implementadas con el contrato `api/openapi.yaml`.

Uso (desde `backend/`):

    python scripts/diff_openapi.py
    python scripts/diff_openapi.py --strict

Es el esqueleto de la Fase 1: compara rutas, métodos HTTP y `operationId`. La
comparación de esquemas de petición y respuesta se completa en las fases 4 y 8.
Sin `--strict` siempre sale con código 0, para que pueda usarse mientras el API
todavía no cubre las 72 operaciones del contrato.
"""

from __future__ import annotations

import argparse
import sys
from pathlib import Path
from typing import Any

import yaml

BACKEND_DIR = Path(__file__).resolve().parents[1]
CONTRACT_PATH = BACKEND_DIR.parent / "api" / "openapi.yaml"
API_PREFIX = "/api/v1"
HTTP_METHODS = frozenset({"get", "post", "put", "patch", "delete", "head", "options"})

if str(BACKEND_DIR) not in sys.path:
    sys.path.insert(0, str(BACKEND_DIR))

from app.main import app  # noqa: E402


def contract_operations(contract: dict[str, Any]) -> dict[tuple[str, str], str | None]:
    """Extrae `(ruta, método) -> operationId` del contrato."""
    operations: dict[tuple[str, str], str | None] = {}
    for path, item in contract.get("paths", {}).items():
        for method, operation in item.items():
            if method.lower() not in HTTP_METHODS:
                continue
            operations[(path, method.upper())] = operation.get("operationId")
    return operations


def implemented_operations() -> dict[tuple[str, str], str | None]:
    """Extrae las operaciones del esquema que genera FastAPI, sin el prefijo."""
    operations: dict[tuple[str, str], str | None] = {}
    for path, item in app.openapi().get("paths", {}).items():
        normalized = path[len(API_PREFIX) :] if path.startswith(API_PREFIX) else path
        for method, operation in item.items():
            if method.lower() not in HTTP_METHODS:
                continue
            operations[(normalized, method.upper())] = operation.get("operationId")
    return operations


def main() -> int:
    parser = argparse.ArgumentParser(description="Compara el API con openapi.yaml.")
    parser.add_argument(
        "--strict",
        action="store_true",
        help="Sale con código 1 si hay diferencias (para usarlo en CI).",
    )
    args = parser.parse_args()

    with CONTRACT_PATH.open(encoding="utf-8") as handle:
        contract = yaml.safe_load(handle)

    expected = contract_operations(contract)
    current = implemented_operations()

    missing = sorted(set(expected) - set(current))
    extra = sorted(set(current) - set(expected))
    mismatched = sorted(
        (key, expected[key], current[key])
        for key in set(expected) & set(current)
        if expected[key] != current[key]
    )

    print(f"Contrato:     {len(expected)} operaciones en {CONTRACT_PATH}")
    print(f"Implementado: {len(current)} operaciones")
    print(f"Faltan:       {len(missing)}")
    print(f"Sobran:       {len(extra)}")
    print(f"operationId distintos: {len(mismatched)}")

    for path, method in missing:
        print(f"  falta   {method} {path}")
    for path, method in extra:
        print(f"  sobra   {method} {path}")
    for (path, method), wanted, found in mismatched:
        print(f"  difiere {method} {path}: contrato={wanted} código={found}")

    if args.strict and (missing or extra or mismatched):
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
