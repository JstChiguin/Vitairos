# VITAIROS – Prototipo móvil (DSY1105)

**Proyecto:** VITAIROS Industrial AI  
**Asignatura:** Desarrollo de Aplicaciones Móviles (DSY1105)  
**Institución:** Duoc UC  
**Equipo:** 1  
**Integrantes:** Chiguin Chiang y Lizandro Godoy  
**Tecnologías:** Kotlin, Jetpack Compose, Room y SQLite

## 1. Descripción del proyecto

VITAIROS es una aplicación Android desarrollada para la captura y gestión de reportes operacionales en terreno.

El prototipo permite registrar información, incorporar archivos adjuntos y consultar los reportes almacenados en el dispositivo, incluso cuando no existe conexión a internet.

El proyecto contempla una aplicación Android nativa y un prototipo HTML navegable utilizado como apoyo durante el desarrollo y la presentación académica.

**Alcance:** prototipo académico con datos ficticios. No existe conexión a un backend productivo ni autenticación corporativa real.

## 2. Funcionalidades implementadas

- Ingreso mediante selección de usuario de demostración.
- Pantalla principal con resumen de reportes y estados.
- Navegación entre Inicio, Nuevo reporte, Cola, Historial y Ajustes.
- Formulario de creación de reportes operacionales.
- Selección de tipo de reporte y proceso asociado.
- Registro de área, turno y observaciones.
- Validación de campos obligatorios.
- Captura de fotografías mediante la cámara.
- Selección de imágenes desde la galería.
- Selección de documentos PDF.
- Vista previa y gestión de archivos adjuntos.
- Revisión de los datos antes de confirmar el reporte.
- Almacenamiento local mediante Room y SQLite.
- Gestión de borradores y reportes pendientes.
- Cola de envío y sincronización simulada.
- Historial y consulta de estados.
- Animación de inicio y mensajes de retroalimentación.

### Flujo principal

Ingreso simulado → Nuevo reporte → Datos generales → Adjuntos → Revisión → Guardado local → Cola de envío → Sincronización simulada → Historial.

### Sincronización

La sincronización utiliza una API simulada (`MockSyncApi`), sin comunicación con un servidor productivo.

El mecanismo contempla reintentos automáticos con intervalos de 5, 15 y 30 segundos.

Los estados permiten identificar reportes en borrador, pendientes, en proceso de envío, sincronizados o con error.

## 3. Requisitos técnicos

| Componente | Versión o tecnología |
|---|---|
| Lenguaje | Kotlin |
| Interfaz | Jetpack Compose |
| Arquitectura | MVVM + Repository |
| Persistencia | Room 2.8.4 / SQLite |
| Procesamiento de Room | KSP 2.3.12 |
| minSdk | 26 (Android 8.0) |
| compileSdk | 37 |
| targetSdk | 37 |
| Android Gradle Plugin | 9.4.1 |
| Compose BOM | 2026.02.01 |

Las versiones corresponden a la configuración declarada en el proyecto.

## 4. Abrir y ejecutar la aplicación

### Desde Android Studio

1. Descargar o clonar el repositorio.
2. Abrir Android Studio.
3. Seleccionar **File → Open**.
4. Abrir la carpeta raíz del proyecto `Vitairos`.
5. Esperar a que finalice la sincronización de Gradle.
6. Seleccionar un emulador Android compatible o conectar un dispositivo físico.
7. Ejecutar el módulo `app` mediante **Run**.

Se recomienda utilizar una versión de Android Studio compatible con Android Gradle Plugin 9.4.1.

### Compilar mediante Gradle

Desde la carpeta raíz del proyecto, ejecutar:

```bash
./gradlew assembleDebug
```

En Windows:

```powershell
.\gradlew.bat assembleDebug
```

El APK de depuración se genera en:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 5. Arquitectura del proyecto

VITAIROS utiliza el patrón **Model-View-ViewModel (MVVM)** para separar la interfaz de usuario, la lógica de presentación y el acceso a los datos.

- **View:** pantallas y componentes desarrollados con Jetpack Compose.
- **ViewModel:** administra el estado de la aplicación, las validaciones y las acciones del usuario.
- **Model y Repository:** representan la información y coordinan las operaciones de almacenamiento.
- **Room/SQLite:** permiten guardar y recuperar reportes localmente.

### Estructura de carpetas

