"""Catálogo de mensajes de error y negociación de `Accept-Language`.

El contrato exige que `message` viaje en el idioma de la petición, con español por
defecto. Los mensajes se indexan por el código de error del contrato, de modo que
cualquier `ApiError` solo necesite indicar su código.
"""

from __future__ import annotations

DEFAULT_LOCALE = "es"
SUPPORTED_LOCALES = ("es", "en")

# Códigos del contrato que ya tienen mensaje localizado. Se amplía por fase.
MESSAGES: dict[str, dict[str, str]] = {
    "es": {
        "BAD_REQUEST": "La solicitud no es válida.",
        "UNAUTHORIZED": "Debes iniciar sesión para continuar.",
        "FORBIDDEN": "No tienes permiso para realizar esta acción.",
        "NOT_FOUND": "El recurso solicitado no existe.",
        "METHOD_NOT_ALLOWED": "El método HTTP no está permitido para esta ruta.",
        "VALIDATION_ERROR": "Los datos enviados no son válidos.",
        "VERSION_CONFLICT": "El recurso cambió desde la última lectura. Vuelve a cargarlo.",
        "RESOURCE_DELETED": "El recurso fue eliminado.",
        "SYNC_EXPIRED": "La sincronización expiró. Vuelve a sincronizar desde el inicio.",
        "RATE_LIMITED": "Demasiadas solicitudes. Inténtalo más tarde.",
        "QUOTA_EXCEEDED": "Alcanzaste el límite de uso de la IA por hoy.",
        "INSUFFICIENT_GEMS": "No tienes gemas suficientes para esta compra.",
        "FILE_TOO_LARGE": "El archivo supera el tamaño máximo permitido.",
        "UNSUPPORTED_FILE": "El tipo de archivo no está permitido.",
        "INTERNAL_ERROR": "Ocurrió un error inesperado. Inténtalo de nuevo.",
        "SERVICE_UNAVAILABLE": "El servicio no está disponible en este momento.",
    },
    "en": {
        "BAD_REQUEST": "The request is not valid.",
        "UNAUTHORIZED": "You must sign in to continue.",
        "FORBIDDEN": "You do not have permission to perform this action.",
        "NOT_FOUND": "The requested resource does not exist.",
        "METHOD_NOT_ALLOWED": "The HTTP method is not allowed for this route.",
        "VALIDATION_ERROR": "The submitted data is not valid.",
        "VERSION_CONFLICT": "The resource changed since it was last read. Load it again.",
        "RESOURCE_DELETED": "The resource was deleted.",
        "SYNC_EXPIRED": "Synchronization expired. Synchronize again from the beginning.",
        "RATE_LIMITED": "Too many requests. Try again later.",
        "QUOTA_EXCEEDED": "You reached the AI usage limit for today.",
        "INSUFFICIENT_GEMS": "You do not have enough gems for this purchase.",
        "FILE_TOO_LARGE": "The file exceeds the maximum allowed size.",
        "UNSUPPORTED_FILE": "The file type is not allowed.",
        "INTERNAL_ERROR": "An unexpected error occurred. Try again.",
        "SERVICE_UNAVAILABLE": "The service is not available right now.",
    },
}


def resolve_locale(accept_language: str | None) -> str:
    """Elige el idioma soportado con mayor preferencia de `Accept-Language`.

    Ejemplos: `en-US,en;q=0.9` devuelve `en`; `fr` devuelve `es` por defecto.
    """
    if not accept_language:
        return DEFAULT_LOCALE

    candidates: list[tuple[float, int, str]] = []
    for index, part in enumerate(accept_language.split(",")):
        token, _, parameters = part.partition(";")
        language = token.strip().lower().split("-")[0]
        if not language:
            continue

        quality = 1.0
        parameters = parameters.strip()
        if parameters.startswith("q="):
            try:
                quality = float(parameters[2:])
            except ValueError:
                quality = 0.0
        if quality <= 0:
            continue

        candidates.append((quality, -index, language))

    for _, _, language in sorted(candidates, reverse=True):
        if language in SUPPORTED_LOCALES:
            return language
    return DEFAULT_LOCALE


def translate(code: str, locale: str) -> str:
    """Devuelve el mensaje del código en el idioma pedido."""
    catalog = MESSAGES.get(locale, MESSAGES[DEFAULT_LOCALE])
    return catalog.get(code) or MESSAGES[DEFAULT_LOCALE]["INTERNAL_ERROR"]
