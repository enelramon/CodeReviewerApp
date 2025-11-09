# Quick Start Guide

Esta guía te ayudará a poner en marcha la aplicación Code Reviewer en 5 minutos.

## ⚡ Inicio Rápido

### Requisitos Mínimos
- Android Studio (última versión estable)
- JDK 11 o superior
- Dispositivo Android o emulador con API 24+

### Pasos para Ejecutar

1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/enelramon/CodeReviewerApp.git
   cd CodeReviewerApp
   ```

2. **Abrir en Android Studio**
   - Abrir Android Studio
   - File → Open → Seleccionar carpeta del proyecto
   - Esperar sincronización de Gradle (primera vez puede tardar unos minutos)

3. **Ejecutar la aplicación**
   - Conectar un dispositivo Android o iniciar un emulador
   - Presionar el botón ▶️ Run o `Shift+F10`
   - La app se instalará y abrirá automáticamente

### Desde Terminal (Alternativa)

```bash
# Linux/Mac
./gradlew installDebug

# Windows
gradlew.bat installDebug
```

## 🎯 Primer Uso

### Ejemplo Básico - Revisar Repositorio de Google

1. La app se abre en la pantalla de Selección
2. Los campos vienen prellenados con valores por defecto:
   - **Owner**: `google`
   - **Repositorio**: `gson`
   - **Branch**: `main`

3. Presionar **"Cargar Archivos"**
   - La app consulta GitHub API
   - Muestra lista de archivos .kt encontrados

4. **Seleccionar archivos** haciendo clic en los checkboxes
   - Puedes seleccionar uno o varios archivos
   
5. Presionar **"Siguiente"** (muestra cantidad de archivos seleccionados)

6. En la **Pantalla de Revisión**:
   - Verás el código con resaltado de sintaxis
   - Escribe comentarios en el campo de texto
   - Presiona "Guardar Comentario" para añadirlo
   - Usa "Anterior"/"Siguiente" para navegar entre archivos
   - Presiona "Resumen" cuando termines

7. En la **Pantalla de Resumen**:
   - Verás todos los comentarios organizados por archivo
   - Presiona "Nueva Revisión" para revisar otro repo

## 🔍 Otros Repositorios para Probar

### Repositorio con Pocos Archivos (Rápido)
```
Owner: square
Repo: okhttp
Branch: master
```

### Repositorio de JetBrains
```
Owner: JetBrains
Repo: kotlin
Branch: master
```
⚠️ Nota: Kotlin tiene muchos archivos, puede tardar en cargar

### Tu Propio Repositorio
```
Owner: tu-usuario
Repo: tu-repositorio-kotlin
Branch: main
```

## 🐛 Solución de Problemas Rápida

### "Error loading files"
- ✅ Verificar conexión a Internet
- ✅ Verificar que el repo existe en GitHub
- ✅ Verificar que el branch existe
- ✅ Probar con otro repositorio público

### "No hay archivos seleccionados"
- ✅ El repo no tiene archivos .kt
- ✅ Prueba con otro repositorio que tenga Kotlin

### App se cierra inesperadamente
- ✅ Verificar que el emulador/dispositivo tiene API 24+
- ✅ Revisar logs en Android Studio (Logcat)

### Gradle sync failed
- ✅ Verificar conexión a Internet
- ✅ Invalidar caché: File → Invalidate Caches → Restart
- ✅ Limpiar proyecto: Build → Clean Project

## 📱 Características de la App

### Pantalla de Selección
- ✍️ Campos editables para owner, repo, branch
- 🔄 Botón de carga con indicador de progreso
- ☑️ Checkboxes para selección múltiple
- ✅ Validación antes de continuar

### Pantalla de Revisión
- 🎨 Resaltado de sintaxis estilo Darcula
- 📝 Campo de comentarios
- ◀️ ▶️ Navegación entre archivos
- 💾 Guardado inmediato de comentarios
- 📊 Indicador de progreso (archivo X de Y)

### Pantalla de Resumen
- 📋 Lista completa de comentarios
- 🏷️ Agrupados por nombre de archivo
- 🔄 Opción para nueva revisión

## 🎓 Próximos Pasos

Una vez que la app funcione, puedes:

1. **Revisar el código**: El archivo `MainActivity.kt` contiene toda la implementación
2. **Leer la arquitectura**: Consultar `ARCHITECTURE.md` para entender el diseño
3. **Explorar la API**: Ver `API_EXAMPLES.md` para ejemplos de uso de GitHub API
4. **Personalizar**: Modificar temas, colores, o agregar funcionalidades

## 🤝 ¿Necesitas Ayuda?

- 📖 Documentación completa en `README.md`
- 🏗️ Arquitectura del código en `ARCHITECTURE.md`
- 🔧 Guía de configuración en `SETUP_GUIDE.md`
- 🌐 Ejemplos de API en `API_EXAMPLES.md`

## 🚀 Tips Pro

### Usar tu propio GitHub Token
Para evitar límites de tasa, puedes agregar un token personal:

```kotlin
// En MainActivity.kt, línea ~80
private val client = OkHttpClient.Builder()
    .addInterceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "token TU_GITHUB_TOKEN")
            .build()
        chain.proceed(request)
    }
    .build()

private val retrofit = Retrofit.Builder()
    .baseUrl("https://api.github.com/")
    .client(client)  // Usar el cliente con autenticación
    .addConverterFactory(...)
    .build()
```

### Personalizar el Tema
Cambiar el esquema de colores en `MainActivity.kt`:

```kotlin
MaterialTheme(
    colorScheme = lightColorScheme(
        primary = Color(0xFF1976D2),      // Azul
        secondary = Color(0xFFFF9800),    // Naranja
        background = Color(0xFFFFFFFF)    // Blanco
    )
)
```

---

**¡Listo para revisar código!** 🎉
