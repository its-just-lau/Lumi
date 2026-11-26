"""Punto de entrada del API de Lumi."""

from __future__ import annotations

import logging
from collections.abc import AsyncIterator
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app import __version__
from app.core.config import get_settings
from app.core.errors import UnhandledErrorMiddleware, register_error_handlers
from app.core.logging import configure_logging
from app.core.request_id import REQUEST_ID_HEADER, RequestIdMiddleware
from app.modules.system.controllers import router as system_router

API_PREFIX = "/api/v1"
API_TITLE = "Lumi API"
API_DESCRIPTION = (
    "API REST de Lumi. Implementa el contrato `api/openapi.yaml` (v1.4.0) bajo el "
    "prefijo `/api/v1`. Esta especificación se genera desde el código: úsala junto "
    "con `backend/scripts/diff_openapi.py` para detectar desviaciones."
)

logger = logging.getLogger("lumi")


@asynccontextmanager
async def lifespan(app: FastAPI) -> AsyncIterator[None]:
    """Tareas de arranque y apagado del servicio."""
    settings = get_settings()
    logger.info("API iniciada (entorno=%s, versión=%s)", settings.app_env, __version__)
    yield
    logger.info("API detenida")


def create_app() -> FastAPI:
    """Construye la aplicación con middlewares, manejadores de error y routers."""
    configure_logging()
    settings = get_settings()

    app = FastAPI(
        title=API_TITLE,
        version=__version__,
        description=API_DESCRIPTION,
        docs_url="/docs",
        openapi_url="/openapi.json",
        lifespan=lifespan,
    )

    # El orden importa: CORS queda fuera para que los errores también lleven sus
    # encabezados, y el manejo de errores va dentro del de X-Request-Id.
    app.add_middleware(UnhandledErrorMiddleware)
    app.add_middleware(RequestIdMiddleware)
    app.add_middleware(
        CORSMiddleware,
        allow_origins=settings.cors_origins,
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
        expose_headers=[REQUEST_ID_HEADER, "ETag"],
    )

    register_error_handlers(app)
    app.include_router(system_router, prefix=API_PREFIX)
    return app


app = create_app()
