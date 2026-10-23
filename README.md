# VITAIROS – Prototipo móvil (DSY1105)

**Proyecto:** VITAIROS Industrial AI  
**Asignatura:** Desarrollo de Aplicaciones Móviles (DSY1105)  
**Institución:** Duoc UC  
**Equipo:** 1  
**Integrantes:** Chiguin Chiang y Lizandro Godoy  
**Tecnologías:** Kotlin, Jetpack Compose, Room y SQLite

## 1. Descripción del proyecto

VITAIROS es un prototipo de aplicación Android desarrollado para la captura y gestión de reportes operacionales en terreno.

Permite registrar información, incorporar fotografías y documentos PDF, almacenar reportes localmente y consultar su historial. Su funcionamiento contempla situaciones en las que no existe conexión a internet.

El proyecto incluye una aplicación Android nativa y un prototipo HTML navegable como apoyo al desarrollo y la presentación académica.

**Alcance:** prototipo académico con usuarios y datos ficticios. No existe conexión con un backend productivo ni autenticación corporativa real.

## 2. Funcionalidades implementadas

- Ingreso mediante usuarios de demostración.
- Pantalla principal con resumen de reportes.
- Navegación entre las secciones de la aplicación.
- Creación y edición de reportes operacionales.
- Selección de tipo de reporte y proceso asociado.
- Registro de área, turno y observaciones.
- Validación de campos obligatorios.
- Captura de fotografías mediante la cámara.
- Selección de imágenes desde la galería.
- Selección de documentos PDF.
- Gestión de archivos adjuntos.
- Revisión de la información antes de confirmar un reporte.
- Almacenamiento local mediante Room/SQLite.
- Gestión de borradores y reportes pendientes.
- Cola de envío y sincronización simulada.
- Historial y trazabilidad de reportes.
- Animación de inicio y mensajes de retroalimentación.

### Flujo principal

Ingreso simulado → Nuevo reporte → Datos generales → Adjuntos → Revisión → Guardado local → Cola de envío → Sincronización simulada → Historial.

## 3. Tecnologías utilizadas

| Componente | Tecnología |
|---|---|
| Lenguaje | Kotlin |
| Interfaz | Jetpack Compose |
| Arquitectura | MVVM |
| Persistencia | Room / SQLite |
| Almacenamiento de adjuntos | Archivos privados de Android |
| Preferencias | SharedPreferences |
| Sincronización | MockSyncApi |
| Control de versiones | Git y GitHub |
| Gestión del proyecto | Trello |

### Configuración Android

- `minSdk`: 26
- `compileSdk`: 37
- `targetSdk`: 37

Las versiones específicas de las dependencias se encuentran en los archivos de configuración Gradle.

## 4. Instalación y ejecución

### Desde Android Studio

1. Clonar o descargar el repositorio.
2. Abrir Android Studio.
3. Seleccionar **File → Open**.
4. Abrir la carpeta raíz del proyecto VITAIROS.
5. Esperar la sincronización de Gradle.
6. Seleccionar un emulador compatible o conectar un dispositivo Android.
7. Ejecutar el módulo `app`.

### Compilación por terminal

En Windows:

```powershell
.\gradlew.bat assembleDebug
```

En Linux o macOS:

```bash
./gradlew assembleDebug
```

El APK de depuración se genera en:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 5. Arquitectura MVVM

La aplicación utiliza el patrón Model-View-ViewModel (MVVM) para separar la interfaz de usuario, la lógica de presentación y el acceso a los datos.

**View:** las pantallas están desarrolladas con Jetpack Compose y muestran la información administrada por el ViewModel.

**ViewModel:** `AppViewModel` coordina el estado de la aplicación, las validaciones, la navegación y las acciones relacionadas con los reportes.

**Model y Repository:** los modelos representan los datos del sistema y `ReportRepository` coordina las operaciones de almacenamiento y recuperación.

**Persistencia:** Room permite trabajar con una base SQLite local.

### Estructura principal

```text
com.example.vitairosapp
│
├── MainActivity.kt
│
├── data/
│   ├── model/
│   ├── catalog/
│   ├── local/
│   │   ├── db/
│   │   │   ├── VitairosDatabase.kt
│   │   │   ├── ReportDao.kt
│   │   │   └── entity/
│   │   └── AttachmentStore.kt
│   ├── remote/
│   │   └── MockSyncApi.kt
│   └── repository/
│       └── ReportRepository.kt
│
├── util/
│
└── ui/
    ├── VitairosApp.kt
    ├── navigation/
    ├── viewmodel/
    │   └── AppViewModel.kt
    ├── components/
    ├── screens/
    │   ├── splash/
    │   ├── login/
    │   ├── home/
    │   ├── report/
    │   ├── queue/
    │   ├── history/
    │   ├── detail/
    │   └── settings/
    └── theme/
```

## 6. Almacenamiento local

La aplicación utiliza Room/SQLite como mecanismo principal de persistencia.

La base de datos `vitairos.db` contempla las siguientes entidades:

| Entidad | Función |
|---|---|
| `ReportEntity` | Almacena la información de los reportes |
| `AttachmentEntity` | Registra los metadatos y referencias de los archivos adjuntos |
| `LogEntryEntity` | Registra eventos de trazabilidad |
| `DraftEntity` | Conserva información del reporte en elaboración |

Los archivos adjuntos se almacenan por separado en el espacio privado de Android. La base de datos mantiene las referencias necesarias para asociarlos a sus reportes.

Este mecanismo permite recuperar información al volver a abrir la aplicación y mantener reportes pendientes sin depender de un servidor.

