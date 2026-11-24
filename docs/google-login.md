# Configuración del inicio de sesión con Google

Se hace en el **mismo proyecto de Google Cloud** que Google Calendar (ver `google-calendar.md`).

1. **Credenciales > ID de cliente de OAuth > Aplicación web**. Copia el *ID de cliente*
   (termina en `.apps.googleusercontent.com`). Este es el que el backend usa para verificar
   el token y el que la app envía como `serverClientId`.
2. Debe existir también el cliente **Android** (paquete `com.lumi.app` + SHA-1 de debug);
   sin él, Google rechaza la solicitud aunque el ID Web sea correcto.
3. En `local.properties` (no se sube a Git) agrega:

   ```
   lumi.googleWebClientId=TU-ID-WEB.apps.googleusercontent.com
   ```

4. Sincroniza Gradle. Sin este valor, el botón muestra
   "El inicio de sesión con Google aún no está configurado".
5. Con la app en modo *Pruebas*, solo entran los correos agregados como usuarios de prueba.
6. El backend necesita implementar `POST /auth/google`: ver `propuesta-auth-google.md`.
