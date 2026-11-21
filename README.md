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

**Alcance:** aplicación operativa local con un modo demostración independiente. En el **modo operativo** (el predeterminado) los reportes, fotografías y PDF se guardan en el dispositivo y funcionan sin conexión; no existe backend, sincronización empresarial, autenticación corporativa ni procesamiento de inteligencia artificial, y el acceso se protege con un inicio de sesión local de demostración. En el **modo demostración** se usan exclusivamente datos ficticios y la sincronización de demostración contra JSONPlaceholder. Open-Meteo (condiciones meteorológicas reales) está disponible en ambos modos. Ambos servicios son públicos, gratuitos y sin clave.

## 2. Funcionalidades implementadas

- Modo operativo local (predeterminado) y modo demostración opcional, con datos separados.
- Inicio de sesión local de demostración en el modo operativo (usuario `operador`) y ingreso con usuarios ficticios en el modo demostración, que no exige credenciales.
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
- Cola local en el modo operativo: los reportes quedan guardados en el dispositivo como «Pendiente de sincronización»; nunca se marcan como enviados porque no existe un destino remoto.
- Cola de envío con reintentos automáticos en el modo demostración.
- Sincronización simulada (predeterminada en la demostración) y sincronización de demostración contra la API REST JSONPlaceholder (opcional, desde Ajustes, solo en la demostración).
- Consulta de condiciones meteorológicas (temperatura, viento, humedad y probabilidad de lluvia) mediante la API REST Open-Meteo, para sitios ficticios seleccionables.
- Historial y trazabilidad de reportes.
- Animación de inicio y mensajes de retroalimentación.
- Pruebas automatizadas unitarias, pruebas instrumentadas de migración de la base de datos e informe de cobertura JaCoCo.

### Flujo principal

Modo operativo: Inicio de sesión → Nuevo reporte → Datos generales → Adjuntos → Revisión → Guardado local (pendiente de sincronización) → Historial.

Modo demostración (sin credenciales): Ingreso a la demostración → Nuevo reporte → Datos generales → Adjuntos → Revisión → Guardado local → Cola de envío → Sincronización (simulada o JSONPlaceholder) → Historial.

## 2.1 Modo operativo y modo demostración

| Aspecto | Modo operativo (predeterminado) | Modo demostración |
|---|---|---|
| Acceso | Inicio de sesión local (`operador` / `12345`), sesión persistente y cierre desde Ajustes | Sin credenciales: ingreso con usuarios ficticios; accesible desde el inicio de sesión operativo |
| Datos | Reportes reales del usuario | Exclusivamente datos ficticios |
| Guardado | Room y almacenamiento privado, sin conexión | Room y almacenamiento privado |
| Envío remoto | No existe; el botón Enviar no se muestra y `send` se rechaza. Los reportes quedan «Pendiente de sincronización» | Simulado o JSONPlaceholder («Envío simulado completado») |
| Controles de red/error/API | Ocultos; el clima usa la conectividad real del dispositivo | Visibles en Inicio, Cola y Ajustes; la «red simulada» no es la conectividad real |
| Indicador | Etiqueta «Operativo» en cada reporte | Encabezado «MODO DEMOSTRACIÓN · datos ficticios» |
| Open-Meteo | Disponible | Disponible |

**Inicio de sesión local:** al entrar al modo operativo sin sesión activa se muestra la pantalla de inicio de sesión. Las credenciales de demostración son usuario `operador` y contraseña `12345`; están pensadas solo para esta versión de demostración y portafolio, no son autenticación corporativa ni seguridad empresarial. La contraseña se oculta visualmente y puede mostrarse con el botón «Mostrar». La contraseña no se guarda en Room, DataStore ni preferencias: la aplicación solo conserva una huella derivada (PBKDF2-HMAC-SHA256 con sal) para compararla, y la sesión se persiste como una marca de tiempo en las preferencias, con una duración máxima de 12 horas. «Cerrar sesión» en Ajustes regresa al inicio de sesión, impide acceder a las pantallas operativas y conserva todos los reportes. El control de acceso está centralizado en `ControlAcceso` y el ViewModel rechaza además las acciones operativas sin sesión. No se registran contraseñas en logs.

**Entrada y salida de la demostración:** desde el inicio de sesión operativo, «Explorar el modo demostración» entra sin credenciales, y «Salir de la demostración» regresa al modo operativo, donde se vuelve a exigir la sesión. Al entrar a la demostración nunca se muestran datos operativos.

**Cambio de modo:** Ajustes → «Entrar al modo demostración» / «Salir de la demostración» (la pantalla de ingreso de la demostración también ofrece salir). No se puede cambiar de modo con un reporte en edición. El modo elegido se conserva al cerrar la aplicación.

