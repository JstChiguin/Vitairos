# VITAIROS – Prototipo móvil (DSY1105)

**Proyecto:** VITAIROS Industrial AI  
**Asignatura:** Desarrollo de Aplicaciones Móviles (DSY1105)  
**Institución:** Duoc UC  
**Equipo:** 1  
**Integrantes:** Chiguin Chiang y Lizandro Godoy  
**Tecnologías:** Kotlin, Jetpack Compose, Room, SQLite y JUnit

## 1. Descripción del proyecto

VITAIROS es un prototipo de aplicación Android desarrollado para la captura y gestión de reportes operacionales en terreno.

Permite registrar información, incorporar fotografías y documentos PDF, almacenar reportes localmente y consultar su historial. Su funcionamiento contempla situaciones en las que no existe conexión a internet.

El proyecto incluye una aplicación Android nativa y un prototipo HTML navegable como apoyo al desarrollo y la presentación académica.

**Alcance:** prototipo académico con usuarios y datos ficticios. No existe conexión con un backend productivo ni autenticación corporativa real. La aplicación consume dos servicios públicos gratuitos y sin clave, únicamente con fines de demostración: JSONPlaceholder (sincronización de demostración) y Open-Meteo (condiciones meteorológicas).

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
- Cola de envío con reintentos automáticos.
- Sincronización simulada (predeterminada) y sincronización de demostración contra la API REST JSONPlaceholder (opcional, desde Ajustes).
- Consulta de condiciones meteorológicas (temperatura, viento, humedad y probabilidad de lluvia) mediante la API REST Open-Meteo, para sitios ficticios seleccionables.
- Historial y trazabilidad de reportes.
- Animación de inicio y mensajes de retroalimentación.
- Pruebas automatizadas unitarias.

### Flujo principal

Ingreso simulado → Nuevo reporte → Datos generales → Adjuntos → Revisión → Guardado local → Cola de envío → Sincronización (simulada o JSONPlaceholder) → Historial.

## 3. Tecnologías utilizadas

| Componente | Tecnología |
|---|---|
| Lenguaje | Kotlin |
| Interfaz | Jetpack Compose |
| Arquitectura | MVVM |
| Persistencia | Room / SQLite |
| Almacenamiento de adjuntos | Archivos privados de Android |
| Preferencias | SharedPreferences |
| Sincronización simulada | `MockSyncApi` |
| Sincronización de demostración | `ApiRemotaSincronizacion` (JSONPlaceholder) |
| Clima | `ApiClimaOpenMeteo` (Open-Meteo) |
| Cliente HTTP | `HttpURLConnection` de la plataforma, sin bibliotecas de red adicionales |
| Pruebas | JUnit 4, `kotlinx-coroutines-test` y `org.json` (solo en pruebas) |
| Control de versiones | Git y GitHub |
| Gestión del proyecto | Trello |

### Configuración Android

- `minSdk`: 26
- `compileSdk`: 37
- `targetSdk`: 37
- `versionCode`: 1
- `versionName`: 1.0

Las versiones específicas de las dependencias se encuentran en los archivos de configuración Gradle.

La aplicación declara el permiso `INTERNET` para consultar las dos APIs. No solicita permisos de ubicación.

## 4. Instalación y ejecución

### Requisitos

- Android Studio con el SDK de Android correspondiente a `compileSdk` 37.
- JDK 25, versión indicada en `gradle/gradle-daemon-jvm.properties`. Gradle 9.6.0 se descarga mediante el wrapper incluido.
- Emulador o dispositivo con Android 8.0 (API 26) o superior.
- Conexión a internet solo para probar JSONPlaceholder y Open-Meteo.

### Desde Android Studio

1. Clonar o descargar el repositorio.
2. Abrir Android Studio.
3. Seleccionar **File → Open**.
4. Abrir la carpeta raíz del proyecto VITAIROS.
5. Esperar la sincronización de Gradle.
6. Seleccionar un emulador compatible o conectar un dispositivo Android.
7. Ejecutar el módulo `app`.

### Generación del APK de depuración

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

Es un APK de depuración firmado con la clave de depuración de Android. No se genera un APK de producción firmado.

## 5. Arquitectura MVVM

La aplicación utiliza el patrón Model-View-ViewModel (MVVM) para separar la interfaz de usuario, la lógica de presentación y el acceso a los datos.

**View:** las pantallas están desarrolladas con Jetpack Compose y muestran la información administrada por el ViewModel.

