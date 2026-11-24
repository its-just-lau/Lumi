# Uso de inteligencia artificial en Lumi

Este documento explica cómo se usó la IA durante el desarrollo de la parte Android de Lumi y cómo se usará dentro del producto. Está escrito para la rúbrica del Proyecto Integrador y para que cualquier integrante sepa qué se hizo con IA y qué se revisó a mano.

> Alcance: describe el trabajo de la app Android. El backend lo desarrolla otro integrante del equipo; su uso de IA, si lo hay, debe agregarse en su propia sección.

## 1. Herramientas

| Herramienta | Para qué se usó | Dónde |
| --- | --- | --- |
| Claude (Anthropic) | Asistente de programación y diseño durante el desarrollo | Código Android, documentación, propuestas de diseño |
| Gemini (Google AI Studio, modelos Flash / Flash-Lite) | Generación de contenido de estudio dentro de la app | **Decidido, aún no implementado** |

## 2. Qué se hizo con ayuda de IA

### Código
- Pantallas en Jetpack Compose, ViewModels, repositorios y servicios Retrofit a partir del contrato `api/openapi.yaml`.
- Caché de lectura con Room, pantallas de carga con *skeletons*, integración con Google Calendar y preparación del inicio de sesión con Google.
- Pantalla de Configuración y pantallas de edición de perfil, preferencias y notificaciones.
- Pruebas unitarias y el flujo de integración continua.

### Diseño
- Propuestas de iconos (varias rondas) y de tipografía; las personas del equipo eligieron entre las opciones y pidieron los cambios.
- La identidad visual (colores, tono motivador) la definió el equipo; la IA la respetó y no modificó el diseño de las pantallas sin que se pidiera.

### Configuración
- Pasos de configuración de Google Cloud (API de Calendar, pantalla de consentimiento, clientes OAuth), hechos con la cuenta y la autorización de la persona responsable.

## 3. Cómo se trabajó

1. La persona responsable de Android describía lo que necesitaba y las restricciones (por ejemplo, "no cambiar el diseño de las pantallas").
2. La IA proponía o escribía el código y explicaba los cambios.
3. La persona compilaba y probaba la app en Android Studio y reportaba los errores. Con esos errores se corregía el código.
4. Los cambios se guardan en Git, en ramas de trabajo, y se integran mediante Pull Requests.

## 4. Qué se revisó y qué límites tiene

- **El código generado lo compila y prueba una persona del equipo** en Android Studio y en su dispositivo. La IA no ejecutó la app.
- Las pruebas unitarias automáticas cubren la lógica pura (parsers, filtros, mapeos). No cubren pantallas ni la conexión real con el backend.
- El contrato de la API es la fuente de verdad. Donde el contrato es ambiguo (por ejemplo, `/progress` y `/progress/weekly` no tienen esquema), la app lee los datos de forma tolerante y lo deja anotado para acordarlo con el backend.
- Los textos legales de Términos y Privacidad son **provisionales** y deben ser revisados por el equipo antes de publicar la app.
- La IA puede equivocarse o inventar detalles. Por eso las versiones de librerías, los endpoints y los valores de configuración se comprobaron contra la documentación o contra la propia compilación.

## 5. Datos y privacidad

- No se enviaron a ninguna herramienta de IA contraseñas, tokens, llaves secretas ni datos personales de usuarios reales.
- `local.properties` (URLs, ID de cliente de Google) no se sube a Git.
- El SHA-1 de la llave de depuración que se registró en Google Cloud es público por diseño (identifica la app, no la protege).

## 6. Uso de IA dentro del producto (plan)

Lumi generará, a partir de los materiales que sube la persona, **lecciones, tarjetas de estudio y cuestionarios** con Gemini.

- **Dónde corre:** en el backend, no en la app. La llave de la API de Gemini nunca debe estar dentro del APK.
- **Modelos:** Gemini Flash o Flash-Lite desde Google AI Studio (nivel gratuito). Hay que confirmar con el equipo los límites vigentes del nivel gratuito antes de depender de ellos.
- **Flujo previsto:** la persona sube un material → el backend lo procesa con Gemini → el material pasa al estado `completed` con su contenido → la app lo muestra en Estudio (tarjetas, cuestionario, lecciones). La app ya consume ese resultado mediante los endpoints de materiales y lecciones.
- **Riesgos a cubrir cuando se implemente:** contenido incorrecto generado por el modelo (mostrar que fue generado por IA y permitir reportarlo), límites de uso y costos, y no enviar a Gemini datos que no sean el material de estudio.

## 7. Reflexión

La IA aceleró la parte repetitiva (estructura de pantallas, repositorios, documentación) y permitió probar varias opciones de diseño rápidamente. Lo que sigue siendo responsabilidad de las personas es decidir qué se construye, revisar que funcione, probar en dispositivos reales y asegurar que el contenido educativo sea correcto.
