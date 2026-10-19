# VITAIROS – Prototipo móvil (DSY1105)

App Android (Kotlin + Jetpack Compose) con el flujo principal de captura de reportes operacionales:
ingreso simulado → nuevo reporte → adjuntos (cámara, galería, PDF) → revisión → cola de envío
con sincronización simulada (reintentos automáticos de 5, 15 y 30 s) → historial y trazabilidad.
Todo es ficticio: no hay backend real, los datos quedan en el almacenamiento privado de la app.

## Abrir y compilar

- Android Studio: *File → Open* y elegir esta carpeta (`VitairosApp`). Ejecutar el módulo `app`.
- Línea de comandos: `gradlew assembleDebug` → `app/build/outputs/apk/debug/app-debug.apk`.

Requisitos: minSdk 26, compileSdk/targetSdk 37, AGP 9.4.1, Compose BOM 2026.02.01.

## Estructura

- `data/` – modelos, repositorio JSON (`reports.json`, `draft.json`), adjuntos (`AttachmentStore`),
  preferencias y API de envío simulada (`MockSyncApi`).
- `ui/AppViewModel.kt` – estado de la app, navegación, guardado, cola y sincronización.
- `ui/screens/` – pantallas (`MainScreens.kt`: ingreso, inicio, cola, historial, detalle, ajustes;
  `ReportFlowScreens.kt`: formulario, adjuntos, revisión).
- `ui/components/` – componentes visuales reutilizables.