**ViewModel:** `AppViewModel` coordina el estado de la aplicación, las validaciones, la navegación y las acciones relacionadas con los reportes. `GestorClima` administra por separado el estado de la consulta meteorológica.

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
│   │   ├── Catalogs.kt
│   │   └── SitiosClima.kt
│   ├── local/
│   │   ├── db/
│   │   │   ├── VitairosDatabase.kt
│   │   │   ├── ReportDao.kt
│   │   │   └── entity/
│   │   └── AttachmentStore.kt
│   ├── remote/
│   │   ├── ApiSincronizacion.kt
│   │   ├── MockSyncApi.kt
│   │   ├── ApiRemotaSincronizacion.kt
│   │   ├── ApiClima.kt
│   │   └── ApiClimaOpenMeteo.kt
│   └── repository/
│       └── ReportRepository.kt
│
├── util/
│
└── ui/
    ├── VitairosApp.kt
    ├── navigation/
    ├── viewmodel/
    │   ├── AppViewModel.kt
    │   ├── GestorClima.kt
    │   └── EstadoClima.kt
    ├── components/
    │   └── TarjetaClima.kt
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

Las pruebas unitarias se encuentran en `app/src/test/`.

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

## 9. Funcionamiento sin conexión, sincronización y servicios externos

### 9.1 Offline-first con Room/SQLite

Todo reporte se guarda primero en la base local Room/SQLite, con o sin conexión. La red solo interviene al enviar un reporte desde la cola; la persistencia local no depende de ella.

Los reportes quedan pendientes en una cola de envío. Si la conexión no está disponible, permanecen en la cola y se envían al recuperarla. Un envío que quedó a medias al cerrar la aplicación vuelve a la cola al reabrirla.

La disponibilidad de conexión se controla desde la aplicación, mediante el interruptor **Conexión simulada** de Inicio y Ajustes, y además se detecta cuando una petición real falla por falta de red.

El mecanismo contempla un intento inicial y hasta tres reintentos automáticos con intervalos de 5, 15 y 30 segundos. Tras agotarlos, el reporte queda disponible para un reintento manual.

### 9.2 Sincronización de demostración

Existen dos implementaciones del envío, que comparten la interfaz `ApiSincronizacion`:

| Modo | Clase | Descripción |
|---|---|---|
| Simulado (predeterminado) | `MockSyncApi` | No transmite información. Espera un tiempo breve y responde con un comprobante generado localmente |
| Remoto de demostración | `ApiRemotaSincronizacion` | Envía una petición POST real a JSONPlaceholder |

El modo remoto se activa con el interruptor **Enviar a la API REST real** en Ajustes. El modo simulado sigue siendo el predeterminado.

**Sobre JSONPlaceholder** (`https://jsonplaceholder.typicode.com/posts`):

- Es un servicio público y gratuito de pruebas. Responde `201` con un identificador, pero **no almacena los reportes de forma permanente**.
- Siempre devuelve el mismo identificador (`101`), por lo que el comprobante del reporte es `JSP-101`, igual para todos los envíos.
- Solo recibe datos de clasificación ficticios: identificador del reporte, versión, tipo, proceso, turno y cantidad de adjuntos, además de un título y un `userId` fijos.
- No se envían el usuario, el identificador de instalación, las observaciones, el área, ni nombres, contenido o hashes de los adjuntos.

**Manejo de errores del modo remoto:**

| Situación | Resultado |
|---|---|
| Sin red o servidor inalcanzable | Sin conexión: el reporte queda en la cola |
| Tiempo de espera agotado (8 s) | Error 408, con reintento automático |
| Respuesta HTTP que no es 2xx | Error con el código recibido |
| Respuesta que no es JSON o sin identificador | Error 502 (respuesta inesperada) |
| Otro error de red | Error 503 |

El interruptor de error forzado de Ajustes aplica a ambos modos y no realiza ninguna petición cuando está activo.

**Importante:** la sincronización no transfiere información a un backend productivo.

### 9.3 Condiciones meteorológicas (Open-Meteo)

La pantalla de Inicio incluye una tarjeta **Condiciones meteorológicas** que consulta la API pública y gratuita Open-Meteo (`https://api.open-meteo.com/v1/forecast`) mediante una petición GET.

**Datos que consulta:**

| Dato | Variable de Open-Meteo |
|---|---|
| Temperatura | `current.temperature_2m` (°C) |
| Humedad relativa | `current.relative_humidity_2m` (%) |
| Viento | `current.wind_speed_10m` (km/h) |
| Probabilidad de lluvia | `daily.precipitation_probability_max` (%) |

**Funcionamiento:**

- Los sitios son ficticios y se seleccionan desde la tarjeta: Planta ficticia Norte (referencia Antofagasta), Centro (Santiago) y Sur (Concepción). Cada uno usa coordenadas de referencia.
- La aplicación no solicita permisos de ubicación ni lee la posición del dispositivo. La única información enviada a Open-Meteo son las coordenadas del sitio elegido.
- El sitio elegido se conserva al cerrar la aplicación.
- La consulta se realiza al abrir Inicio, al cambiar de sitio, al cambiar la conexión simulada y al pulsar **Actualizar clima**.
- Estados de la tarjeta: sin datos, consultando, datos disponibles, sin conexión y error. En los dos últimos se conserva la última lectura obtenida, si existe.
- Los errores se manejan igual que en la sincronización: sin red, tiempo de espera agotado, error HTTP y respuesta inesperada.