**Origen visible y catálogos:** cada tarjeta y el detalle muestran el modo de origen del reporte. Los catálogos de tipo y proceso del modo operativo no contienen textos de demostración; los de la demostración se identifican con «(demostración)». La ranura del borrador (0 operativo, 1 demostración) determina el modo del borrador al recuperarlo, aunque su JSON no lo indique.

**Reportes anteriores:** los reportes creados antes de separar los modos no se reclasifican automáticamente sin evidencia de su origen. Los envíos simulados antiguos que aparecen en el modo operativo se rotulan «Enviado (simulado, histórico)».

**Separación de datos:** cada reporte guarda su `modo` en la tabla `reports`. El repositorio solo expone, edita, elimina y restablece los reportes del modo activo, y cada modo tiene su propio borrador (ranuras 0 y 1 de la tabla `draft`). Entrar o salir de la demostración no modifica los reportes operativos, y «Restablecer demostración» solo elimina datos ficticios y sus archivos. Los archivos adjuntos de ambos modos se protegen al limpiar archivos huérfanos.

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

Es un APK de depuración firmado con la clave de depuración de Android.

### Generación segura del APK Release

El APK Release se firma con una clave privada que **no se incluye en el repositorio** (`*.jks`, `*.keystore` y `keystore.properties` están en `.gitignore`).

1. Crear el almacén de claves fuera del repositorio con `keytool` (la herramienta solicita las contraseñas de forma interactiva):

   ```powershell
   keytool -genkeypair -v -keystore C:\ruta\privada\vitairos-release.jks -alias vitairos -keyalg RSA -keysize 2048 -validity 10000
   ```

2. Copiar `keystore.properties.example` a `keystore.properties` en la raíz del proyecto y completar `storeFile`, `storePassword`, `keyAlias` y `keyPassword`. Este archivo no se versiona.
3. Generar el APK:

   ```powershell
   .\gradlew.bat assembleRelease
   ```

   El resultado queda en `app/build/outputs/apk/release/app-release.apk`. Sin `keystore.properties`, Gradle genera `app-release-unsigned.apk`, que no es instalable; nunca se firma con la clave de depuración.
4. Verificar la firma con las herramientas del SDK:

   ```powershell
   apksigner verify --verbose --print-certs app\build\outputs\apk\release\app-release.apk
   ```

El Release usa R8 con las reglas de `app/proguard-rules.pro`, que conservan las bibliotecas `kotlin` y `kotlinx` (sin ellas la app se cierra al iniciar). La firma se verificó con `apksigner` (esquema v2, certificado propio distinto del de depuración) y el APK se instaló y abrió en un emulador Android 13.

Conservar una copia de seguridad privada del `.jks` y de sus contraseñas: sin ellas no es posible publicar actualizaciones firmadas con la misma clave. `applicationId`, `versionCode` (1) y `versionName` (1.0) se mantienen.

### Instalación en Android

- Con el dispositivo conectado y la depuración USB activada: `adb install -r app\build\outputs\apk\release\app-release.apk`.
- O copiar el APK al teléfono y abrirlo, permitiendo la instalación de orígenes desconocidos para la aplicación que lo abre.
- Requiere Android 8.0 (API 26) o superior.

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
│   │   │   ├── VitairosDatabase.kt  (versión 2, migración 1→2)
│   │   │   ├── ReportDao.kt
│   │   │   └── entity/
│   │   ├── AppPreferences.kt
│   │   ├── SelectorModo.kt
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

Las pruebas unitarias se encuentran en `app/src/test/` y las pruebas instrumentadas de migración en `app/src/androidTest/`. Los esquemas exportados de Room están en `app/schemas/`.

## 6. Almacenamiento local

La aplicación utiliza Room/SQLite como mecanismo principal de persistencia.

La base de datos `vitairos.db` contempla las siguientes entidades:

| Entidad | Función |
|---|---|
| `ReportEntity` | Almacena la información de los reportes |
| `AttachmentEntity` | Registra los metadatos y referencias de los archivos adjuntos |
| `LogEntryEntity` | Registra eventos de trazabilidad |
| `DraftEntity` | Conserva el reporte en elaboración (una ranura por modo) |

La tabla `reports` incluye la columna `modo` (`OPERATIVO` o `DEMO`). La base está en la versión 2: la migración 1→2 es no destructiva, agrega la columna con valor `OPERATIVO` por defecto y reclasifica como `DEMO` los reportes y el borrador que ya contenían adjuntos de demostración. Se validó con pruebas instrumentadas sobre un emulador.

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

