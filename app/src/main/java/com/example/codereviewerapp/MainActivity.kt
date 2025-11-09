package com.example.codereviewerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dev.snipme.highlights.Highlights
import dev.snipme.highlights.model.BoldHighlight
import dev.snipme.highlights.model.ColorHighlight
import dev.snipme.highlights.model.SyntaxLanguage
import dev.snipme.highlights.model.SyntaxThemes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.Base64

// Data Models
@Serializable
data class GitHubTree(
    val tree: List<GitHubTreeItem>
)

@Serializable
data class GitHubTreeItem(
    val path: String,
    val type: String,
    val sha: String
)

@Serializable
data class GitHubBlob(
    val content: String,
    val encoding: String
)

data class FileItem(
    val path: String,
    val sha: String,
    var isSelected: Boolean = false
)

data class CodeComment(
    val fileName: String,
    val comment: String
)

// UI State
data class CodeReviewUiState(
    val owner: String = "google",
    val repo: String = "gson",
    val branch: String = "main",
    val files: List<FileItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentFileContent: String = "",
    val currentFileName: String = "",
    val currentComment: String = "",
    val comments: List<CodeComment> = emptyList()
)

// UI Events
sealed interface CodeReviewUiEvent {
    data class UpdateOwner(val owner: String) : CodeReviewUiEvent
    data class UpdateRepo(val repo: String) : CodeReviewUiEvent
    data class UpdateBranch(val branch: String) : CodeReviewUiEvent
    data object LoadFiles : CodeReviewUiEvent
    data class ToggleFileSelection(val file: FileItem) : CodeReviewUiEvent
    data class LoadFileContent(val file: FileItem) : CodeReviewUiEvent
    data class UpdateComment(val comment: String) : CodeReviewUiEvent
    data object AddComment : CodeReviewUiEvent
}

// Retrofit API Interface
interface GitHubApi {
    @GET("repos/{owner}/{repo}/git/trees/{sha}")
    suspend fun getTree(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("sha") sha: String,
        @Query("recursive") recursive: Int = 1
    ): GitHubTree

    @GET("repos/{owner}/{repo}/git/blobs/{sha}")
    suspend fun getBlob(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("sha") sha: String
    ): GitHubBlob
}

// ViewModel
class CodeReviewViewModel : ViewModel() {
    private val json = Json { ignoreUnknownKeys = true }
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .client(OkHttpClient.Builder().build())
        .build()

    private val api = retrofit.create(GitHubApi::class.java)

    private val _uiState = MutableStateFlow(CodeReviewUiState())
    val uiState: StateFlow<CodeReviewUiState> = _uiState.asStateFlow()

    fun onEvent(event: CodeReviewUiEvent) {
        when (event) {
            is CodeReviewUiEvent.UpdateOwner -> {
                _uiState.update { it.copy(owner = event.owner) }
            }
            is CodeReviewUiEvent.UpdateRepo -> {
                _uiState.update { it.copy(repo = event.repo) }
            }
            is CodeReviewUiEvent.UpdateBranch -> {
                _uiState.update { it.copy(branch = event.branch) }
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
            is CodeReviewUiEvent.UpdateComment -> {
                _uiState.update { it.copy(currentComment = event.comment) }
            }
            is CodeReviewUiEvent.AddComment -> {
                addComment()
            }
        }
    }

    private suspend fun loadFiles() {
        val owner = _uiState.value.owner
        val repo = _uiState.value.repo
        val branch = _uiState.value.branch
        _uiState.update { it.copy(isLoading = true, error = null) }
        try {
            val tree = api.getTree(owner, repo, branch, 1)
            val filesList = tree.tree
                .filter { it.type == "blob" && it.path.endsWith(".kt") }
                .map { FileItem(it.path, it.sha) }
            _uiState.update { it.copy(files = filesList, isLoading = false) }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    error = e.message ?: "Error loading files",
                    isLoading = false
                )
            }
        }
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
        try {
            val blob = api.getBlob(owner, repo, file.sha)
            val decoded = if (blob.encoding == "base64") {
                String(Base64.getDecoder().decode(blob.content))
            } else {
                blob.content
            }
            _uiState.update { it.copy(currentFileContent = decoded, isLoading = false) }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    error = e.message ?: "Error loading file content",
                    currentFileContent = "",
                    isLoading = false
                )
            }
        }
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

    private fun addComment() {
        _uiState.update { currentState ->
            if (currentState.currentComment.isNotBlank()) {
                val updatedComments = currentState.comments + CodeComment(
                    currentState.currentFileName,
                    currentState.currentComment
                )
                currentState.copy(
                    comments = updatedComments,
                    currentComment = ""
                )
            } else {
                currentState
            }
        }
    }

    fun getSelectedFiles() = _uiState.value.files.filter { it.isSelected }
}

// Main Activity
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CodeReviewerApp()
        }
    }
}

