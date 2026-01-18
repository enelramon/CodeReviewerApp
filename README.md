# CodeReviewerApp

App de Jetpack Compose para revisar código de repositorios de GitHub con inteligencia artificial y gestión de historial.

## Descripción

Esta es una aplicación de Android desarrollada en Jetpack Compose que permite revisar archivos de código de repositorios de GitHub. La aplicación cuenta con cuatro pantallas principales:

1. **Pantalla de Selección**: Permite ingresar los datos de un repositorio de GitHub, seleccionar el tipo de proyecto (Kotlin/Blazor), y elegir los archivos que se desean revisar mediante checkboxes.

2. **Pantalla de Revisión**: Muestra el código fuente con resaltado de sintaxis dinámico y permite agregar comentarios sobre cada archivo con sugerencias de IA contextuales.

3. **Pantalla de Resumen**: Muestra una lista de todos los comentarios agregados durante la revisión y permite generar un resumen con IA.

4. **Pantalla de Historial**: Explora revisiones pasadas guardadas en Firebase Firestore.

## Características

- ✅ Implementación en un solo archivo `.kt` (MainActivity.kt)
- ✅ Uso de ViewModel para gestión del estado
- ✅ Integración con GitHub API usando Retrofit
- ✅ Serialización con kotlinx.serialization
- ✅ Resaltado de sintaxis dinámico (Kotlin/C#) con dev.snipme:highlights
- ✅ Navegación entre pantallas con Jetpack Compose Navigation
- ✅ Material Design 3
- ✨ **NUEVO**: Selector de tipo de proyecto (Kotlin/Blazor)
- ✨ **NUEVO**: Persistencia de comentarios al navegar entre archivos
- ✨ **NUEVO**: Actualización de comentarios existentes (sin duplicados)
- ✨ **NUEVO**: Sugerencias automáticas contextuales con Gemini AI
- ✨ **NUEVO**: Resumen de revisión generado por IA
- ✨ **NUEVO**: Historial de revisiones con Firebase Firestore
- ✨ **NUEVO**: Evento ResetState para limpiar estado entre revisiones

## Tecnologías Utilizadas

- **Jetpack Compose**: UI moderna y declarativa
- **ViewModel**: Gestión de estado y lógica de negocio
- **Retrofit**: Cliente HTTP para consumir la API de GitHub
- **kotlinx.serialization**: Serialización/deserialización JSON
- **Gemini AI**: Inteligencia artificial de Google para sugerencias de código
- **Firebase Firestore**: Base de datos en la nube para historial de revisiones
- **dev.snipme:highlights**: Resaltado de sintaxis de código
- **Jetpack Navigation**: Navegación entre pantallas

## Nuevas Características

Esta versión incluye varias mejoras importantes:

- 🎨 **Selector de Tipo de Proyecto**: Soporte para Kotlin y Blazor (C#) con resaltado de sintaxis dinámico
- 💾 **Persistencia de Comentarios**: Los comentarios se cargan automáticamente al navegar entre archivos
- 🔄 **Gestión Inteligente**: Actualiza comentarios existentes en lugar de crear duplicados
- 🤖 **IA Contextual**: Sugerencias especializadas según el tipo de proyecto
- 📊 **Resumen con IA**: Genera un análisis ejecutivo de toda la revisión
- 📚 **Historial en Firestore**: Guarda y consulta revisiones pasadas en la nube
- 🧹 **Estado Limpio**: Reinicia la app para nueva revisión conservando configuración del repo

**Ver [NUEVAS_CARACTERISTICAS.md](NUEVAS_CARACTERISTICAS.md) para documentación detallada de todas las mejoras.**

## Estructura del Código

El archivo `MainActivity.kt` contiene:

- **Data Models**: `GitHubTree`, `GitHubTreeItem`, `GitHubBlob`, `FileItem`, `CodeComment`
- **GitHubApi**: Interface de Retrofit para llamadas a la API de GitHub
- **CodeReviewViewModel**: ViewModel que gestiona el estado de la aplicación
- **MainActivity**: Activity principal
- **Composables**:
  - `CodeReviewerApp`: Composable raíz con navegación
  - `SelectionScreen`: Pantalla de selección de archivos
  - `ReviewScreen`: Pantalla de revisión con resaltado de sintaxis
  - `SummaryScreen`: Pantalla de resumen de comentarios
  - `SyntaxHighlightedCode`: Componente para mostrar código con resaltado

## API de GitHub Utilizada

La aplicación consume dos endpoints de la GitHub REST API:

1. **GET /repos/{owner}/{repo}/git/trees/{sha}**: Obtiene el árbol de archivos del repositorio
2. **GET /repos/{owner}/{repo}/git/blobs/{sha}**: Obtiene el contenido de un archivo específico

## Requisitos

- Android Studio Arctic Fox o superior
- Android SDK API 26 o superior
- Kotlin 2.2.21 o superior
- Conexión a Internet para acceder a la API de GitHub
- Cuenta de Firebase (para funcionalidad de historial)
- API Key de Gemini (para sugerencias con IA)

## Configuración

### 1. Configurar Gemini AI

Para usar la función de sugerencias automáticas con IA:

1. Obtén una API Key de Gemini en [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Crea un archivo `local.properties` en la raíz del proyecto (si no existe)
3. Agrega tu API Key: `GEMINI_API_KEY=tu_api_key_aqui`

**Ver [GEMINI_SETUP.md](GEMINI_SETUP.md) para instrucciones detalladas.**

### 2. Configurar Firebase (Opcional pero Recomendado)

Para usar la funcionalidad de historial:

1. Crea un proyecto en [Firebase Console](https://console.firebase.google.com/)
2. Descarga el archivo `google-services.json`
3. Colócalo en la carpeta `app/` del proyecto
4. Habilita Firestore Database en Firebase Console

**Ver [FIREBASE_SETUP.md](FIREBASE_SETUP.md) para instrucciones detalladas.**

**Nota**: La app funciona sin Firebase, pero la funcionalidad de historial no estará disponible.

## Compilación

```bash
./gradlew build
```

## Instalación

1. Clonar el repositorio
2. Configurar Gemini AI (ver sección Configuración)
3. (Opcional) Configurar Firebase (ver sección Configuración)
4. Abrir el proyecto en Android Studio
5. Sincronizar el proyecto con Gradle
6. Ejecutar la aplicación en un emulador o dispositivo físico

## Uso

### Flujo Básico

1. Abrir la aplicación
2. **Seleccionar tipo de proyecto** (Kotlin o Blazor)
3. Ingresar la URL del repositorio de GitHub
4. Presionar el botón de búsqueda para cargar branches
5. Seleccionar el branch deseado
6. Presionar "Cargar Archivos"
7. Seleccionar los archivos que deseas revisar usando los checkboxes
8. Presionar "Siguiente" para ir a la pantalla de revisión

### Durante la Revisión

9. Revisar el código con resaltado de sintaxis (adaptado al tipo de proyecto)
10. **Presionar "Sugerir"** para obtener una sugerencia automática contextual usando Gemini AI
11. Agregar o editar comentarios sobre el código
12. Presionar "Guardar" para guardar/actualizar el comentario
13. Navegar entre archivos con los botones "Anterior" y "Siguiente"
    - Los comentarios previos se cargarán automáticamente
14. Al finalizar, presionar "Resumen" para ver todos los comentarios

### En el Resumen

15. **Presionar "Generar Resumen con IA"** para obtener un análisis ejecutivo
16. Revisar el resumen y todos los comentarios organizados por archivo
17. **Presionar "Finalizar y Guardar en Historial"** para guardar la revisión en Firestore
18. Usar el botón compartir para enviar los comentarios
19. Presionar "Nueva Revisión" para comenzar otra revisión

### Ver Historial

20. Desde la pantalla de selección, toca el ícono de historial en la barra superior
21. Explora revisiones pasadas con toda su información
22. Visualiza resúmenes de IA y comentarios previos

## Configuración de Gemini AI

Para usar la función de sugerencias automáticas con IA:

1. Obtén una API Key de Gemini en [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Crea un archivo `local.properties` en la raíz del proyecto
3. Agrega tu API Key: `GEMINI_API_KEY=tu_api_key_aqui`

**Ver [GEMINI_SETUP.md](GEMINI_SETUP.md) para instrucciones detalladas.**

## Documentación Adicional

- 📖 [NUEVAS_CARACTERISTICAS.md](NUEVAS_CARACTERISTICAS.md) - Documentación completa de todas las nuevas características
- 🔥 [FIREBASE_SETUP.md](FIREBASE_SETUP.md) - Guía de configuración de Firebase Firestore
- 🤖 [GEMINI_SETUP.md](GEMINI_SETUP.md) - Guía de configuración de Gemini AI
- 🏗️ [ARCHITECTURE.md](ARCHITECTURE.md) - Arquitectura de la aplicación
- 🚀 [QUICKSTART.md](QUICKSTART.md) - Guía rápida de inicio

## Notas

- La aplicación requiere permisos de Internet
- Los comentarios se gestionan en memoria durante la sesión activa
- El historial se guarda en Firestore (requiere configuración)
- Para repositorios privados, sería necesario agregar autenticación con token de GitHub
- El tipo de proyecto afecta el resaltado de sintaxis y el contexto de las sugerencias de IA
- Los comentarios se actualizan automáticamente si editas uno existente (sin duplicados)

## Tipos de Proyecto Soportados

### Kotlin
- Resaltado de sintaxis para Kotlin
- Sugerencias de IA enfocadas en:
  - Coroutines y flujos
  - Null safety
  - Convenciones de Kotlin
  - Arquitectura Android (MVVM, Repository)

### Blazor (C#)
- Resaltado de sintaxis para C#
- Sugerencias de IA enfocadas en:
  - Componentes Blazor
  - Data binding
  - Gestión de estado
  - Buenas prácticas de .NET

## Solución de Problemas

### "Gemini API key no configurada"
- Verifica que hayas creado el archivo `local.properties`
- Confirma que la key esté correcta: `GEMINI_API_KEY=tu_key`
- Sincroniza el proyecto con Gradle

### "Error al guardar en historial"
- Verifica la configuración de Firebase (ver FIREBASE_SETUP.md)
- Asegúrate de que `google-services.json` esté en `app/`
- Revisa las reglas de Firestore

### No carga los archivos
- Verifica la URL del repositorio
- Asegura conexión a Internet
- Confirma que el repositorio sea público

## Contribuir

Las contribuciones son bienvenidas. Por favor:
1. Fork el repositorio
2. Crea una rama para tu feature (`git checkout -b feature/NuevaCaracteristica`)
3. Commit tus cambios (`git commit -m 'Agrega nueva característica'`)
4. Push a la rama (`git push origin feature/NuevaCaracteristica`)
5. Abre un Pull Request