**Limitaciones del clima:** la probabilidad de lluvia es el máximo previsto para el día completo, no el valor del instante. La última lectura se guarda solo en memoria y se pierde al cerrar la aplicación. Open-Meteo limita su uso gratuito a fines no comerciales.

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

## 12. Pruebas

### 12.1 Pruebas automatizadas

El proyecto incluye pruebas unitarias en `app/src/test/`, ejecutadas en la JVM. No requieren emulador ni conexión a internet: las pruebas de red usan un servidor HTTP local del JDK.

| Suite | Pruebas | Qué verifica |
|---|---|---|
| `EstadosReporteTest` | 9 | Estados de reporte (editable, eliminable, pendiente), valores iniciales, trazabilidad, identificadores y constantes de reintento |
| `SerializacionReporteTest` | 6 | Serialización JSON de reportes y adjuntos, campos opcionales y datos inválidos |
| `RepositorioReportesTest` | 14 | Altas, actualizaciones y bajas, envío interrumpido que vuelve a la cola, borrador y migración del JSON antiguo, con un DAO en memoria |
| `ApiSimuladaTest` | 5 | Respuestas de la sincronización simulada |
| `ApiRemotaSincronizacionTest` | 13 | Cliente de JSONPlaceholder: respuestas correctas, errores HTTP, tiempo de espera, sin conexión, respuestas inesperadas y datos que no deben enviarse |
| `ApiClimaOpenMeteoTest` | 13 | Cliente de Open-Meteo: petición GET, interpretación de la respuesta, errores HTTP, tiempo de espera, sin conexión y respuestas inválidas |
| `GestorClimaTest` | 15 | Estados de la tarjeta del clima, conservación de la última lectura, cambio de sitio y catálogo de sitios |

Resultado de la última ejecución: **75 pruebas, 0 fallidas, 0 omitidas**.

**Comandos:**

En Windows:

```powershell
.\gradlew.bat testDebugUnitTest
```

En Linux o macOS:

```bash
./gradlew testDebugUnitTest
```

El informe HTML se genera en:

```text
app/build/reports/tests/testDebugUnitTest/index.html
```

**Lo que las pruebas automatizadas no cubren:**

- Pruebas de interfaz Compose ni pruebas instrumentadas en dispositivo o emulador.
- La base de datos Room real: el repositorio se prueba con un DAO en memoria.
- El bucle de reintentos automáticos de `AppViewModel`, que solo cuenta con pruebas de sus constantes.

### 12.2 Verificación manual

Durante el desarrollo y la preparación de la defensa se realizaron verificaciones manuales de las funcionalidades principales.

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
| Tarjeta de clima con conexión y sin conexión (emulador) | Realizada |

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

La documentación académica no equivale a la aprobación formal de los requisitos por parte de VITAIROS Industrial AI. Los documentos de esta carpeta no se actualizaron con la sincronización mediante JSONPlaceholder, el clima ni las pruebas automatizadas descritas en este README.

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

Se implementó el flujo principal de registro de reportes, incorporación de adjuntos, validaciones, almacenamiento local, cola de sincronización, historial, sincronización de demostración mediante JSONPlaceholder, consulta meteorológica mediante Open-Meteo y pruebas unitarias automatizadas.

### Limitaciones actuales

- La autenticación utiliza usuarios ficticios.
- La sincronización predeterminada es simulada. La opción remota usa JSONPlaceholder, que no conserva los reportes, y envía solo datos de clasificación ficticios, sin adjuntos.
- No existe integración con servicios empresariales reales ni con un backend productivo.
- Los adjuntos no se transmiten a ningún servicio externo.
- La detección de falta de conexión depende del interruptor de la aplicación y del fallo de la petición; no se usa el estado de red del sistema.
- La probabilidad de lluvia corresponde al máximo diario y la última lectura del clima no se conserva al cerrar la aplicación.
- No hay pruebas de interfaz ni instrumentadas, y el bucle de reintentos no tiene pruebas automatizadas propias.
- Solo se genera un APK de depuración; no existe configuración de firma de producción.
- Los requisitos definitivos requieren validación por parte de VITAIROS.
- No se implementan servicios productivos de inteligencia artificial en esta etapa.

### Trabajo pendiente

- Revisar las observaciones de la evaluación.
- Completar las validaciones pendientes con VITAIROS.
- Ampliar las pruebas: bucle de reintentos, pruebas de interfaz y pruebas instrumentales con Room.
- Actualizar los documentos de `documentacion/` con las APIs y las pruebas incorporadas.
- Evaluar futuras integraciones según los requisitos aprobados.

---

**VITAIROS Industrial AI**  
**Equipo 1 — Chiguin Chiang y Lizandro Godoy**  
**Duoc UC — Desarrollo de Aplicaciones Móviles (DSY1105)**
