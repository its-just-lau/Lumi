"""Errores del API con el formato `{ code, message, details?, requestId }`."""

from __future__ import annotations

import logging
from typing import Any

from fastapi import FastAPI, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from starlette.exceptions import HTTPException as StarletteHTTPException
from starlette.types import ASGIApp, Receive, Scope, Send

from app.core.i18n import resolve_locale, translate
from app.core.request_id import get_request_id, new_request_id

LOGGER = logging.getLogger("lumi.errors")

# Código del contrato para las excepciones HTTP que no llevan uno propio.
STATUS_CODES: dict[int, str] = {
    400: "BAD_REQUEST",
    401: "UNAUTHORIZED",
    403: "FORBIDDEN",
    404: "NOT_FOUND",
    405: "METHOD_NOT_ALLOWED",
    413: "FILE_TOO_LARGE",
    415: "UNSUPPORTED_FILE",
    422: "VALIDATION_ERROR",
    429: "RATE_LIMITED",
    500: "INTERNAL_ERROR",
    503: "SERVICE_UNAVAILABLE",
}


class ApiError(Exception):
    """Error de negocio o de contrato que se traduce a una respuesta JSON estándar."""

    def __init__(
        self,
        status_code: int,
        code: str,
        details: dict[str, Any] | None = None,
        headers: dict[str, str] | None = None,
    ) -> None:
        super().__init__(code)
        self.status_code = status_code
        self.code = code
        self.details = details
        self.headers = headers


def _request_id(request: Request) -> str:
    request_id = getattr(request.state, "request_id", "") or get_request_id()
    return request_id or new_request_id()


def error_response(
    request: Request,
    status_code: int,
    code: str,
    details: dict[str, Any] | None = None,
    headers: dict[str, str] | None = None,
) -> JSONResponse:
    """Construye la respuesta de error con mensaje localizado y `requestId`."""
    locale = resolve_locale(request.headers.get("accept-language"))
    payload: dict[str, Any] = {
        "code": code,
        "message": translate(code, locale),
        "requestId": _request_id(request),
    }
    if details:
        payload["details"] = details
    return JSONResponse(status_code=status_code, content=payload, headers=headers)


async def api_error_handler(request: Request, exc: ApiError) -> JSONResponse:
    return error_response(request, exc.status_code, exc.code, exc.details, exc.headers)


async def http_exception_handler(request: Request, exc: StarletteHTTPException) -> JSONResponse:
    code = STATUS_CODES.get(exc.status_code, "INTERNAL_ERROR")
    headers = dict(exc.headers) if exc.headers else None
    return error_response(request, exc.status_code, code, headers=headers)


async def validation_exception_handler(
    request: Request, exc: RequestValidationError
) -> JSONResponse:
    details = {
        "errors": [
            {
                "field": ".".join(str(part) for part in error.get("loc", ())),
                "message": error.get("msg", ""),
            }
            for error in exc.errors()
        ]
    }
    return error_response(request, 422, "VALIDATION_ERROR", details)


class UnhandledErrorMiddleware:
    """Convierte cualquier excepción no controlada en el error estándar 500.

    Se coloca dentro del middleware de `X-Request-Id` para que la respuesta de error
    también lleve el encabezado y para no filtrar trazas al cliente.
    """

    def __init__(self, app: ASGIApp) -> None:
        self.app = app

    async def __call__(self, scope: Scope, receive: Receive, send: Send) -> None:
        if scope["type"] != "http":
            await self.app(scope, receive, send)
            return

        response_started = False

        async def send_wrapper(message: dict[str, Any]) -> None:
            nonlocal response_started
            if message["type"] == "http.response.start":
                response_started = True
            await send(message)

        try:
            await self.app(scope, receive, send_wrapper)
        except Exception:
            if response_started:
                raise
            LOGGER.exception(
                "Error no controlado en %s %s",
                scope.get("method", "-"),
                scope.get("path", "-"),
            )
            request = Request(scope, receive)
            response = error_response(request, 500, "INTERNAL_ERROR")
            await response(scope, receive, send)


def register_error_handlers(app: FastAPI) -> None:
    """Registra los manejadores de error del contrato."""
    app.add_exception_handler(ApiError, api_error_handler)
    app.add_exception_handler(StarletteHTTPException, http_exception_handler)
    app.add_exception_handler(RequestValidationError, validation_exception_handler)
