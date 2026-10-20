# VITAIROS – Prototipo móvil (DSY1105)

Aplicación Android desarrollada con Kotlin y Jetpack Compose para la captura y gestión de reportes operacionales.

El flujo contempla:

Ingreso simulado → Nuevo reporte → Adjuntos (cámara, galería y PDF) → Revisión → Cola de envío → Sincronización simulada → Historial y trazabilidad.

La sincronización contempla reintentos automáticos a los 5, 15 y 30 segundos.

**Alcance actual:** prototipo académico con datos ficticios. No existe conexión a un backend real. La información se almacena localmente en el espacio privado de la aplicación.

## Abrir y compilar

**Android Studio**

1. Seleccionar `File → Open`.
2. Abrir la carpeta `VitairosApp`.
3. Ejecutar el módulo `app`.

**Línea de comandos**

```bash
gradlew assembleDebug
```

APK generado:

`app/build/outputs/apk/debug/app-debug.apk`

**Requisitos técnicos declarados:**

- minSdk 26
- compileSdk / targetSdk 37
- Android Gradle Plugin 9.4.1
- Compose BOM 2026.02.01

## Estructura del proyecto

- `data/`: modelos, repositorio JSON (`reports.json`, `draft.json`), adjuntos (`AttachmentStore`), preferencias y API simulada (`MockSyncApi`).
- `ui/AppViewModel.kt`: estado de la aplicación, navegación, guardado, cola y sincronización.
- `ui/screens/`: pantallas de ingreso, inicio, cola, historial, detalle, ajustes, formulario, adjuntos y revisión.
- `ui/components/`: componentes visuales reutilizables.

## Prototipo HTML navegable

Se incluye un prototipo HTML para demostrar visualmente el flujo de la aplicación y facilitar su revisión.

**Ubicación:** `prototipo/index.html`

**Ejecución:** abrir `index.html` en un navegador web.

Este prototipo es independiente de la aplicación Android y no reemplaza su implementación nativa.

**Estado:** en pruebas, pendiente de validación por VITAIROS.

## Gestión del proyecto

El desarrollo se organiza mediante un tablero Kanban en Trello, utilizado para:

- Seguimiento de tareas y requerimientos.
- Control de funcionalidades en desarrollo.
- Registro de pruebas y validaciones.
- Seguimiento de avances e integración de cambios en GitHub.

**[Ver tablero Trello — VITAIROS Industrial AI](https://trello.com/b/aMtA47Sm/vitairos-industrial-ai-dsy1105)**

## Control de versiones

Se utiliza Git y GitHub para gestionar el código fuente y registrar los avances del proyecto.

- `main`: rama principal.
- `develop`: integración de funcionalidades.
- `feature/*`: desarrollo de funcionalidades específicas.

Los cambios se integran mediante Pull Requests.

**[Pull Request #1 — Prototipo HTML](https://github.com/JstChiguin/Vitairos/pull/1)**

## Estado del proyecto

Proyecto académico en desarrollo para la asignatura **Desarrollo de Aplicaciones Móviles (DSY1105)**.

La implementación Android y sus funcionalidades deben verificarse mediante pruebas en Android Studio. La validación del prototipo por parte de VITAIROS permanece pendiente.
