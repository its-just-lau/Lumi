"""Configuración de logs con el identificador de petición en cada línea."""

from __future__ import annotations

import logging
from logging.config import dictConfig

from app.core.request_id import get_request_id

LOG_FORMAT = "%(asctime)s %(levelname)-8s [%(request_id)s] %(name)s: %(message)s"


class RequestIdLogFilter(logging.Filter):
    """Añade `request_id` a cada registro para poder correlacionar logs y respuestas."""

    def filter(self, record: logging.LogRecord) -> bool:
        record.request_id = get_request_id() or "-"
        return True


def configure_logging(level: str = "INFO") -> None:
    """Configura el logger raíz una sola vez por proceso."""
    dictConfig(
        {
            "version": 1,
            "disable_existing_loggers": False,
            "filters": {"request_id": {"()": RequestIdLogFilter}},
            "formatters": {
                "default": {
                    "format": LOG_FORMAT,
                    "datefmt": "%Y-%m-%d %H:%M:%S",
                }
            },
            "handlers": {
                "console": {
                    "class": "logging.StreamHandler",
                    "formatter": "default",
                    "filters": ["request_id"],
                }
            },
            "root": {"handlers": ["console"], "level": level},
        }
    )
