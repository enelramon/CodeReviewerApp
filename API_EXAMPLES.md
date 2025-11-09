# Ejemplos de Uso de la API de GitHub

Este documento proporciona ejemplos de cómo la aplicación interactúa con la GitHub REST API.

## Endpoints Utilizados

### 1. Obtener Árbol de Archivos

**Endpoint:** `GET /repos/{owner}/{repo}/git/trees/{sha}`

**Descripción:** Obtiene la estructura de archivos y directorios de un repositorio en un commit/branch específico.

**Ejemplo de Request:**
```
GET https://api.github.com/repos/google/gson/git/trees/main?recursive=1
```

**Parámetros:**
- `owner`: Propietario del repositorio (ej: "google")
- `repo`: Nombre del repositorio (ej: "gson")
- `sha`: Branch, tag o commit SHA (ej: "main")
- `recursive`: 1 para obtener toda la estructura recursivamente

**Ejemplo de Response (simplificado):**
```json
{
  "sha": "abc123...",
  "url": "https://api.github.com/repos/google/gson/git/trees/abc123",
  "tree": [
    {
      "path": "src/main/java/com/google/gson/Gson.java",
      "mode": "100644",
      "type": "blob",
      "sha": "def456...",
      "size": 42567,
      "url": "https://api.github.com/repos/google/gson/git/blobs/def456"
    },
    {
      "path": "README.md",
      "mode": "100644",
      "type": "blob",
      "sha": "ghi789...",
      "size": 3456,
      "url": "https://api.github.com/repos/google/gson/git/blobs/ghi789"
    },
    {
      "path": "src",
      "mode": "040000",
      "type": "tree",
      "sha": "jkl012...",
      "url": "https://api.github.com/repos/google/gson/git/trees/jkl012"
    }
  ],
  "truncated": false
}
```

**Campos Importantes:**
- `type`: "blob" (archivo) o "tree" (directorio)
- `path`: Ruta completa del archivo/directorio
- `sha`: Identificador único del contenido
- `size`: Tamaño en bytes

**Uso en la App:**
```kotlin
val tree = api.getTree(owner = "google", repo = "gson", sha = "main", recursive = 1)
val ktFiles = tree.tree.filter { it.type == "blob" && it.path.endsWith(".kt") }
```

### 2. Obtener Contenido de Archivo

**Endpoint:** `GET /repos/{owner}/{repo}/git/blobs/{sha}`

**Descripción:** Obtiene el contenido de un archivo específico usando su SHA.

**Ejemplo de Request:**
```
GET https://api.github.com/repos/google/gson/git/blobs/def456
```

**Parámetros:**
- `owner`: Propietario del repositorio
- `repo`: Nombre del repositorio
- `sha`: SHA del blob (obtenido del árbol)

**Ejemplo de Response:**
```json
{
  "sha": "def456...",
  "node_id": "MDQ6QmxvYmRlZjQ1Ng==",
  "size": 42567,
  "url": "https://api.github.com/repos/google/gson/git/blobs/def456",
  "content": "cGFja2FnZSBjb20uZ29vZ2xlLmdz\nb247CgppbXBvcnQgamF2YS5s\nYW5nLnJlZmxlY3QuVHlwZTsK\nCnB1YmxpYyBjbGFzcyBHc29u\nIHsKICAgIC8vIENvZGUgaGVy\nZQp9Cg==\n",
  "encoding": "base64"
}
```

**Campos Importantes:**
- `content`: Contenido del archivo codificado en Base64
- `encoding`: Tipo de codificación (usualmente "base64")
- `size`: Tamaño del archivo en bytes

**Decodificación:**
```kotlin
val decoded = if (blob.encoding == "base64") {
    String(Base64.getDecoder().decode(blob.content))
} else {
    blob.content
}
```

## Ejemplo Completo de Flujo

### Escenario: Obtener y Mostrar un Archivo Kotlin

