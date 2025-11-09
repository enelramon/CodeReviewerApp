# Configuración de Gemini AI

Esta aplicación utiliza Gemini AI de Google para generar sugerencias automáticas de comentarios de código.

## Requisitos

1. Una cuenta de Google
2. Acceso a Google AI Studio
3. Una API Key de Gemini

## Pasos para obtener la API Key

1. **Visita Google AI Studio**
   - Ve a: https://makersuite.google.com/app/apikey
   - Inicia sesión con tu cuenta de Google

2. **Crear una API Key**
   - Haz clic en "Create API Key"
   - Selecciona un proyecto de Google Cloud o crea uno nuevo
   - Copia la API Key generada

3. **Configurar la API Key en el proyecto**
   
   Crea un archivo `local.properties` en la raíz del proyecto (si no existe):
   
   ```properties
   ## This file must *NOT* be checked into Version Control Systems,
   # as it contains information specific to your local configuration.
   
   # SDK Location
   sdk.dir=/path/to/your/Android/Sdk
   
   # Gemini API Key
   GEMINI_API_KEY=tu_api_key_aqui
   ```

   **Importante**: 
   - El archivo `local.properties` está incluido en `.gitignore` y no debe ser committeado
   - Reemplaza `tu_api_key_aqui` con tu API Key real
   - No compartas tu API Key públicamente

## Uso de la Función de Sugerencias

1. **En la Pantalla de Revisión**:
   - Selecciona un archivo para revisar
   - Presiona el botón **"Sugerir"** (con ícono de bombilla 💡)
   - Espera unos segundos mientras Gemini analiza el código
   - La sugerencia aparecerá automáticamente en el campo de comentarios
   - Puedes editar la sugerencia antes de guardarla
   - Presiona **"Guardar"** para añadir el comentario

2. **Sin API Key**:
   - Si no configuras una API Key, el botón "Sugerir" mostrará un mensaje de error
   - Todas las demás funciones de la app seguirán funcionando normalmente
   - Puedes escribir comentarios manualmente sin usar Gemini

## Límites de Uso

La API de Gemini tiene límites de uso gratuito:
- **Límite gratuito**: 60 requests por minuto
- Para proyectos grandes, considera los planes de pago de Google Cloud

Para más información sobre límites y precios:
- https://ai.google.dev/pricing

## Solución de Problemas

### "Gemini API key no configurada"
- Verifica que el archivo `local.properties` existe en la raíz del proyecto
- Asegúrate de que la línea `GEMINI_API_KEY=tu_clave` está correctamente escrita
- Reconstruye el proyecto (Build → Rebuild Project)

### "Error al generar sugerencia"
- Verifica tu conexión a Internet
- Comprueba que tu API Key es válida
- Verifica que no has excedido el límite de requests
- El archivo a revisar podría ser demasiado grande

### La sugerencia tarda mucho
- Las sugerencias pueden tardar entre 2-10 segundos dependiendo del tamaño del archivo
- Si tarda más de 30 segundos, puede haber un problema de red

## Seguridad

**NUNCA** debes:
- Commitear tu API Key al repositorio
- Compartir tu API Key públicamente
- Incluir tu API Key en el código fuente

Si accidentalmente expones tu API Key:
1. Ve a Google AI Studio
2. Revoca la API Key comprometida
3. Genera una nueva API Key

## Referencias

- [Google AI Studio](https://makersuite.google.com/)
- [Gemini API Documentation](https://ai.google.dev/docs)
- [Generative AI SDK for Android](https://github.com/google/generative-ai-android)
