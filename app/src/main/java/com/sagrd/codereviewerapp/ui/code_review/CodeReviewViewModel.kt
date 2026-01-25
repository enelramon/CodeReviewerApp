package com.sagrd.codereviewerapp.ui.code_review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.sagrd.codereviewerapp.BuildConfig
import com.sagrd.codereviewerapp.data.CodeComment
import com.sagrd.codereviewerapp.data.FileItem
import com.sagrd.codereviewerapp.data.FirestoreRepository
import com.sagrd.codereviewerapp.data.GitHubApi
import com.sagrd.codereviewerapp.data.GitHubRepository
import com.sagrd.codereviewerapp.data.ProjectType
import com.sagrd.codereviewerapp.data.ReviewHistoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.Date

// ViewModel
class CodeReviewViewModel : ViewModel() {
    // Gemini API key - In production, this should be stored securely
    private val geminiApiKey: String = BuildConfig.GEMINI_API_KEY

    private val generativeModel = if (geminiApiKey.isNotBlank()) {
        GenerativeModel(
            modelName = "gemini-2.5-flash-lite",
            apiKey = geminiApiKey,
            generationConfig = generationConfig {
                temperature = 0.7f
                topK = 40
                topP = 0.95f
                maxOutputTokens = 1024
            }
        )
    } else null

    private val json = Json { ignoreUnknownKeys = true }
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .client(
            OkHttpClient.Builder()
                .apply {
                    if (BuildConfig.DEBUG) {
                        addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                    }
                }
                .build()
        )
        .build()

    private val api = retrofit.create(GitHubApi::class.java)
    private val githubRepository = GitHubRepository(api)
    private val firestoreRepository = FirestoreRepository(FirebaseFirestore.getInstance())

    private val _uiState = MutableStateFlow(CodeReviewUiState())
    val uiState: StateFlow<CodeReviewUiState> = _uiState.asStateFlow()

    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val appId = "code-reviewer-app"

    private suspend fun ensureAuth() {
        if (auth.currentUser == null) {
            auth.signInAnonymously().await()
        }
    }
    init {
        viewModelScope.launch {
            ensureAuth()
        }
    }
    fun onEvent(event: CodeReviewUiEvent) {
        when (event) {
            is CodeReviewUiEvent.UpdateRepositoryUrl -> {
                _uiState.update { it.copy(repositoryUrl = event.url) }
                parseGitHubUrl(event.url)
            }

            is CodeReviewUiEvent.UpdateOwner -> {
                _uiState.update { it.copy(owner = event.owner) }
            }

            is CodeReviewUiEvent.UpdateRepo -> {
                _uiState.update { it.copy(repo = event.repo) }
            }

            is CodeReviewUiEvent.UpdateBranch -> {
                _uiState.update { it.copy(branch = event.branch) }
            }

            is CodeReviewUiEvent.UpdateProjectType -> {
                _uiState.update { it.copy(projectType = event.projectType) }
            }

            is CodeReviewUiEvent.LoadBranches -> {
                viewModelScope.launch {
                    loadBranches()
                }
            }

            is CodeReviewUiEvent.LoadFiles -> {
                // Launch a coroutine to perform the suspend operation
                viewModelScope.launch {
                    loadFiles()
                }
            }

            is CodeReviewUiEvent.ToggleFileSelection -> {
                toggleFileSelection(event.file)
            }

            is CodeReviewUiEvent.LoadFileContent -> {
                viewModelScope.launch {
                    loadFileContent(event.file)
                }
            }

            is CodeReviewUiEvent.LoadCommentForFile -> {
                loadCommentForFile(event.fileName)
            }

            is CodeReviewUiEvent.UpdateComment -> {
                _uiState.update { it.copy(currentComment = event.comment) }
            }

            is CodeReviewUiEvent.AddComment -> {
                addOrUpdateComment()
            }

            is CodeReviewUiEvent.SuggestComment -> {
                viewModelScope.launch {
                    suggestComment()
                }
            }

            is CodeReviewUiEvent.GenerateAISummary -> {
                viewModelScope.launch {
                    generateAISummary()
                }
            }

            is CodeReviewUiEvent.SaveReviewToHistory -> {
                viewModelScope.launch {
                    saveReviewToHistory()
                }
            }

            is CodeReviewUiEvent.LoadHistory -> {
                viewModelScope.launch {
                    loadHistory()
                }
            }

            is CodeReviewUiEvent.DeleteHistoryItem -> {
                viewModelScope.launch {
                    deleteHistoryItem(event.item)
                }
            }

            is CodeReviewUiEvent.UndoDeleteHistoryItem -> {
                viewModelScope.launch {
                    undoDeleteHistoryItem()
                }
            }

            is CodeReviewUiEvent.EditReview -> {
                editReview(event.item)
            }


            is CodeReviewUiEvent.ConsumeReviewSavedEvent -> {
                 resetState()
            }

            is CodeReviewUiEvent.ResetState -> {
                resetState()
            }
        }
    }

