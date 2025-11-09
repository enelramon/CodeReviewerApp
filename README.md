# CodeReviewerApp

App de Jetpack Compose para revisar código de repositorios de GitHub.

## Descripción

Esta es una aplicación de Android desarrollada en Jetpack Compose que permite revisar archivos de código Kotlin (.kt) de repositorios de GitHub. La aplicación cuenta con tres pantallas principales:

1. **Pantalla de Selección**: Permite ingresar los datos de un repositorio de GitHub (owner, repositorio, branch) y seleccionar los archivos .kt que se desean revisar mediante checkboxes.

2. **Pantalla de Revisión**: Muestra el código fuente con resaltado de sintaxis usando la librería `dev.snipme:highlights` y permite agregar comentarios sobre cada archivo.

3. **Pantalla de Resumen**: Muestra una lista de todos los comentarios agregados durante la revisión.

## Características

- ✅ Implementación en un solo archivo `.kt` (MainActivity.kt)
- ✅ Uso de ViewModel para gestión del estado
- ✅ Integración con GitHub API usando Retrofit
- ✅ Serialización con kotlinx.serialization
- ✅ Resaltado de sintaxis Kotlin con dev.snipme:highlights
- ✅ Navegación entre pantallas con Jetpack Compose Navigation
- ✅ Material Design 3
- ✨ **NUEVO**: Sugerencias automáticas de comentarios con Gemini AI

## Tecnologías Utilizadas

- **Jetpack Compose**: UI moderna y declarativa
- **ViewModel**: Gestión de estado y lógica de negocio
- **Retrofit**: Cliente HTTP para consumir la API de GitHub
- **kotlinx.serialization**: Serialización/deserialización JSON
- **Gemini AI**: Inteligencia artificial de Google para sugerencias de código
- **dev.snipme:highlights**: Resaltado de sintaxis de código
- **Jetpack Navigation**: Navegación entre pantallas

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
- Android SDK API 24 o superior
- Kotlin 1.9.10 o superior
- Conexión a Internet para acceder a la API de GitHub

## Compilación

```bash
./gradlew build
```

## Instalación

1. Clonar el repositorio
2. Abrir el proyecto en Android Studio
3. Ejecutar la aplicación en un emulador o dispositivo físico

## Uso

1. Abrir la aplicación
2. Ingresar el owner, repositorio y branch de GitHub (por defecto: google/gson/main)
3. Presionar "Cargar Archivos"
4. Seleccionar los archivos .kt que deseas revisar usando los checkboxes
5. Presionar "Siguiente" para ir a la pantalla de revisión
6. Revisar el código con resaltado de sintaxis
7. **NUEVO**: Presionar "Sugerir" para obtener una sugerencia automática de comentario usando Gemini AI
8. Agregar o editar comentarios sobre el código
9. Presionar "Guardar" para guardar el comentario
10. Navegar entre archivos con los botones "Anterior" y "Siguiente"
11. Al finalizar, presionar "Resumen" para ver todos los comentarios
12. En el resumen, puedes ver todos los comentarios organizados por archivo

## Configuración de Gemini AI

Para usar la función de sugerencias automáticas con IA:

1. Obtén una API Key de Gemini en [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Crea un archivo `local.properties` en la raíz del proyecto
3. Agrega tu API Key: `GEMINI_API_KEY=tu_api_key_aqui`

**Ver [GEMINI_SETUP.md](GEMINI_SETUP.md) para instrucciones detalladas.**

## Notas

- La aplicación requiere permisos de Internet
- Los comentarios se almacenan en memoria durante la sesión
- Para repositorios privados, sería necesario agregar autenticación con token de GitHub