```kotlin
// 1. Obtener lista de archivos
val tree = api.getTree(
    owner = "JetBrains",
    repo = "kotlin",
    sha = "master",
    recursive = 1
)

// 2. Filtrar solo archivos .kt
val kotlinFiles = tree.tree
    .filter { it.type == "blob" && it.path.endsWith(".kt") }

println("Encontrados ${kotlinFiles.size} archivos Kotlin")

// 3. Seleccionar un archivo (por ejemplo, el primero)
val selectedFile = kotlinFiles.first()
println("Archivo seleccionado: ${selectedFile.path}")

// 4. Obtener contenido del archivo
val blob = api.getBlob(
    owner = "JetBrains",
    repo = "kotlin",
    sha = selectedFile.sha
)

// 5. Decodificar contenido
val content = String(Base64.getDecoder().decode(blob.content))
println("Contenido del archivo:")
println(content)

// 6. Aplicar resaltado de sintaxis
val highlights = Highlights.Builder()
    .code(content)
    .theme(SyntaxThemes.darcula())
    .language(SyntaxLanguage.KOTLIN)
    .build()
```

## Límites de la API

### Rate Limiting

GitHub API tiene límites de tasa:
- **Sin autenticación**: 60 requests por hora
- **Con autenticación**: 5,000 requests por hora

**Headers de respuesta importantes:**
```
X-RateLimit-Limit: 60
X-RateLimit-Remaining: 59
X-RateLimit-Reset: 1372700873
```

### Tamaño de Archivos

- Archivos mayores a 1 MB pueden no devolver el contenido completo
- Se puede usar el campo `truncated` para verificar

### Autenticación (Opcional)

Para repositorios privados o mayor límite de tasa:

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "token YOUR_GITHUB_TOKEN")
            .build()
        chain.proceed(request)
    }
    .build()

val retrofit = Retrofit.Builder()
    .baseUrl("https://api.github.com/")
    .client(client)
    .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
    .build()
```

## Manejo de Errores

### Errores Comunes

**404 - Not Found:**
```json
{
  "message": "Not Found",
  "documentation_url": "https://docs.github.com/rest/reference/git#get-a-tree"
}
```
- Repositorio no existe
- Branch no existe
- SHA inválido

**403 - Forbidden:**
```json
{
  "message": "API rate limit exceeded for 192.30.252.1. (But here's the good news: Authenticated requests get a higher rate limit. Check out the documentation for more details.)",
  "documentation_url": "https://docs.github.com/rest/overview/resources-in-the-rest-api#rate-limiting"
}
```
- Límite de tasa excedido
- Repositorio privado sin autenticación

**422 - Unprocessable Entity:**
```json
{
  "message": "The tree must be less than 100,000 entries. If you receive this error, use the `recursive` flag with caution.",
  "documentation_url": "https://docs.github.com/rest/reference/git#get-a-tree"
}
```
- Árbol muy grande (usar sin recursive)

### Ejemplo de Manejo en la App

```kotlin
try {
    val tree = api.getTree(owner, repo, branch, 1)
    files = tree.tree
        .filter { it.type == "blob" && it.path.endsWith(".kt") }
        .map { FileItem(it.path, it.sha) }
} catch (e: HttpException) {
    when (e.code()) {
        404 -> error = "Repositorio o branch no encontrado"
        403 -> error = "Límite de API excedido o acceso denegado"
        422 -> error = "Repositorio muy grande"
        else -> error = "Error HTTP: ${e.code()}"
    }
} catch (e: IOException) {
    error = "Error de conexión: ${e.message}"
} catch (e: Exception) {
    error = "Error inesperado: ${e.message}"
}
```

## Referencias

- [GitHub REST API Documentation](https://docs.github.com/en/rest)
- [Git Trees API](https://docs.github.com/en/rest/git/trees)
- [Git Blobs API](https://docs.github.com/en/rest/git/blobs)
- [Rate Limiting](https://docs.github.com/en/rest/overview/resources-in-the-rest-api#rate-limiting)
- [Authentication](https://docs.github.com/en/rest/overview/resources-in-the-rest-api#authentication)