    private suspend fun loadFiles() {
        val owner = _uiState.value.owner
        val repo = _uiState.value.repo
        val branch = _uiState.value.branch
        val projectType = _uiState.value.projectType

        _uiState.update { it.copy(isLoading = true, error = null) }

        githubRepository.getFiles(owner, repo, branch, projectType).fold(
            onSuccess = { filesList ->
                _uiState.update { it.copy(files = filesList, isLoading = false) }
            },
            onFailure = { e ->
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Error loading files",
                        isLoading = false
                    )
                }
            }
        )
    }

    private suspend fun loadFileContent(file: FileItem) {
        val owner = _uiState.value.owner
        val repo = _uiState.value.repo
        _uiState.update {
            it.copy(
                isLoading = true,
                error = null,
                currentFileName = file.path
            )
        }
        
        githubRepository.getFileContent(owner, repo, file.sha).fold(
            onSuccess = { content ->
                _uiState.update { it.copy(currentFileContent = content, isLoading = false) }
            },
            onFailure = { e ->
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Error loading file content",
                        currentFileContent = "",
                        isLoading = false
                    )
                }
            }
        )
    }

    private suspend fun deleteHistoryItem(item: ReviewHistoryItem) {
        _uiState.update { it.copy(isLoading = true) }
        val result = firestoreRepository.deleteReviewHistory(item.id)
        if (result.isSuccess) {
            _uiState.update { state ->
                state.copy(
                    history = state.history.filter { it.id != item.id },
                    isLoading = false,
                    lastDeletedItem = item
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    error = "Error al eliminar: ${result.exceptionOrNull()?.message}",
                    isLoading = false
                )
            }
        }
    }

    private suspend fun undoDeleteHistoryItem() {
        val itemToRestore = _uiState.value.lastDeletedItem ?: return
        _uiState.update { it.copy(isLoading = true) }
        
        val result = firestoreRepository.saveReviewHistory(itemToRestore)
        result.fold(
            onSuccess = {
                loadHistory()
                _uiState.update { it.copy(lastDeletedItem = null) }
            },
            onFailure = { e ->
                _uiState.update {
                    it.copy(
                        error = "Error al restaurar: ${e.message}",
                        isLoading = false
                    )
                }
            }
        )
    }

    private fun toggleFileSelection(file: FileItem) {
        _uiState.update { currentState ->
            val updatedFiles = currentState.files.map {
                if (it.path == file.path) it.copy(isSelected = !it.isSelected)
                else it
            }
            currentState.copy(files = updatedFiles)
        }
    }

    private fun addOrUpdateComment() {
        _uiState.update { currentState ->
            if (currentState.currentComment.isNotBlank()) {
                val existingCommentIndex = currentState.comments.indexOfFirst {
                    it.fileName == currentState.currentFileName
                }

                val updatedComments = if (existingCommentIndex != -1) {
                    // Update existing comment
                    currentState.comments.toMutableList().apply {
                        set(existingCommentIndex, CodeComment(
                            currentState.currentFileName,
                            currentState.currentComment
                        )
                        )
                    }
                } else {
                    // Add new comment
                    currentState.comments + CodeComment(
                        currentState.currentFileName,
                        currentState.currentComment
                    )
                }

                currentState.copy(
                    comments = updatedComments,
                    currentComment = ""
                )
            } else {
                currentState
            }
        }
    }

    private fun loadCommentForFile(fileName: String) {
        _uiState.update { currentState ->
            val existingComment = currentState.comments.find { it.fileName == fileName }
            currentState.copy(
                currentComment = existingComment?.comment ?: ""
            )
        }
    }

    private suspend fun suggestComment() {
        if (generativeModel == null) {
            _uiState.update {
                it.copy(error = "Gemini API key no configurada. Agregue GEMINI_API_KEY en local.properties")
            }
            return
        }

        val currentState = _uiState.value
        if (currentState.currentFileContent.isBlank()) {
            return
        }

        _uiState.update { it.copy(isSuggesting = true, error = null) }

        try {
            val projectContext = when (currentState.projectType) {
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
                Eres un experto revisor de código especializado en ${currentState.projectType.displayName}.
                Analiza el siguiente código y proporciona un comentario de revisión constructivo en español.
                El comentario debe ser breve, específico y enfocarse en mejoras de:
                - Calidad del código
                - Mejores prácticas
                - Posibles bugs
                - Rendimiento
                - Legibilidad
                
                $projectContext
                
                Archivo: ${currentState.currentFileName}
                
                Código:
                ```
                ${currentState.currentFileContent}
                ```
                
                Proporciona solo el comentario de revisión, sin encabezados ni formato adicional.
            """.trimIndent()

            val response = withContext(Dispatchers.IO) {
                generativeModel.generateContent(prompt)
            }

            val suggestion = response.text ?: "No se pudo generar una sugerencia."

            _uiState.update {
                it.copy(
                    currentComment = suggestion,
                    isSuggesting = false
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    error = "Error al generar sugerencia: ${e.message}",
                    isSuggesting = false
                )
            }
        }
    }

    private suspend fun generateAISummary() {
        if (generativeModel == null) {
            _uiState.update {
                it.copy(error = "Gemini API key no configurada. Agregue GEMINI_API_KEY en local.properties")
            }
            return
        }

        val currentState = _uiState.value
        if (currentState.comments.isEmpty()) {
            return
        }

        _uiState.update { it.copy(isSuggesting = true, error = null) }

        try {
            val commentsText = currentState.comments.joinToString("\n\n") { comment ->
                "Archivo: ${comment.fileName}\nComentario: ${comment.comment}"
            }

            val prompt = """
                Eres un experto en análisis de código. A continuación se presentan los comentarios de una revisión de código para un proyecto ${currentState.projectType.displayName}.
                
                Genera un resumen ejecutivo en español que:
                - Identifique los temas principales encontrados
                - Resalte los problemas críticos
                - Sugiera áreas de mejora general
                - Sea conciso (máximo 300 palabras)
                
                Comentarios de la revisión:
                $commentsText
                
                Proporciona solo el resumen, sin encabezados adicionales.
            """.trimIndent()

            val response = withContext(Dispatchers.IO) {
                generativeModel.generateContent(prompt)
            }

            val summary = response.text ?: "No se pudo generar el resumen."

            _uiState.update {
                it.copy(
                    aiSummary = summary,
                    isSuggesting = false
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    error = "Error al generar resumen: ${e.message}",
                    isSuggesting = false
                )
            }
        }
    }

    private suspend fun saveReviewToHistory() {
        val currentState = _uiState.value
        if (currentState.comments.isEmpty()) {
            return
        }

        _uiState.update { it.copy(isSaving = true, error = null) }

        try {
            val historyItem = ReviewHistoryItem(
                owner = currentState.owner,
                repo = currentState.repo,
                branch = currentState.branch,
                date = Date(),
                comments = currentState.comments,
                aiSummary = currentState.aiSummary,
                projectType = currentState.projectType.name
            )

            val result = if (currentState.editingReviewId != null) {
                firestoreRepository.updateReviewHistory(currentState.editingReviewId, historyItem)
            } else {
                firestoreRepository.saveReviewHistory(historyItem)
            }

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = null,
                            reviewSaved = true
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            error = "Error al guardar en historial: ${e.message}",
                            isSaving = false
                        )
                    }
                }
            )
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    error = "Error al guardar: ${e.message}",
                    isSaving = false
                )
            }
        }
    }

    private suspend fun loadHistory() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        try {
            val result = firestoreRepository.loadReviewHistory()

            result.fold(
                onSuccess = { history ->
                    _uiState.update {
                        it.copy(
                            history = history,
                            isLoading = false
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            error = "Error al cargar historial: ${e.message}",
                            isLoading = false,
                            history = emptyList()
                        )
                    }
                }
            )
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    error = "Error al cargar historial: ${e.message}",
                    isLoading = false,
                    history = emptyList()
                )
            }
        }
    }

    private fun editReview(item: ReviewHistoryItem) {
        _uiState.update {
            it.copy(
                owner = item.owner,
                repo = item.repo,
                branch = item.branch,
                projectType = try { ProjectType.valueOf(item.projectType) } catch (e: Exception) { ProjectType.KOTLIN },
                comments = item.comments,
                aiSummary = item.aiSummary,
                editingReviewId = item.id,
                files = emptyList(), // Reset files before loading
                currentFileContent = "",
                currentFileName = ""
            )
        }

        viewModelScope.launch {
            loadFiles()
            // After loading files, select the ones that have comments
            _uiState.update { currentState ->
                val commentedFiles = currentState.comments.map { it.fileName }.toSet()
                val updatedFiles = currentState.files.map { file ->
                    if (commentedFiles.contains(file.path)) file.copy(isSelected = true) else file
                }
                currentState.copy(files = updatedFiles)
            }
        }
    }

    private fun resetState() {
        _uiState.update { currentState ->
            currentState.copy(
                files = emptyList(),
                currentFileContent = "",
                currentFileName = "",
                currentComment = "",
                comments = emptyList(),
                aiSummary = "",
                error = null,
                editingReviewId = null
                // Keep owner, repo, branch, and projectType
            )
        }
    }

    private fun parseGitHubUrl(url: String) {
        // Parse GitHub URL to extract owner and repo
        // Supports formats:
        // - https://github.com/owner/repo
        // - https://github.com/owner/repo.git
        // - github.com/owner/repo
        val regex = Regex("""(?:https?://)?(?:www\.)?github\.com/([^/]+)/([^/\.]+)(?:\.git)?""")
        val matchResult = regex.find(url)

        if (matchResult != null) {
            val (owner, repo) = matchResult.destructured
            _uiState.update {
                it.copy(
                    owner = owner,
                    repo = repo
                )
            }
        }
    }

    private suspend fun loadBranches() {
        val owner = _uiState.value.owner
        val repo = _uiState.value.repo

        if (owner.isBlank() || repo.isBlank()) {
            _uiState.update {
                it.copy(error = "Owner y Repo son requeridos para buscar branches")
            }
            return
        }

        _uiState.update { it.copy(isLoadingBranches = true, error = null) }
        
        githubRepository.getBranches(owner, repo).fold(
            onSuccess = { branchNames ->
                _uiState.update {
                    it.copy(
                        branches = branchNames,
                        isLoadingBranches = false,
                        // Set first branch as default if current branch is not in the list
                        branch = if (branchNames.isNotEmpty() && !branchNames.contains(it.branch)) {
                            branchNames.first()
                        } else {
                            it.branch
                        }
                    )
                }
            },
            onFailure = { e ->
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Error al cargar branches",
                        isLoadingBranches = false
                    )
                }
            }
        )
    }
}
