# Firebase Setup Guide

## Overview

Esta aplicación utiliza Firebase Firestore para almacenar el historial de revisiones de código. A continuación se describen los pasos para configurar Firebase en tu proyecto.

## Requisitos Previos

- Cuenta de Google/Firebase
- Android Studio instalado
- Proyecto de CodeReviewerApp clonado

## Pasos de Configuración

### 1. Crear un Proyecto en Firebase

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Haz clic en "Agregar proyecto" o "Add project"
3. Ingresa un nombre para tu proyecto (ej: "CodeReviewerApp")
4. Sigue los pasos del asistente
5. Espera a que se cree el proyecto

### 2. Agregar una App Android al Proyecto

1. En la consola de Firebase, selecciona tu proyecto
2. Haz clic en el ícono de Android para agregar una app Android
3. Ingresa el nombre del paquete: `com.sagrd.codereviewerapp`
4. (Opcional) Ingresa un apodo para la app
5. Descarga el archivo `google-services.json`

### 3. Configurar el Archivo google-services.json

1. Copia el archivo `google-services.json` descargado
2. Pégalo en la carpeta `app/` de tu proyecto Android:
   ```
   CodeReviewerApp/
   └── app/
       └── google-services.json
   ```

**Nota**: El archivo `google-services.json` está en `.gitignore` y no se subirá al repositorio.

### 4. Configurar Firestore Database

1. En la consola de Firebase, ve a "Build" → "Firestore Database"
2. Haz clic en "Crear base de datos" o "Create database"
3. Selecciona modo de producción o modo de prueba:
   - **Modo de prueba**: Permitirá lectura/escritura sin autenticación durante 30 días (recomendado para desarrollo)
   - **Modo de producción**: Requiere configurar reglas de seguridad
4. Selecciona la ubicación de tu base de datos (ej: us-central)
5. Haz clic en "Habilitar"

### 5. Configurar Reglas de Seguridad (Opcional para Producción)

Si elegiste modo de producción, configura las reglas de Firestore:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Permitir acceso a la colección de historial
    match /artifacts/{appId}/public/data/history/reviews/{document=**} {
      allow read, write: if true; // Cambia esto según tus necesidades de seguridad
    }
  }
}
```

Para desarrollo/pruebas, puedes usar estas reglas más permisivas:

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

**⚠️ ADVERTENCIA**: Las reglas permisivas anteriores son SOLO para desarrollo. En producción, implementa reglas de seguridad apropiadas.

## Estructura de Datos en Firestore

La aplicación guarda los datos del historial en la siguiente estructura:

```
artifacts/
└── {appId}/
    └── public/
        └── data/
            └── history/
                └── reviews/
                    └── {reviewId}/
                        ├── owner: String
                        ├── repo: String
                        ├── branch: String
                        ├── date: Timestamp
                        ├── projectType: String
                        ├── aiSummary: String
                        └── comments: Array[
                            {
                                fileName: String,
                                comment: String
                            }
                        ]
```

### Campo `appId`

Por defecto, la aplicación usa `"code-reviewer-app"` como `appId`. Puedes cambiarlo en la clase `FirestoreRepository` en `MainActivity.kt`:

```kotlin
class FirestoreRepository(private val firestore: FirebaseFirestore) {
    private val appId = "code-reviewer-app" // Cambia esto si lo deseas
    // ...
}
```

## Verificar la Configuración

1. Abre el proyecto en Android Studio
2. Sincroniza el proyecto con Gradle
3. Ejecuta la aplicación en un emulador o dispositivo físico
4. Completa una revisión de código y guárdala en el historial
5. Ve a Firebase Console → Firestore Database
6. Verifica que se haya creado la estructura de datos con tu revisión

## Solución de Problemas

### Error: "google-services.json is missing"

- Asegúrate de haber copiado el archivo `google-services.json` en la carpeta `app/`
- Limpia y reconstruye el proyecto: Build → Clean Project → Build → Rebuild Project

### Error: "Default FirebaseApp is not initialized"

- Verifica que el archivo `google-services.json` esté correctamente configurado
- Asegúrate de que el nombre del paquete en Firebase coincida con el de tu app
- Sincroniza el proyecto con Gradle

### Error: "PERMISSION_DENIED"

- Revisa las reglas de seguridad en Firebase Console
- Para desarrollo, usa reglas permisivas temporalmente
- Verifica que Firestore esté habilitado en tu proyecto

## Alternativa: Usar sin Firebase (Opcional)

Si prefieres no usar Firebase, puedes modificar el código para:

1. Comentar o eliminar las llamadas a `FirestoreRepository`
2. Usar almacenamiento local (Room, SharedPreferences, o archivos JSON)
3. El resto de las funcionalidades de la app seguirán funcionando normalmente

Sin embargo, perderás la funcionalidad de historial sincronizado en la nube.

## Recursos Adicionales

- [Documentación oficial de Firebase](https://firebase.google.com/docs)
- [Firestore para Android](https://firebase.google.com/docs/firestore/quickstart)
- [Reglas de seguridad de Firestore](https://firebase.google.com/docs/firestore/security/get-started)
