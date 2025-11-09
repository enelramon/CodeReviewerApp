# Arquitectura del Código

Este documento explica la arquitectura y estructura del código de la aplicación Code Reviewer.

## Visión General

La aplicación sigue el patrón arquitectónico MVVM (Model-View-ViewModel) recomendado por Google para aplicaciones Android con Jetpack Compose.

```
┌─────────────────┐
│   MainActivity  │  (Activity de Android)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ CodeReviewerApp │  (Composable raíz con NavigationHost)
└────────┬────────┘
         │
         ├────────────────────────────────┐
         │                                │
         ▼                                ▼
┌────────────────┐            ┌──────────────────┐
│  Navigation    │            │ CodeReviewVM     │
│  (3 Screens)   │◄───────────│  (ViewModel)     │
└────────────────┘            └────────┬─────────┘
         │                              │
         │                              ▼
         │                    ┌─────────────────┐
         │                    │  GitHubApi      │
         │                    │  (Retrofit)     │
         │                    └─────────────────┘
         │
         ├──────────┬──────────┬──────────┐
         ▼          ▼          ▼          ▼
    Selection   Review    Summary   SyntaxHighlight
    Screen      Screen    Screen    Component
```

## Componentes Principales

### 1. Data Models (Líneas 32-58)

#### Modelos de GitHub API
- **GitHubTree**: Respuesta del endpoint de árbol de archivos
- **GitHubTreeItem**: Item individual en el árbol (archivo o directorio)
- **GitHubBlob**: Contenido de un archivo (blob en terminología de Git)

#### Modelos de la Aplicación
- **FileItem**: Representa un archivo seleccionable con estado
- **CodeComment**: Comentario asociado a un archivo específico

```kotlin
@Serializable
data class GitHubTree(
    val tree: List<GitHubTreeItem>
)

data class FileItem(
    val path: String,
    val sha: String,
    var isSelected: Boolean = false
)
```

### 2. API Interface (Líneas 60-73)

Interfaz de Retrofit que define los endpoints de GitHub:

- **getTree()**: Obtiene el árbol de archivos del repositorio
  - Path: `/repos/{owner}/{repo}/git/trees/{sha}`
  - Parámetro recursive=1 para obtener todos los archivos
  
- **getBlob()**: Obtiene el contenido de un archivo específico
  - Path: `/repos/{owner}/{repo}/git/blobs/{sha}`
  - Devuelve contenido en Base64

```kotlin
interface GitHubApi {
    @GET("repos/{owner}/{repo}/git/trees/{sha}")
    suspend fun getTree(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("sha") sha: String,
        @Query("recursive") recursive: Int = 1
    ): GitHubTree
}
```

### 3. ViewModel (Líneas 75-143)

**CodeReviewViewModel** gestiona todo el estado de la aplicación:

#### Estado Observable
- `owner`, `repo`, `branch`: Información del repositorio
- `files`: Lista de archivos .kt disponibles
- `isLoading`: Estado de carga
- `error`: Mensajes de error
- `currentFileContent`: Contenido del archivo actual
- `currentFileName`: Nombre del archivo actual
- `currentComment`: Comentario en edición
- `comments`: Lista de comentarios guardados

#### Funciones Principales
- **loadFiles()**: Carga la lista de archivos del repositorio
- **loadFileContent()**: Carga el contenido de un archivo específico
- **toggleFileSelection()**: Alterna la selección de un archivo
- **addComment()**: Guarda un comentario
- **getSelectedFiles()**: Filtra archivos seleccionados

#### Configuración de Retrofit
```kotlin
private val retrofit = Retrofit.Builder()
    .baseUrl("https://api.github.com/")
    .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
    .client(OkHttpClient.Builder().build())
    .build()
```

### 4. Screens (Composables)

#### SelectionScreen (Líneas 184-283)

**Responsabilidades:**
- Permitir entrada de datos del repositorio (owner, repo, branch)
- Mostrar lista de archivos .kt con checkboxes
- Validar selección antes de continuar

**Componentes:**
- TextField para owner, repo, branch
- Botón "Cargar Archivos"
- LazyColumn con checkboxes para cada archivo
- Botón "Siguiente" habilitado solo si hay archivos seleccionados