## 7. Validaciones

Las validaciones del formulario se gestionan desde el ViewModel, separadas de los componentes visuales.

Antes de avanzar hacia la pantalla de adjuntos, se comprueba que el usuario haya seleccionado el tipo de reporte y el proceso asociado.

Si falta alguno de estos datos, se muestra el mensaje:

> Selecciona tipo de reporte y proceso asociado.

El usuario debe completar los campos requeridos antes de continuar.

## 8. Recursos nativos de Android

La aplicación incorpora funcionalidades del dispositivo para adjuntar archivos a los reportes.

**Cámara:** permite capturar fotografías y asociarlas al reporte.

**Galería:** permite seleccionar imágenes existentes en el dispositivo.

**Documentos PDF:** permite seleccionar documentos mediante el selector de archivos de Android.

Los adjuntos seleccionados pueden revisarse antes de confirmar el reporte.

## 9. Funcionamiento sin conexión y sincronización

El prototipo permite almacenar información localmente y mantener reportes pendientes en una cola de envío.

La sincronización se representa mediante `MockSyncApi`, sin comunicación con un servidor empresarial.

El mecanismo contempla un intento inicial y hasta tres reintentos con intervalos de 5, 15 y 30 segundos.

**Importante:** la sincronización es simulada y no transfiere información a un backend productivo.

## 10. Interfaz y animaciones

La interfaz fue desarrollada con Jetpack Compose y componentes reutilizables.

La pantalla de inicio utiliza una animación de aparición implementada mediante `Animatable`, `LaunchedEffect` y `animateTo`, con una duración configurada de 700 milisegundos.

También se incorporan mensajes de validación e indicadores visuales para informar al usuario sobre las acciones realizadas.

## 11. Prototipo HTML navegable

El repositorio incluye un prototipo HTML para revisar visualmente el flujo principal de VITAIROS.

**Ubicación:**

```text
prototipo/index.html
```

Para ejecutarlo, abrir `index.html` en un navegador web.

El prototipo HTML es independiente de la aplicación Android y no reemplaza su implementación nativa.

**Estado:** presentado académicamente y pendiente de validación formal por parte de VITAIROS Industrial AI.

## 12. Pruebas realizadas

Durante el desarrollo y la preparación de la defensa se realizaron verificaciones de las funcionalidades principales.

| Funcionalidad | Verificación académica |
|---|---|
| Ingreso simulado | Realizada |
| Navegación entre pantallas | Realizada |
| Creación de reportes | Realizada |
| Validación de campos obligatorios | Realizada |
| Captura de fotografías | Realizada |
| Selección de adjuntos | Realizada |
| Almacenamiento local | Realizada |
| Cola de envío sin conexión simulada | Realizada |
| Historial de reportes | Realizada |
| Animación de inicio | Realizada |

Las pruebas corresponden al alcance académico y no constituyen una certificación de funcionamiento en un entorno productivo.

## 13. Documentación técnica

La carpeta `documentacion/` contiene los antecedentes y evidencias del proyecto, incluyendo:

- Arquitectura MVVM.
- Descripción del almacenamiento local.
- Navegación y pantallas.
- Plan de pruebas y evidencias.
- Especificación de Requisitos de Software (ERS).
- Revisión de requisitos funcionales y no funcionales.
- Informe del prototipo Android.
- Matriz de requerimientos.
- Diagrama de arquitectura MVVM.

La documentación académica no equivale a la aprobación formal de los requisitos por parte de VITAIROS Industrial AI.

## 14. Gestión del proyecto

El equipo utilizó Trello para organizar las actividades, distribuir responsabilidades y registrar avances mediante un tablero Kanban.

Se documentaron las tareas de levantamiento de requerimientos, prototipo HTML, implementación Android y documentación técnica.

**Tablero Trello:**

https://trello.com/b/aMtA47Sm/vitairos-industrial-ai-dsy1105

## 15. Control de versiones

El desarrollo se administra mediante Git y GitHub.

El flujo de trabajo contempla:

- `main`: rama principal.
- `develop`: integración de funcionalidades.
- `feature/*`: desarrollo de cambios específicos.

Los avances se registran mediante commits y Pull Requests.

**Repositorio GitHub:**

https://github.com/JstChiguin/Vitairos

**Pull Request #1 — Prototipo HTML:**

https://github.com/JstChiguin/Vitairos/pull/1

**Pull Request #2 — Documentación técnica:**

https://github.com/JstChiguin/Vitairos/pull/2

## 16. Estado del proyecto

**Estado académico:** prototipo Android desarrollado y presentado durante las Evaluaciones Parciales 2 y 3 de la asignatura Desarrollo de Aplicaciones Móviles (DSY1105).

Se implementó el flujo principal de registro de reportes, incorporación de adjuntos, validaciones, almacenamiento local, cola de sincronización simulada e historial.

### Limitaciones actuales

- La autenticación utiliza usuarios ficticios.
- La sincronización es simulada.
- No existe integración con servicios empresariales reales.
- Los requisitos definitivos requieren validación por parte de VITAIROS.
- No se implementan servicios productivos de inteligencia artificial en esta etapa.

### Trabajo pendiente

- Revisar las observaciones de la evaluación.
- Completar las validaciones pendientes con VITAIROS.
- Ampliar las pruebas.
- Evaluar futuras integraciones según los requisitos aprobados.

---

**VITAIROS Industrial AI**  
**Equipo 1 — Chiguin Chiang y Lizandro Godoy**  
**Duoc UC — Desarrollo de Aplicaciones Móviles (DSY1105)**