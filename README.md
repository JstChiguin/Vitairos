# VITAIROS – Prototipo móvil (DSY1105)

App Android (Kotlin + Jetpack Compose) con el flujo principal de captura de reportes operacionales:
ingreso simulado → nuevo reporte → adjuntos (cámara, galería, PDF) → revisión → cola de envío
con sincronización simulada (reintentos automáticos de 5, 15 y 30 s) → historial y trazabilidad.
Todo es ficticio: no hay backend real, los datos quedan en el almacenamiento privado de la app.

## Abrir y compilar

- Android Studio: *File → Open* y elegir esta carpeta (`Vitairos`). Ejecutar el módulo `app`.
- Línea de comandos: `gradlew assembleDebug` → `app/build/outputs/apk/debug/app-debug.apk`.

Requisitos: minSdk 26, compileSdk/targetSdk 37, AGP 9.4.1, Compose BOM 2026.02.01, Room 2.8.4 (KSP 2.3.12).

## Estructura

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
