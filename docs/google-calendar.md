# Integración con Google Calendar (servicio de terceros)

Lumi permite crear eventos en el Google Calendar del usuario desde el calendario de Inicio
y muestra sus próximos eventos junto a las tareas.

## Cómo funciona

1. El usuario toca **+** en el calendario de Inicio.
2. Google Identity Services (`AuthorizationClient`) muestra la pantalla de permisos
   (alcance `calendar.events`). El token se pide cada vez; Lumi **no** guarda contraseñas ni
   el token en disco, solo recuerda en el dispositivo que el usuario ya aceptó.
3. La app llama directamente a la API REST de Google Calendar v3
   (`GET/POST calendars/primary/events`) con un cliente Retrofit propio
   (`GoogleCalendarService`), distinto del de Lumi: los tokens de Lumi nunca van a Google.

Código: `features/calendar/` (acceso OAuth, ViewModel y formulario),
`core/network/GoogleCalendarService.kt`, `data/repositories/GoogleCalendarRepository.kt`.

## Configuración en Google Cloud (una sola vez)

1. Crear un proyecto en <https://console.cloud.google.com>.
2. **APIs y servicios > Biblioteca**: habilitar **Google Calendar API**.
3. **Pantalla de consentimiento de OAuth**: tipo *Externo*, estado *Pruebas*; agregar el
   alcance `https://www.googleapis.com/auth/calendar.events` y los correos de los
   evaluadores/integrantes como *usuarios de prueba*.
4. **Credenciales > Crear credenciales > ID de cliente de OAuth > Android**:
   - Nombre de paquete: `com.lumi.app`
   - Huella SHA-1: ejecutar `./gradlew signingReport` y copiar la de la variante `debug`
     (cada integrante que compile con su propia llave debug debe registrar la suya).
5. No hace falta poner ningún ID en el código ni en `local.properties`.

## Limitaciones conocidas

- El dispositivo/emulador necesita Google Play Services.
- Mientras la app esté en modo *Pruebas*, solo los usuarios de prueba pueden conectarse.
- Los eventos de varios días se muestran solo en su día de inicio.