Todo reporte se guarda primero en la base local Room/SQLite, con o sin conexión. En el modo operativo la red solo se usa para consultar el clima; en la demostración interviene además al enviar un reporte desde la cola. La persistencia local no depende de la red.

En el modo operativo, los reportes guardados quedan en una cola local sin envío remoto. En la demostración quedan pendientes en una cola de envío. Si la conexión no está disponible, permanecen en la cola y se envían al recuperarla. Un envío que quedó a medias al cerrar la aplicación vuelve a la cola al reabrirla.

La disponibilidad de conexión se controla desde la aplicación, mediante el interruptor **Conexión simulada** de Inicio y Ajustes, y además se detecta cuando una petición real falla por falta de red.

El mecanismo contempla un intento inicial y hasta tres reintentos automáticos con intervalos de 5, 15 y 30 segundos. Tras agotarlos, el reporte queda disponible para un reintento manual.

### 9.2 Sincronización de demostración (solo modo demostración)

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

**Estados de la cola:** Borrador (no enviado, no aparece en la cola), Pendiente (preparado para envío; en el modo operativo se muestra «Pendiente de sincronización»), Enviando, Enviado y Error. La cola solo lista Pendiente, Enviando y Error; los enviados permanecen en el Historial con su trazabilidad y nunca se eliminan. Una pulsación repetida no duplica el guardado ni el envío.

**Mensajes de resultado:** en la demostración un envío correcto informa «Envío simulado completado»; con JSONPlaceholder se aclara que es una API de pruebas que no almacena el reporte. Nunca se afirma que se guardó en un servidor empresarial.

**Importante:** la sincronización no transfiere información a un backend productivo. Los reportes operativos nunca se envían a JSONPlaceholder: fuera del modo demostración `send` y `sendAll` se rechazan y el reporte permanece guardado localmente, pendiente de sincronización real.

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
- La consulta se realiza al abrir Inicio, al cambiar de sitio, al cambiar la red simulada y al pulsar **Actualizar clima**.
- Al pulsar **Actualizar clima** se comprueba de inmediato la conectividad: en el modo operativo la del dispositivo (`ConnectivityManager`, permiso `ACCESS_NETWORK_STATE`) y en la demostración, además, el interruptor de red simulada. Sin conexión se muestra «Sin conexión. No se puede actualizar el clima», indicando si la causa es la red simulada o el dispositivo; no hace falta cambiar de sitio.
- Si existe una lectura anterior se rotula «Última información disponible (no es una lectura actual)» y su hora de medición; nunca se presenta como recién consultada. Al recuperar la conexión se puede consultar de nuevo.
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
| `AislamientoModosTest` | 23 | Separación de reportes entre modos, entrada y salida de la demo, edición y eliminación en el modo correcto, persistencia tras reabrir, integridad de adjuntos, borradores por modo (la ranura define el modo), sin reclasificación de reportes antiguos y trazabilidad de envíos simulados |
| `SesionOperativaTest` | 18 | Inicio de sesión correcto e incorrecto, campos vacíos, cierre, persistencia y expiración, ausencia de contraseña en el almacén y protección de pantallas por modo (`ControlAcceso`) |
| `EtiquetasYCatalogosTest` | 11 | Catálogos sin textos de demostración en el modo operativo, etiquetas de estado y de modo, y estados de la cola |
| `SelectorModoTest` | 7 | Modo operativo por defecto, entrada y salida de la demostración y persistencia del modo |
| `ApiSimuladaTest` | 5 | Respuestas de la sincronización simulada |
| `ApiRemotaSincronizacionTest` | 13 | Cliente de JSONPlaceholder: respuestas correctas, errores HTTP, tiempo de espera, sin conexión, respuestas inesperadas y datos que no deben enviarse |
| `ApiClimaOpenMeteoTest` | 13 | Cliente de Open-Meteo: petición GET, interpretación de la respuesta, errores HTTP, tiempo de espera, sin conexión y respuestas inválidas |
| `GestorClimaTest` | 22 | Estados de la tarjeta del clima, actualización inmediata sin conexión, lectura anterior no presentada como actual, origen de la desconexión, recuperación de conectividad, cambio de sitio y catálogo de sitios |

Resultado de la última ejecución: **141 pruebas unitarias, 0 fallidas, 0 omitidas**.

Además, `MigracionBaseDatosTest` (`app/src/androidTest/`, 3 pruebas) valida con `MigrationTestHelper` la migración de Room 1→2 sobre un emulador Android 13: conserva reportes y adjuntos, los clasifica por modo y conserva el borrador. Resultado de la última ejecución: **3 aprobadas, 0 fallidas**.