@Composable
fun CodeReviewerApp() {
    val navController = rememberNavController()
    val viewModel: CodeReviewViewModel = viewModel()

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF6200EE),
            secondary = Color(0xFF03DAC6),
            background = Color(0xFFF5F5F5)
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NavHost(navController = navController, startDestination = "selection") {
                composable("selection") {
                    SelectionScreen(navController, viewModel)
                }
                composable("review") {
                    ReviewScreen(navController, viewModel)
                }
                composable("summary") {
                    SummaryScreen(navController, viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionScreen(navController: NavHostController, viewModel: CodeReviewViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Selección de Archivos") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Input fields for repo info
            OutlinedTextField(
                value = uiState.owner,
                onValueChange = { viewModel.onEvent(CodeReviewUiEvent.UpdateOwner(it)) },
                label = { Text("Owner") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.repo,
                onValueChange = { viewModel.onEvent(CodeReviewUiEvent.UpdateRepo(it)) },
                label = { Text("Repositorio") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.branch,
                onValueChange = { viewModel.onEvent(CodeReviewUiEvent.UpdateBranch(it)) },
                label = { Text("Branch") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Load button
            Button(
                onClick = {
                    viewModel.onEvent(CodeReviewUiEvent.LoadFiles)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                Text(if (uiState.isLoading) "Cargando..." else "Cargar Archivos")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error message
            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(8.dp)
                )
            }

            // Files list
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(uiState.files) { file ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.onEvent(CodeReviewUiEvent.ToggleFileSelection(file)) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = file.isSelected,
                            onCheckedChange = { viewModel.onEvent(CodeReviewUiEvent.ToggleFileSelection(file)) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = file.path)
                    }
                    Divider()
                }
            }

            // Next button
            Button(
                onClick = {
                    if (viewModel.getSelectedFiles().isNotEmpty()) {
                        navController.navigate("review")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = viewModel.getSelectedFiles().isNotEmpty()
            ) {
                Text("Siguiente (${viewModel.getSelectedFiles().size} archivos)")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(navController: NavHostController, viewModel: CodeReviewViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedFiles = viewModel.getSelectedFiles()
    var currentFileIndex by remember { mutableStateOf(0) }

    LaunchedEffect(currentFileIndex) {
        if (selectedFiles.isNotEmpty() && currentFileIndex < selectedFiles.size) {
            viewModel.onEvent(CodeReviewUiEvent.LoadFileContent(selectedFiles[currentFileIndex]))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Revisión de Código") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (selectedFiles.isEmpty()) {
                Text("No hay archivos seleccionados")
                Button(onClick = { navController.popBackStack() }) {
                    Text("Volver")
                }
            } else {
                // File name
                Text(
                    text = uiState.currentFileName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Progress indicator
                Text(
                    text = "Archivo ${currentFileIndex + 1} de ${selectedFiles.size}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Code viewer with syntax highlighting
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2B2B2B)
                    )
                ) {
                    if (uiState.isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        SyntaxHighlightedCode(uiState.currentFileContent)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Comment field
                OutlinedTextField(
                    value = uiState.currentComment,
                    onValueChange = { viewModel.onEvent(CodeReviewUiEvent.UpdateComment(it)) },
                    label = { Text("Comentario") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            viewModel.onEvent(CodeReviewUiEvent.AddComment)
                        },
                        enabled = uiState.currentComment.isNotBlank()
                    ) {
                        Text("Guardar Comentario")
                    }

                    Row {
                        if (currentFileIndex > 0) {
                            Button(
                                onClick = { currentFileIndex-- },
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text("Anterior")
                            }
                        }

                        if (currentFileIndex < selectedFiles.size - 1) {
                            Button(onClick = { currentFileIndex++ }) {
                                Text("Siguiente")
                            }
                        } else {
                            Button(onClick = { navController.navigate("summary") }) {
                                Text("Resumen")
                            }
                        }
                    }
                }
            }
        }
    }
}

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
        var lastIndex = 0
        highlights.getHighlights().forEach { highlight ->
            // Add text before highlight
            if (highlight.location.start > lastIndex) {
                append(code.substring(lastIndex, highlight.location.start))
            }

            // Add highlighted text
            val text = code.substring(highlight.location.start, highlight.location.end)
            when (highlight) {
                is ColorHighlight -> {
                    withStyle(SpanStyle(color = Color(highlight.rgb))) {
                        append(text)
                    }
                }
                is BoldHighlight -> {
                    withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) {
                        append(text)
                    }
                }
                else -> append(text)
            }
            lastIndex = highlight.location.end
        }

        // Add remaining text
        if (lastIndex < code.length) {
            append(code.substring(lastIndex))
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = annotatedString,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = Color.White,
                lineHeight = 18.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(navController: NavHostController, viewModel: CodeReviewViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resumen de Comentarios") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (uiState.comments.isEmpty()) {
                Text(
                    text = "No hay comentarios guardados",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(uiState.comments) { comment ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = comment.fileName,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = comment.comment,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navController.navigate("selection") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Nueva Revisión")
            }
        }
    }
}