**Flujo:**
1. Usuario ingresa datos del repo
2. Presiona "Cargar Archivos"
3. ViewModel llama a GitHub API
4. Se muestran archivos .kt en lista
5. Usuario selecciona archivos
6. Navega a ReviewScreen

#### ReviewScreen (Líneas 285-408)

**Responsabilidades:**
- Mostrar código con resaltado de sintaxis
- Permitir agregar comentarios
- Navegar entre archivos seleccionados

**Componentes:**
- Card con código resaltado (SyntaxHighlightedCode)
- TextField para comentarios
- Botones de navegación (Anterior/Siguiente)
- Indicador de progreso

**Flujo:**
1. Al entrar, carga el primer archivo seleccionado
2. Muestra código con resaltado
3. Usuario puede agregar comentario
4. Botón "Guardar Comentario" añade a la lista
5. Navegación entre archivos con LaunchedEffect

#### SummaryScreen (Líneas 465-522)

**Responsabilidades:**
- Mostrar todos los comentarios guardados
- Permitir iniciar nueva revisión

**Componentes:**
- LazyColumn con Cards para cada comentario
- Botón "Nueva Revisión" para volver al inicio

### 5. SyntaxHighlightedCode Component (Líneas 410-463)

**Responsabilidad:** Renderizar código Kotlin con resaltado de sintaxis

**Implementación:**
```kotlin
@Composable
fun SyntaxHighlightedCode(code: String) {
    val highlights = remember(code) {
        Highlights.Builder()
            .code(code)
            .theme(SyntaxThemes.darcula())
            .language(SyntaxLanguage.KOTLIN)
            .build()
    }
    
    val annotatedString = buildAnnotatedString {
        // Procesa highlights y aplica estilos
    }
    
    LazyColumn {
        item {
            Text(
                text = annotatedString,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
```

**Características:**
- Usa `dev.snipme:highlights` para análisis de sintaxis
- Tema Darcula (fondo oscuro)
- Fuente monospace
- Colores según tipo de token (keyword, string, comment, etc.)

## Flujo de Datos

### Carga de Archivos
```
Usuario ingresa datos → loadFiles() → GitHub API
                                    → getTree()
                                    → Filtrar .kt
                                    → Actualizar files
                                    → UI se actualiza
```

### Revisión de Archivo
```
Usuario selecciona archivo → loadFileContent() → GitHub API
                                                → getBlob()
                                                → Decodificar Base64
                                                → Actualizar currentFileContent
                                                → SyntaxHighlightedCode renderiza
```

### Guardar Comentario
```
Usuario escribe comentario → addComment() → Agregar a comments
                                          → Limpiar currentComment
                                          → UI se actualiza
```

## Gestión de Estado

### Compose State
Todo el estado se gestiona con `mutableStateOf` y `mutableStateListOf`:
- Cambios en el estado automáticamente recomponen la UI
- `remember` previene recreación innecesaria de objetos
- `LaunchedEffect` para efectos secundarios (cargar datos)

### Navegación
- NavController gestiona el stack de navegación
- Rutas: "selection", "review", "summary"
- ViewModel compartido entre todas las pantallas

## Consideraciones de Diseño

### Performance
- `LazyColumn` para listas grandes (archivos, comentarios)
- `remember` para evitar recrear highlights en cada recomposición
- Carga asíncrona con coroutines (suspend functions)

### UX
- Loading states durante llamadas de red
- Mensajes de error claros
- Botones deshabilitados cuando no aplican
- Indicador de progreso en revisión

### Mantenibilidad
- Todo en un archivo facilita comprensión inicial
- Separación clara de responsabilidades por función
- Comentarios en secciones clave
- Nombres descriptivos de variables y funciones

## Extensiones Futuras

### Posibles Mejoras
1. **Persistencia**: Guardar comentarios en base de datos local (Room)
2. **Autenticación**: Soportar repos privados con GitHub token
3. **Más lenguajes**: Extender soporte más allá de .kt
4. **Export**: Exportar comentarios a PDF o Markdown
5. **Filtros**: Filtrar archivos por directorio o patrón
6. **Multi-repo**: Revisar múltiples repos en una sesión
7. **Offline**: Cachear archivos para revisión sin conexión
8. **Colaboración**: Compartir revisiones con otros usuarios
