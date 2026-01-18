# Guía de Configuración Rápida - Code Reviewer App

## 🚀 Inicio Rápido

Sigue estos pasos para configurar y ejecutar la aplicación con todas las nuevas características.

## Paso 1: Configurar Gemini AI (Requerido)

1. Obtén una API Key gratuita:
   - Ve a [Google AI Studio](https://makersuite.google.com/app/apikey)
   - Inicia sesión con tu cuenta de Google
   - Crea una nueva API Key
   - Copia la key

2. Configura la API Key en el proyecto:
   ```bash
   # En la raíz del proyecto, crea/edita local.properties
   echo "GEMINI_API_KEY=tu_api_key_aqui" >> local.properties
   ```

3. Verifica que el archivo `local.properties` NO se suba a git:
   ```bash
   # Ya está en .gitignore, solo verificar
   cat .gitignore | grep local.properties
   ```

## Paso 2: Configurar Firebase (Opcional)

### Opción A: Sin Firebase (Funcionalidad Limitada)

Si no configuras Firebase:
- ✅ La app funcionará normalmente
- ✅ Podrás hacer revisiones de código
- ✅ Tendrás sugerencias de IA
- ❌ NO podrás guardar historial
- ❌ NO podrás ver revisiones pasadas

Para usar sin Firebase, simplemente salta al Paso 3.

### Opción B: Con Firebase (Recomendado)

1. **Crear Proyecto en Firebase:**
   ```
   1. Ve a https://console.firebase.google.com/
   2. Clic en "Agregar proyecto"
   3. Nombre: "CodeReviewerApp" (o el que prefieras)
   4. Sigue el asistente
   ```

2. **Agregar App Android:**
   ```
   1. En Firebase Console, clic en ícono Android
   2. Nombre del paquete: com.sagrd.codereviewerapp
   3. Descarga google-services.json
   4. Coloca en: app/google-services.json
   ```

3. **Habilitar Firestore:**
   ```
   1. En Firebase Console: Build → Firestore Database
   2. Clic en "Crear base de datos"
   3. Selecciona "Modo de prueba" (para desarrollo)
   4. Ubicación: us-central (o la más cercana)
   5. Clic en "Habilitar"
   ```

4. **Configurar Reglas (Temporal - Solo Desarrollo):**
   ```javascript
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /{document=**} {
         allow read, write: if true;
       }
     }
   }
   ```
   ⚠️ **IMPORTANTE**: Estas reglas son solo para desarrollo. Para producción, implementa reglas de seguridad apropiadas.

Ver [FIREBASE_SETUP.md](FIREBASE_SETUP.md) para instrucciones detalladas.

## Paso 3: Instalar y Ejecutar

1. **Abrir en Android Studio:**
   ```bash
   # Si no tienes el proyecto clonado
   git clone https://github.com/enelramon/CodeReviewerApp.git
   cd CodeReviewerApp
   
   # Abrir en Android Studio
   # File → Open → Seleccionar la carpeta del proyecto
   ```

2. **Sincronizar Gradle:**
   - Android Studio lo hará automáticamente
   - O manualmente: File → Sync Project with Gradle Files

3. **Ejecutar la App:**
   - Conecta un dispositivo Android o inicia un emulador
   - Clic en el botón "Run" (triángulo verde)
   - O presiona Shift + F10

## 📱 Primer Uso

### 1. Pantalla de Selección

```
1. Selecciona tipo de proyecto:
   [ Kotlin ]  [ Blazor ]

2. Ingresa URL del repositorio:
   https://github.com/usuario/repositorio

3. Clic en 🔍 para cargar branches

4. Selecciona el branch deseado

5. Clic en "Cargar Archivos"

6. Marca los archivos que quieres revisar

7. Clic en "Siguiente"
```

### 2. Pantalla de Revisión

```
Para cada archivo:
1. Lee el código (con resaltado de sintaxis)
2. Clic en 💡 para obtener sugerencia de IA
3. Escribe o edita el comentario
4. Clic en 💾 para guardar
5. Usa ◀ ▶ para navegar entre archivos
   (Los comentarios previos se cargan automáticamente)
```

### 3. Pantalla de Resumen

```
1. Revisa todos tus comentarios

2. (Opcional) Clic en "Generar Resumen con IA"
   - Análisis completo de la revisión
   - Temas principales encontrados
   - Recomendaciones generales

3. Clic en "Finalizar y Guardar en Historial"
   (Solo si configuraste Firebase)

4. O comparte los comentarios con el ícono 📤

5. Clic en "Nueva Revisión" para empezar otra
```

### 4. Ver Historial

```
Desde la pantalla de selección:
1. Clic en el ícono 🕐 (Historial) arriba a la derecha
2. Navega por revisiones pasadas
3. Ve resúmenes de IA y comentarios
4. Vuelve con ◀
```

## 🎨 Características Clave

### Tipos de Proyecto

**Kotlin:**
- Resaltado de sintaxis Kotlin
- Sugerencias enfocadas en:
  - Coroutines y flujos
  - Null safety
  - Convenciones de Kotlin
  - Arquitectura Android

**Blazor:**
- Resaltado de sintaxis C#
- Sugerencias enfocadas en:
  - Componentes Blazor
  - Data binding
  - Gestión de estado
  - Buenas prácticas .NET

### Persistencia de Comentarios

- Al navegar entre archivos, los comentarios se guardan automáticamente
- Volver a un archivo carga el comentario previo
- Actualizar un comentario no crea duplicados

### IA Contextual

- Sugerencias especializadas según el tipo de proyecto
- Análisis enfocado en mejores prácticas específicas
- Resumen ejecutivo de toda la revisión

## 🔧 Solución de Problemas

### "Gemini API key no configurada"

```bash
# Verifica que local.properties existe
ls -la local.properties

# Verifica el contenido
cat local.properties | grep GEMINI_API_KEY

# Si no existe, créalo:
echo "GEMINI_API_KEY=tu_clave_aqui" > local.properties

# Sincroniza Gradle en Android Studio
```

### "Error al guardar en historial"

```
Si no configuraste Firebase:
- La app funciona sin historial
- Solo ignora este error

Si configuraste Firebase:
1. Verifica que google-services.json esté en app/
2. Revisa las reglas de Firestore
3. Verifica conexión a Internet
4. Ve a Firebase Console para ver los datos
```

### "No carga los archivos del repositorio"

```
1. Verifica la URL del repositorio
2. Asegura conexión a Internet
3. Verifica que el repositorio sea público
4. Prueba con otro repositorio conocido:
   https://github.com/google/gson
```

### "La app no compila"

```bash
# Limpia y reconstruye
./gradlew clean
./gradlew build

# O en Android Studio:
# Build → Clean Project
# Build → Rebuild Project

# Verifica que tienes:
# - Android SDK instalado
# - Gradle sincronizado
# - Dependencias descargadas
```

## 📚 Documentación Adicional

Para más información detallada:

- 📖 [NUEVAS_CARACTERISTICAS.md](NUEVAS_CARACTERISTICAS.md) - Todas las características nuevas
- 🔥 [FIREBASE_SETUP.md](FIREBASE_SETUP.md) - Configuración detallada de Firebase
- 🤖 [GEMINI_SETUP.md](GEMINI_SETUP.md) - Configuración detallada de Gemini
- 📊 [RESUMEN_IMPLEMENTACION.md](RESUMEN_IMPLEMENTACION.md) - Resumen técnico completo
- 📘 [README.md](README.md) - Documentación principal

## 🆘 Ayuda y Soporte

Si tienes problemas:

1. Revisa esta guía
2. Consulta la documentación específica
3. Busca en los issues de GitHub
4. Abre un nuevo issue con:
   - Descripción del problema
   - Pasos para reproducir
   - Logs/capturas de pantalla
   - Información del entorno

## ✅ Checklist de Configuración

Marca lo que hayas completado:

- [ ] Clonar repositorio
- [ ] Abrir en Android Studio
- [ ] Crear `local.properties`
- [ ] Agregar `GEMINI_API_KEY`
- [ ] (Opcional) Configurar Firebase
- [ ] (Opcional) Agregar `google-services.json`
- [ ] Sincronizar Gradle
- [ ] Ejecutar la app
- [ ] Probar una revisión de código
- [ ] Obtener sugerencias de IA
- [ ] Guardar comentarios
- [ ] Ver resumen
- [ ] (Opcional) Guardar en historial
- [ ] (Opcional) Ver historial

## 🎉 ¡Listo!

Una vez completados estos pasos, tu app está configurada y lista para usar. Disfruta revisando código con el poder de la IA.

---

**Versión**: 2.0 con Firebase y IA Contextual
**Última actualización**: Enero 2026
