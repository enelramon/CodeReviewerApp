package com.sagrd.codereviewerapp.data

import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiRepository(private val generativeModel: GenerativeModel?) {

    suspend fun suggestComment(
        fileContent: String,
        fileName: String,
        projectType: ProjectType
    ): Result<String> = withContext(Dispatchers.IO) {
        if (generativeModel == null) {
            return@withContext Result.failure(Exception("Gemini API key no configurada."))
        }

        try {
            val projectContext = when (projectType) {
                ProjectType.KOTLIN -> """
                    Este es código Kotlin. Enfócate en:
                    - Uso correcto de coroutines y flujos
                    - Null safety y manejo de tipos
                    - Convenciones de Kotlin (data classes, extension functions, etc.)
                    - Patrones de arquitectura Android (MVVM, Repository, etc.)
                """.trimIndent()
                ProjectType.BLAZOR -> """
                    Este es código Blazor (C#). Enfócate en:
                    - Componentes Blazor y ciclo de vida
                    - Data binding y eventos
                    - Gestión de estado
                    - Buenas prácticas de C# y .NET
                    - Patrones de arquitectura web
                """.trimIndent()
            }

            val prompt = """
                Eres un experto revisor de código especializado en ${projectType.displayName}.
                Analiza el siguiente código y proporciona un comentario de revisión constructivo en español.
                El comentario debe ser breve, específico y enfocarse en mejoras de:
                - Calidad del código
                - Mejores prácticas
                - Posibles bugs
                - Rendimiento
                - Legibilidad
                
                $projectContext
                
                Archivo: $fileName
                
                Código:
                ```
                $fileContent
                ```
                
                Proporciona solo el comentario de revisión, sin encabezados ni formato adicional.
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            val suggestion = response.text ?: "No se pudo generar una sugerencia."
            Result.success(suggestion)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateAISummary(
        comments: List<CodeComment>,
        projectType: ProjectType
    ): Result<String> = withContext(Dispatchers.IO) {
        if (generativeModel == null) {
            return@withContext Result.failure(Exception("Gemini API key no configurada."))
        }

        if (comments.isEmpty()) {
            return@withContext Result.failure(Exception("No hay comentarios para resumir."))
        }

        try {
            val commentsText = comments.joinToString("\n\n") { comment ->
                "Archivo: ${comment.fileName}\nComentario: ${comment.comment}"
            }

            val prompt = """
                Eres un experto en análisis de código. A continuación se presentan los comentarios de una revisión de código para un proyecto ${projectType.displayName}.
                
                Genera un resumen ejecutivo en español que:
                - Identifique los temas principales encontrados
                - Resalte los problemas críticos
                - Sugiera áreas de mejora general
                - Sea conciso (máximo 300 palabras)
                
                Comentarios de la revisión:
                $commentsText
                
                Proporciona solo el resumen, sin encabezados adicionales.
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            val summary = response.text ?: "No se pudo generar el resumen."
            Result.success(summary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