```text
com.example.vitairosapp
│
├── MainActivity.kt
│
├── data
│   ├── model
│   │   ├── Report
│   │   ├── Attachment
│   │   ├── ReportStatus
│   │   ├── AttachmentSource
│   │   ├── LogEntry
│   │   ├── SyncResult
│   │   └── ImportResult
│   │
│   ├── catalog
│   │   └── Catalogs
│   │
│   ├── local
│   │   ├── AppPreferences
│   │   ├── AttachmentStore
│   │   ├── ReportJson
│   │   └── db
│   │       ├── VitairosDatabase
│   │       ├── ReportDao
│   │       ├── ReportMappers
│   │       └── entity
│   │
│   ├── remote
│   │   └── MockSyncApi
│   │
│   └── repository
│       └── ReportRepository
│
├── util
│   ├── BitmapUtils
│   ├── PdfUtils
│   └── Format
│
└── ui
    ├── VitairosApp.kt
    ├── navigation
    │   ├── Screen
    │   └── Tab
    │
    ├── viewmodel
    │   ├── AppViewModel
    │   └── Confirmation
    │
    ├── components
    │
    ├── screens
    │   ├── splash
    │   ├── login
    │   ├── home
    │   ├── report
    │   ├── queue
    │   ├── history
    │   ├── detail
    │   └── settings
    │
    └── theme
```

## 6. Validaciones y gestión de estado

Las validaciones del formulario se gestionan desde `AppViewModel.kt`.

Por ejemplo, antes de avanzar desde los datos generales hacia los adjuntos, el método `goToAttachments()` comprueba que se hayan seleccionado el tipo de reporte y el proceso asociado.

Si alguno está vacío, se muestra un mensaje de advertencia y se impide avanzar.

Esto permite mantener las validaciones separadas de los componentes visuales de Jetpack Compose.

El ViewModel también administra las acciones de navegación, la información temporal del reporte y los estados que se muestran en pantalla.

## 7. Almacenamiento local con Room y SQLite

La información de los reportes se almacena en una base de datos SQLite administrada mediante Room.

La base de datos se identifica como `vitairos.db` y se encuentra en el almacenamiento privado de la aplicación.

### Tablas principales

| Tabla | Información almacenada |
|---|---|
| `reports` | Datos generales, estado, intentos y otra información del reporte |
| `attachments` | Metadatos y referencias de los archivos adjuntos |
| `log_entries` | Eventos utilizados para la trazabilidad |
| `draft` | Información temporal del reporte en creación o edición |

### Archivos adjuntos

Las fotografías y documentos PDF se almacenan en el espacio privado de la aplicación.

Los archivos se organizan bajo una estructura similar a:

```text
files/attachments/<idReporte>/
```

La base de datos conserva las referencias y los metadatos correspondientes.

Las fotografías pueden optimizarse antes de ser almacenadas para reducir su tamaño.

### Persistencia y funcionamiento sin conexión

El almacenamiento local permite:

- Guardar reportes sin depender de internet.
- Mantener reportes pendientes en la cola.
- Recuperar información al volver a abrir la aplicación.
- Consultar estados e historial.
- Conservar borradores durante el proceso de registro.

La sincronización es simulada y no envía información a un servidor empresarial.

### Migración desde JSON

El proyecto contempla la migración de información almacenada anteriormente en archivos JSON, como `reports.json` y `draft.json`, hacia la base de datos Room.

El archivo `reports.migrated.json` se utiliza como respaldo durante este proceso.

Las preferencias de la sesión simulada y los controles de demostración se administran mediante SharedPreferences.

## 8. Recursos nativos del dispositivo

La aplicación incorpora recursos de Android para adjuntar documentos a los reportes.

### Cámara

Permite capturar una fotografía y asociarla al reporte.

Durante las pruebas se verificó que la imagen capturada apareciera en el listado de adjuntos con información sobre su tamaño y resolución.

### Galería

Permite seleccionar imágenes existentes en el dispositivo.

### Documentos PDF

Permite seleccionar archivos PDF para incorporarlos al reporte.

### Gestión de adjuntos

El prototipo contempla un máximo de cinco archivos por reporte y un tamaño máximo de 10 MB por archivo.

Los adjuntos pueden visualizarse o eliminarse antes de confirmar el registro.

## 9. Animaciones e interfaz

La interfaz está desarrollada mediante Jetpack Compose y utiliza componentes reutilizables para mantener consistencia visual.

La pantalla inicial se implementa en `SplashScreen.kt` y utiliza:

- `Animatable`
- `LaunchedEffect`
- `animateTo`
- `graphicsLayer`

