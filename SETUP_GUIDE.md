# Guía de Configuración del Proyecto

## Requisitos Previos

1. **Java Development Kit (JDK)**: Instalar JDK 11 o superior
   ```bash
   java -version
   ```

2. **Android SDK**: Necesitas tener Android SDK instalado (se puede instalar con Android Studio)

3. **Gradle**: El proyecto incluye Gradle Wrapper, por lo que no necesitas instalarlo manualmente

## Pasos para Compilar el Proyecto

### Opción 1: Usando Android Studio (Recomendado)

1. Abrir Android Studio
2. Seleccionar "Open an Existing Project"
3. Navegar hasta la carpeta del proyecto y seleccionarla
4. Esperar a que Gradle sincronice las dependencias
5. Una vez sincronizado, hacer clic en "Run" o presionar Shift+F10

### Opción 2: Usando la Línea de Comandos

1. Navegar hasta el directorio del proyecto:
   ```bash
   cd CodeReviewerApp
   ```

2. Dar permisos de ejecución al gradlew (en Linux/Mac):
   ```bash
   chmod +x gradlew
   ```

3. Compilar el proyecto:
   ```bash
   ./gradlew build
   ```

4. Para instalar en un dispositivo conectado:
   ```bash
   ./gradlew installDebug
   ```

## Solución de Problemas Comunes

### Error: SDK no encontrado

Si recibes un error sobre el SDK de Android no encontrado:

1. Crear un archivo `local.properties` en la raíz del proyecto
2. Agregar la ruta al SDK:
   ```properties
   sdk.dir=/ruta/a/tu/Android/Sdk
   ```

En Windows:
```properties
sdk.dir=C\:\\Users\\TuUsuario\\AppData\\Local\\Android\\Sdk
```

En Mac:
```properties
sdk.dir=/Users/TuUsuario/Library/Android/sdk
```

En Linux:
```properties
sdk.dir=/home/TuUsuario/Android/Sdk
```

### Error de Conexión a Internet

Si hay problemas descargando dependencias:

1. Verificar la conexión a Internet
2. Si estás detrás de un proxy, configurarlo en `gradle.properties`:
   ```properties
   systemProp.http.proxyHost=proxy.company.com
   systemProp.http.proxyPort=8080
   systemProp.https.proxyHost=proxy.company.com
   systemProp.https.proxyPort=8080
   ```

### Error de Memoria

Si Gradle se queda sin memoria:

Editar `gradle.properties` y aumentar la memoria:
```properties
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
```

## Dependencias del Proyecto

El proyecto utiliza las siguientes dependencias principales:

- **Jetpack Compose**: 2023.10.01 (BOM)
- **Retrofit**: 2.9.0
- **Kotlinx Serialization**: 1.6.0
- **dev.snipme:highlights**: 0.7.0
- **Navigation Compose**: 2.7.5

Todas las dependencias se descargan automáticamente al compilar el proyecto.

## Verificación de la Instalación

Para verificar que todo está correctamente configurado:

```bash
./gradlew check
```

Este comando ejecutará las verificaciones del proyecto sin crear el APK.

## Configuración del Emulador

Si no tienes un emulador configurado:

1. Abrir Android Studio
2. Ir a Tools > Device Manager
3. Crear un nuevo dispositivo virtual
4. Seleccionar una imagen del sistema (API 24 o superior)
5. Finalizar la configuración

## Ejecución de la Aplicación

Una vez compilado:

1. Conectar un dispositivo Android con depuración USB activada, o
2. Iniciar un emulador
3. Ejecutar:
   ```bash
   ./gradlew installDebug
   ```

La aplicación aparecerá en el dispositivo/emulador con el nombre "Code Reviewer".
