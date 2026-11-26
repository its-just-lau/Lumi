"""Identificador de petición (`X-Request-Id`) y su contexto."""

from __future__ import annotations

import logging
import time
import uuid
from contextvars import ContextVar

from starlette.datastructures import Headers, MutableHeaders
from starlette.types import ASGIApp, Message, Receive, Scope, Send

REQUEST_ID_HEADER = "X-Request-Id"

LOGGER = logging.getLogger("lumi.request")

_request_id: ContextVar[str] = ContextVar("request_id", default="")


def get_request_id() -> str:
    """Devuelve el identificador de la petición en curso (vacío fuera de una)."""
    return _request_id.get()


def new_request_id() -> str:
    """Genera un identificador nuevo con formato UUID."""
    return str(uuid.uuid4())


def normalize_request_id(value: str | None) -> str:
    """Reutiliza el identificador recibido si es un UUID; si no, genera uno nuevo.

    El contrato declara `requestId` como UUID, por eso no se aceptan valores libres.
    """
    if not value:
        return new_request_id()
    try:
        return str(uuid.UUID(value.strip()))
    except (AttributeError, ValueError):
        return new_request_id()


class RequestIdMiddleware:
    """Asigna un `X-Request-Id` a cada petición y lo devuelve en la respuesta.

    Se implementa como middleware ASGI puro para que el `ContextVar` funcione igual
    en endpoints síncronos y asíncronos.
    """

    def __init__(self, app: ASGIApp) -> None:
        self.app = app

    async def __call__(self, scope: Scope, receive: Receive, send: Send) -> None:
        if scope["type"] != "http":
            await self.app(scope, receive, send)
            return

        request_id = normalize_request_id(Headers(scope=scope).get(REQUEST_ID_HEADER))
        scope.setdefault("state", {})["request_id"] = request_id
        token = _request_id.set(request_id)

        started = time.perf_counter()
        status_code = 500

        async def send_with_request_id(message: Message) -> None:
            nonlocal status_code
            if message["type"] == "http.response.start":
                status_code = message["status"]
                MutableHeaders(scope=message)[REQUEST_ID_HEADER] = request_id
            await send(message)

        try:
            await self.app(scope, receive, send_with_request_id)
        finally:
            elapsed_ms = (time.perf_counter() - started) * 1000
            LOGGER.info(
                "%s %s -> %s (%.1f ms)",
                scope.get("method", "-"),
                scope.get("path", "-"),
                status_code,
                elapsed_ms,
            )
            _request_id.reset(token)