La animación de aparición tiene una duración configurada de 700 milisegundos.

La aplicación también presenta indicadores de progreso, mensajes de validación y estados de los reportes para informar al usuario sobre las acciones realizadas.

## 10. Prototipo HTML navegable

El repositorio incluye un prototipo HTML que permite revisar visualmente el flujo principal de VITAIROS.

**Ubicación:**

```text
prototipo/index.html
```

**Ejecución:**

Abrir el archivo `index.html` en un navegador web.

El prototipo HTML es independiente de la aplicación Android y no reemplaza su implementación nativa.

**Estado:** presentado durante la evaluación académica. Pendiente de validación formal por parte de VITAIROS Industrial AI.

## 11. Pruebas realizadas

Durante el desarrollo y la preparación de la evaluación se realizaron pruebas en un emulador Android y en un dispositivo físico.

| Prueba | Resultado |
|---|---|
| Ingreso mediante usuario de demostración | Funcional |
| Navegación entre pantallas | Funcional |
| Creación de reportes | Funcional |
| Validación de campos obligatorios | Funcional |
| Captura fotográfica | Funcional |
| Selección de imágenes desde galería | Funcional |
| Selección de archivos PDF | Funcional |
| Guardado local de reportes | Funcional |
| Recuperación de información al reabrir | Funcional |
| Cola de envío sin conexión simulada | Funcional |
| Historial de reportes | Funcional |
| Animación de inicio | Funcional |

Las pruebas corresponden al alcance académico del prototipo y no constituyen una validación industrial o productiva.

## 12. Documentación del proyecto

La documentación elaborada para el proyecto incluye:

- Informe de evidencias del prototipo Android.
- Especificación de Requisitos de Software (ERS).
- Matriz de requerimientos funcionales y no funcionales.
- Diagrama de arquitectura MVVM.
- Registro de tareas y avances en Trello.

La ERS y la matriz conservan los requisitos pendientes de aprobación formal por parte de VITAIROS, diferenciándolos de las funcionalidades implementadas y probadas académicamente.

## 13. Gestión del proyecto con Trello

El equipo utilizó Trello para organizar y dar seguimiento a las actividades mediante un tablero Kanban.

El tablero contempla las siguientes listas:

- Backlog.
- Por hacer.
- En desarrollo.
- En pruebas.
- Terminado.
- Bloqueado.

Las actividades académicas principales se encuentran registradas en la lista Terminado, con sus respectivos checklists, comentarios y responsables.

**Tablero del proyecto:**

https://trello.com/b/aMtA47Sm/vitairos-industrial-ai-dsy1105

## 14. Control de versiones con Git y GitHub

El código fuente se administra mediante Git y GitHub.

Se utilizó una organización por ramas:

| Rama | Función |
|---|---|
| `main` | Versión principal del proyecto |
| `develop` | Integración de funcionalidades |
| `feature/*` | Desarrollo de funcionalidades específicas |

Los cambios se registraron mediante commits y se integraron utilizando Pull Requests.

**Repositorio del proyecto:**

https://github.com/JstChiguin/Vitairos

**Pull Request #1 — Prototipo HTML:**

https://github.com/JstChiguin/Vitairos/pull/1

El historial del repositorio permite revisar las modificaciones y la participación de los integrantes.

## 15. Estado actual del proyecto

**Estado académico:** prototipo Android implementado, probado y presentado durante las Evaluaciones Parciales 2 y 3 de Desarrollo de Aplicaciones Móviles.

Se completó el flujo principal de ingreso, creación de reportes, adjuntos, revisión, almacenamiento local, cola de sincronización simulada e historial.

El desarrollo se organizó mediante arquitectura MVVM y persistencia local con Room/SQLite.

**Limitaciones actuales:**

- El ingreso utiliza usuarios ficticios.
- La sincronización se realiza mediante una API simulada.
- No existe integración con un backend productivo.
- Los catálogos y requisitos definitivos requieren validación por parte de VITAIROS.
- No se contempla procesamiento de inteligencia artificial productivo en esta etapa.

**Próximos pasos:**

- Revisar observaciones de la evaluación académica.
- Completar las validaciones pendientes con VITAIROS.
- Ampliar las pruebas de funcionamiento.
- Evaluar futuras integraciones según los requisitos aprobados.

---

**VITAIROS Industrial AI**  
**Equipo 1 — Chiguin Chiang y Lizandro Godoy**  
**Duoc UC · Desarrollo de Aplicaciones Móviles (DSY1105)**
