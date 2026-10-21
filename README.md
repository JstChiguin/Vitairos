# VITAIROS – Prototipo móvil (DSY1105)

Aplicación Android académica para captura y gestión de reportes operacionales, con Kotlin y Jetpack Compose.

**Integrantes:** Chiguin Chiang y Lizandro Godoy.

## Alcance

Ingreso ficticio → nuevo reporte → adjuntos (cámara, galería, PDF) → revisión → cola → sincronización simulada → historial y trazabilidad. No hay backend productivo ni datos reales.

## Abrir y compilar

Abrir el repositorio en Android Studio y ejecutar el módulo `app`. Para compilar por terminal en Windows: `gradlew.bat assembleDebug`. APK esperado: `app/build/outputs/apk/debug/app-debug.apk`.

Configuración declarada en `app/build.gradle.kts`: `minSdk 26`, `compileSdk 37`, `targetSdk 37`. La compilación no ha sido verificada en esta revisión documental.

## Arquitectura y persistencia actual

- Presentación: Jetpack Compose (`ui/VitairosApp.kt`, `ui/screens/`).
- Estado y flujo: `ui/AppViewModel.kt` (MVVM simplificado).
- Reportes y borrador: JSON privado (`data/reports.json`, `data/draft.json`).
- Adjuntos: almacenamiento privado `filesDir/attachments/`.
- Preferencias: `SharedPreferences`.
- Sincronización: `MockSyncApi`, sin servidor real. Intento inicial y hasta tres reintentos con esperas de 5, 15 y 30 segundos.

**Nota:** Room/SQLite se mencionan como alternativa recomendada en la ERS, pero no aparecen implementados en el código revisado.

## Prototipo HTML

`prototipo/index.html` se abre en un navegador y sirve para revisar el flujo visual. No reemplaza la app Android. Estado: en pruebas, pendiente de validación por VITAIROS.

## Gestión del proyecto

- [Tablero Kanban en Trello](https://trello.com/b/aMtA47Sm/vitairos-industrial-ai-dsy1105) (acceso según permisos).
- [Pull Request #1 – prototipo HTML](https://github.com/JstChiguin/Vitairos/pull/1).

Flujo Git: `feature/*` → `develop` → `main`, con Pull Requests.

## Documentación

Consultar la carpeta `documentacion/` para ERS, arquitectura MVVM, persistencia, navegación, revisión de requisitos y plan de pruebas. Los documentos son preliminares; no equivalen a aprobación de la empresa ni a pruebas ejecutadas.
