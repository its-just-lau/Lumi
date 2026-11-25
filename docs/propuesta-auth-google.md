# Propuesta de contrato: inicio de sesión con Google

**Estado:** el contrato v1.3 no tiene este endpoint. La app Android ya lo llama
(`AuthService.loginWithGoogle`), pero fallará con 404 hasta que el backend lo implemente
(y Prism no lo simulará hasta que se agregue al `openapi.yaml`).

## Flujo

1. En la app, el botón **Continuar con Google** abre el selector de cuentas
   (Credential Manager) y obtiene un **ID token** de Google.
2. La app envía el token a `POST /auth/google`. El token nunca se guarda en el teléfono.
3. El backend **verifica el token con Google** (firma, `aud` = su ID de cliente Web, `exp`,
   `email_verified`), busca al usuario por correo y, si no existe, lo crea.
4. Responde igual que `/auth/login`: `AuthResponse` con `accessToken`, `refreshToken`,
   `expiresIn` y `user`. A partir de ahí la sesión es la normal de Lumi.

## Fragmento para `openapi.yaml`

```yaml
  /auth/google:
    post:
      tags: [Auth]
      summary: Iniciar sesión o registrarse con Google
      operationId: loginWithGoogle
      security: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [idToken]
              properties:
                idToken: {type: string, description: ID token emitido por Google}
      responses:
        '200':
          description: Sesión iniciada
          content:
            application/json:
              schema: {$ref: '#/components/schemas/AuthResponse'}
        '401': {$ref: '#/components/responses/Unauthorized'}
```

(Usar el mismo esquema de respuesta que ya tiene `/auth/login`.)

## Cosas que el backend debe decidir

- Si ya existe una cuenta con ese correo registrada con contraseña: se vincula o se rechaza.
- Cuentas creadas con Google no tienen contraseña (`/auth/password/*` no aplica).
- El nombre del usuario se toma del campo `name` del ID token.
