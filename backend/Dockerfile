# Imagen de desarrollo local del API. Render no usa este archivo: construye el
# servicio con el runtime de Python y `requirements.txt`.
FROM python:3.12-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_NO_CACHE_DIR=1

WORKDIR /app

# Se instalan también las dependencias de desarrollo para poder ejecutar
# `pytest` y `ruff` dentro del contenedor.
COPY requirements.txt requirements-dev.txt ./
RUN pip install --no-cache-dir -r requirements-dev.txt

COPY . .

EXPOSE 8000

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
