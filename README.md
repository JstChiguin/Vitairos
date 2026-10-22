# VITAIROS – Prototipo móvil (DSY1105)

Aplicación Android desarrollada con Kotlin y Jetpack Compose para la captura y gestión de reportes operacionales.

El flujo contempla:

Ingreso simulado → Nuevo reporte → Adjuntos (cámara, galería y PDF) → Revisión → Cola de envío → Sincronización simulada → Historial y trazabilidad.

La sincronización contempla reintentos automáticos a los 5, 15 y 30 segundos.

**Alcance actual:** prototipo académico con datos ficticios. No existe conexión a un backend real. La información se almacena localmente en el espacio privado de la aplicación, en una base de datos SQLite.

## Abrir y compilar

**Android Studio**

1. Seleccionar `File → Open`.
2. Abrir la carpeta `Vitairos`.
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
- Room 2.8.4 (KSP 2.3.12)

## Estructura del proyecto

La app sigue el patrón **MVVM**: las pantallas (View) muestran el estado del `AppViewModel` (ViewModel), que usa el repositorio y la base de datos (Model).

```
com.example.vitairosapp
├── MainActivity.kt
├── data
│   ├── model        Report, Attachment, ReportStatus, AttachmentSource, LogEntry, SyncResult, ImportResult
│   ├── catalog      Catalogs (tipos, procesos, turnos y usuarios demo)
│   ├── local        AppPreferences, AttachmentStore (archivos adjuntos), ReportJson (serialización)
│   │   └── db       VitairosDatabase (Room/SQLite), ReportDao, entity/ (tablas), ReportMappers
│   ├── remote       MockSyncApi (envío simulado)
│   └── repository   ReportRepository (lee y escribe en la base SQLite)
├── util             BitmapUtils, PdfUtils, Format (fechas y tamaños)
└── ui
    ├── VitairosApp.kt   Contenedor principal (Scaffold, navegación entre pantallas)
    ├── navigation       Screen, Tab
    ├── viewmodel        AppViewModel (estado, guardado, cola y sincronización), Confirmation
    ├── components       Componentes reutilizables (botones, tarjetas, avisos, adjuntos, cabecera, barra inferior…)
    ├── screens
    │   ├── splash       SplashScreen (pantalla de carga al abrir la app)
    │   ├── login        LoginScreen
    │   ├── home         HomeScreen
    │   ├── report       ReportFormScreen, AttachmentsScreen, ReviewScreen
    │   ├── queue        QueueScreen
    │   ├── history      HistoryScreen
    │   ├── detail       DetailScreen
    │   └── settings     SettingsScreen
    └── theme            Color, Theme, Type
```

## Almacenamiento local (SQLite)

Los datos se guardan en una base SQLite con **Room** (`databases/vitairos.db`, en el almacenamiento
privado de la app):

| Tabla | Contenido |
|---|---|
| `reports` | Datos de cada reporte: tipo, proceso, área, turno, observaciones, estado, intentos, versión, comprobante |
| `attachments` | Metadatos de cada adjunto (nombre, tamaño, SHA-256, ruta del archivo). Clave foránea a `reports` con borrado en cascada |
| `log_entries` | Eventos de trazabilidad de cada reporte. Clave foránea a `reports` con borrado en cascada |
| `draft` | El reporte que se está creando o editando (una fila, en JSON), para recuperarlo si se cierra la app |

- Los archivos de los adjuntos (fotos optimizadas y PDF) quedan en `files/attachments/<idReporte>/`;
  la base solo guarda su ruta. Tras sincronizar un reporte se borran los archivos y se conserva la metadata.
- La sesión simulada y los controles de la demo se guardan en SharedPreferences (`vitairos_prefs`).
- Si la app tenía datos de la versión anterior (`reports.json` / `draft.json`), se migran solos a la base
  la primera vez que se abre; el JSON queda como respaldo en `reports.migrated.json`.

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