**Comandos:**

En Windows:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat connectedDebugAndroidTest
.\gradlew.bat createDebugUnitTestCoverageReport
```

`connectedDebugAndroidTest` requiere un emulador o dispositivo conectado. En Linux o macOS se usa `./gradlew`.

Los informes se generan en:

```text
app/build/reports/tests/testDebugUnitTest/index.html
app/build/reports/coverage/test/debug/index.html
```

### 12.1.1 Cobertura (JaCoCo)

Medida con `createDebugUnitTestCoverageReport` sobre las 98 pruebas unitarias:

| Métrica | Cubierto | Total | Porcentaje |
|---|---|---|---|
| Instrucciones | 4.426 | 29.229 | 15,1 % |
| Ramas | 147 | 1.138 | 12,9 % |
| Líneas | 516 | 3.236 | 15,9 % |

La cobertura se concentra en la capa de datos: repositorio 94,7 % de líneas, clientes remotos 90,8 % y modelos 94,9 %. Las pantallas Compose y `AppViewModel` (8,3 % de líneas) no tienen pruebas automatizadas y el DAO generado por Room solo se ejercita en las pruebas instrumentadas, que no forman parte de este informe; por eso el porcentaje global es bajo.

**Lo que las pruebas automatizadas no cubren:**

- Pruebas de interfaz Compose.
- La base de datos Room real en las pruebas unitarias: el repositorio se prueba con un DAO en memoria (solo la migración se prueba con Room real).
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
| Inicio en modo operativo, entrada y salida de la demostración y persistencia del modo tras reiniciar (emulador Android 13) | Realizada |
| Clima real de Open-Meteo en modo demostración (emulador) | Realizada |

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

La documentación académica no equivale a la aprobación formal de los requisitos por parte de VITAIROS Industrial AI. Los documentos de esta carpeta no se actualizaron con los modos operativo y demostración, la sincronización mediante JSONPlaceholder, el clima ni las pruebas automatizadas descritas en este README.

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

Se implementó el flujo principal de registro de reportes, incorporación de adjuntos, validaciones, almacenamiento local, cola local, historial, modo demostración independiente con sincronización de demostración mediante JSONPlaceholder, consulta meteorológica mediante Open-Meteo, pruebas unitarias e instrumentadas, cobertura JaCoCo y configuración de firma para el APK Release.

### Limitaciones actuales

- No existe backend, sincronización empresarial, autenticación corporativa ni procesamiento de inteligencia artificial; en el modo operativo los reportes permanecen solo en el dispositivo.
- El inicio de sesión del modo operativo es local y de demostración (`operador` / `12345`): una sola credencial conocida, sin cuentas, roles ni recuperación; no equivale a seguridad empresarial.
- En el modo operativo no hay destino remoto: los reportes quedan «Pendiente de sincronización» y no existe un estado «Enviado» real para ellos.
- La sincronización de la demostración es simulada o usa JSONPlaceholder, que no conserva los reportes y recibe solo datos de clasificación ficticios, sin adjuntos.
- Los adjuntos no se transmiten a ningún servicio externo.
- La red simulada solo existe en la demostración; el estado de red real del dispositivo solo se consulta para el clima.
- La probabilidad de lluvia corresponde al máximo diario y la última lectura del clima no se conserva al cerrar la aplicación.
- La cobertura de pruebas es baja en la interfaz y el ViewModel (ver 12.1.1); no hay pruebas de interfaz Compose y el bucle de reintentos no tiene pruebas automatizadas propias.
- Los datos creados antes de la versión 2 se clasifican como operativos, salvo los que ya contenían adjuntos de demostración; no se reclasifican por otros indicios. Los envíos simulados antiguos se rotulan como históricos.
- El APK Release solo es instalable si se firma con una clave privada propia (ver «Generación segura del APK Release»); la instalación en un teléfono físico no se ha probado.
- Los requisitos definitivos requieren validación por parte de VITAIROS.

### Trabajo pendiente

- Revisar las observaciones de la evaluación.
- Completar las validaciones pendientes con VITAIROS.
- Ampliar las pruebas: bucle de reintentos, pruebas de interfaz y pruebas instrumentadas del DAO.
- Actualizar los documentos de `documentacion/` con las APIs y las pruebas incorporadas.
- Evaluar futuras integraciones según los requisitos aprobados.

---

**VITAIROS Industrial AI**  
**Equipo 1 — Chiguin Chiang y Lizandro Godoy**  
**Duoc UC — Desarrollo de Aplicaciones Móviles (DSY1105)**
