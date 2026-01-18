package com.sagrd.codereviewerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects
import com.sagrd.codereviewerapp.ui.theme.CodeReviewerAppTheme
import dev.snipme.highlights.Highlights
import dev.snipme.highlights.model.SyntaxLanguage
import dev.snipme.highlights.model.SyntaxThemes
import dev.snipme.kodeview.view.CodeTextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*

// --- Modelos ---

@Serializable
data class FileItem(val path: String = "", val sha: String = "")

@Serializable
data class CodeComment(val fileName: String = "", val comment: String = "")

@Serializable
data class ReviewHistoryItem(
    val id: String = "",
    val owner: String = "",
    val repo: String = "",
    val date: Long = System.currentTimeMillis(),
    val projectType: String = "",
    val comments: List<CodeComment> = emptyList(),
    val summary: String = ""
)

enum class ProjectType(val displayName: String) {
    KOTLIN("Kotlin"),
    BLAZOR("Blazor / C#")
}

data class CodeReviewUiState(
    val owner: String = "SagrarioSoftware",
    val repo: String = "",
    val projectType: ProjectType = ProjectType.KOTLIN,
    val files: List<FileItem> = emptyList(),
    val selectedFiles: List<FileItem> = emptyList(),
    val currentFileName: String = "",
    val currentFileContent: String = "",
    val currentComment: String = "",
    val comments: List<CodeComment> = emptyList(),
    val history: List<ReviewHistoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
)

// --- Navigation ---

@Serializable sealed class Destinations {
    @Serializable object Selection : Destinations()
    @Serializable object Review : Destinations()
    @Serializable object Summary : Destinations()
    @Serializable object History : Destinations()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CodeReviewerAppTheme {
                CodeReviewerApp()
            }
        }
    }
}

@Composable
fun CodeReviewerApp() {
    val navController = rememberNavController()
    val viewModel: CodeReviewViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    NavHost(navController = navController, startDestination = Destinations.Selection) {
        composable<Destinations.Selection> {
            SelectionScreen(
                viewModel = viewModel,
                onNavigateToReview = { navController.navigate(Destinations.Review) },
                onNavigateToHistory = {
                    viewModel.onEvent(CodeReviewUiEvent.FetchHistory)
                    navController.navigate(Destinations.History)
                }
            )
        }
        composable<Destinations.Review> {
            ReviewScreen(
                viewModel = viewModel,
                onNavigateToSummary = { navController.navigate(Destinations.Summary) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable<Destinations.Summary> {
            SummaryScreen(
                viewModel = viewModel,
                onFinish = {
                    viewModel.onEvent(CodeReviewUiEvent.SaveReviewToHistory)
                    navController.navigate(Destinations.Selection) {
                        popUpTo(Destinations.Selection) { inclusive = true }
                    }
                }
            )
        }
        composable<Destinations.History> {
            HistoryScreen(
                uiState = uiState,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

// --- Screens ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionScreen(viewModel: CodeReviewViewModel, onNavigateToReview: () -> Unit, onNavigateToHistory: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Revisión de Código") },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, contentDescription = "Historial")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = uiState.owner,
                onValueChange = { viewModel.onEvent(CodeReviewUiEvent.UpdateOwner(it)) },
                label = { Text("Dueño GitHub") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.repo,
                onValueChange = { viewModel.onEvent(CodeReviewUiEvent.UpdateRepo(it)) },
                label = { Text("Repositorio") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Tipo de Proyecto", modifier = Modifier.padding(top = 8.dp))
            Row {
                ProjectType.entries.forEach { type ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
                        viewModel.onEvent(CodeReviewUiEvent.UpdateProjectType(type))
                    }) {
                        RadioButton(selected = uiState.projectType == type, onClick = null)
                        Text(type.displayName, modifier = Modifier.padding(end = 8.dp))
                    }
                }
            }

            Button(onClick = { viewModel.onEvent(CodeReviewUiEvent.FetchFiles) }, modifier = Modifier.fillMaxWidth()) {
                Text("Cargar Archivos")
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(uiState.files) { file ->
                    ListItem(
                        headlineContent = { Text(file.path) },
                        trailingContent = {
                            Checkbox(checked = uiState.selectedFiles.contains(file), onCheckedChange = {
                                viewModel.onEvent(CodeReviewUiEvent.ToggleFileSelection(file))
                            })
                        },
                        modifier = Modifier.clickable { viewModel.onEvent(CodeReviewUiEvent.ToggleFileSelection(file)) }
                    )
                }
            }

            if (uiState.selectedFiles.isNotEmpty()) {
                Button(onClick = onNavigateToReview, modifier = Modifier.fillMaxWidth()) {
                    Text("Revisar ${uiState.selectedFiles.size} archivos")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(viewModel: CodeReviewViewModel, onNavigateToSummary: () -> Unit, onNavigateBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    var index by remember { mutableIntStateOf(0) }

    LaunchedEffect(index, uiState.selectedFiles) {
        if (uiState.selectedFiles.isNotEmpty()) {
            viewModel.onEvent(CodeReviewUiEvent.LoadFileContent(uiState.selectedFiles[index]))
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("${index + 1}/${uiState.selectedFiles.size}") }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text(uiState.currentFileName, fontWeight = FontWeight.Bold)
            Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color.LightGray.copy(0.2f))) {
                if (uiState.isLoading) CircularProgressIndicator(Modifier.align(Alignment.Center))
                else SyntaxCodeView(uiState.currentFileContent, uiState.currentFileName, uiState.projectType)
            }
            OutlinedTextField(
                value = uiState.currentComment,
                onValueChange = { viewModel.onEvent(CodeReviewUiEvent.UpdateComment(it)) },
                label = { Text("Nota del archivo") },
                modifier = Modifier.fillMaxWidth().height(100.dp)
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(onClick = { viewModel.onEvent(CodeReviewUiEvent.SaveCurrentComment) }) {
                    Icon(Icons.Default.Save, "Guardar")
                }
                Row {
                    if (index > 0) IconButton(onClick = { index-- }) { Icon(Icons.Default.NavigateBefore, null) }
                    if (index < uiState.selectedFiles.size - 1) IconButton(onClick = { index++ }) { Icon(Icons.Default.NavigateNext, null) }
                    else Button(onClick = onNavigateToSummary) { Text("Resumen") }
                }
            }
        }
    }
}

@Composable
fun SyntaxCodeView(code: String, fileName: String, projectType: ProjectType) {
    val lang = when {
        fileName.endsWith(".kt") -> SyntaxLanguage.KOTLIN
        fileName.endsWith(".cs") || projectType == ProjectType.BLAZOR -> SyntaxLanguage.CSHARP
        else -> SyntaxLanguage.KOTLIN
    }
    val highlights = Highlights.Builder().code(code).language(lang).theme(SyntaxThemes.monokai()).build()
    CodeTextView(highlights = highlights, modifier = Modifier.fillMaxSize())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(viewModel: CodeReviewViewModel, onFinish: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text("Resumen") }) }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            LazyColumn(Modifier.weight(1f)) {
                items(uiState.comments) { Text("${it.fileName}: ${it.comment}", modifier = Modifier.padding(8.dp)) }
            }
            Button(onClick = onFinish, modifier = Modifier.fillMaxWidth(), enabled = !uiState.isSaving) {
                if (uiState.isSaving) CircularProgressIndicator(Modifier.size(20.dp))
                else Text("Guardar en Historial y Finalizar")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(uiState: CodeReviewUiState, onNavigateBack: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = { Text("Historial") }, navigationIcon = {
            IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null) }
        })
    }) { padding ->
        LazyColumn(Modifier.padding(padding)) {
            items(uiState.history) { item ->
                Card(Modifier.fillMaxWidth().padding(8.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("${item.owner}/${item.repo}", fontWeight = FontWeight.Bold)
                        Text("Notas: ${item.comments.size}", style = MaterialTheme.typography.bodySmall)
                        Text(SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault()).format(Date(item.date)))
                    }
                }
            }
        }
    }
}

// --- ViewModel ---

sealed class CodeReviewUiEvent {
    data class UpdateOwner(val owner: String) : CodeReviewUiEvent()
    data class UpdateRepo(val repo: String) : CodeReviewUiEvent()
    data class UpdateProjectType(val type: ProjectType) : CodeReviewUiEvent()
    object FetchFiles : CodeReviewUiEvent()
    data class ToggleFileSelection(val file: FileItem) : CodeReviewUiEvent()
    data class LoadFileContent(val file: FileItem) : CodeReviewUiEvent()
    data class UpdateComment(val comment: String) : CodeReviewUiEvent()
    object SaveCurrentComment : CodeReviewUiEvent()
    object SaveReviewToHistory : CodeReviewUiEvent()
    object FetchHistory : CodeReviewUiEvent()
    object ResetState : CodeReviewUiEvent()
}

class CodeReviewViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CodeReviewUiState())
    val uiState = _uiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val appId = "code-reviewer-app"

    // ASEGURAR AUTH ANÓNIMA PARA PRODUCCIÓN
    private suspend fun ensureAuth() {
        if (auth.currentUser == null) {
            auth.signInAnonymously().await()
        }
    }

    fun onEvent(event: CodeReviewUiEvent) {
        when (event) {
            is CodeReviewUiEvent.UpdateOwner -> _uiState.update { it.copy(owner = event.owner) }
            is CodeReviewUiEvent.UpdateRepo -> _uiState.update { it.copy(repo = event.repo) }
            is CodeReviewUiEvent.UpdateProjectType -> _uiState.update { it.copy(projectType = event.type) }
            is CodeReviewUiEvent.FetchFiles -> fetchFiles()
            is CodeReviewUiEvent.ToggleFileSelection -> toggleFile(event.file)
            is CodeReviewUiEvent.LoadFileContent -> loadContent(event.file)
            is CodeReviewUiEvent.UpdateComment -> _uiState.update { it.copy(currentComment = event.comment) }
            is CodeReviewUiEvent.SaveCurrentComment -> saveComment()
            is CodeReviewUiEvent.SaveReviewToHistory -> saveToFirestore()
            is CodeReviewUiEvent.FetchHistory -> fetchHistory()
            is CodeReviewUiEvent.ResetState -> _uiState.update { CodeReviewUiState(owner = it.owner) }
        }
    }

    private fun fetchFiles() {
        // Simulación: Aquí iría tu llamada a Retrofit para GitHub
        _uiState.update { it.copy(files = listOf(FileItem("MainActivity.kt", "sha"), FileItem("Home.razor", "sha2"))) }
    }

    private fun toggleFile(file: FileItem) {
        _uiState.update {
            val list = if (it.selectedFiles.contains(file)) it.selectedFiles - file else it.selectedFiles + file
            it.copy(selectedFiles = list)
        }
    }

    private fun loadContent(file: FileItem) {
        val prev = _uiState.value.comments.find { it.fileName == file.path }?.comment ?: ""
        _uiState.update { it.copy(isLoading = true, currentFileName = file.path, currentComment = prev) }
        viewModelScope.launch {
            kotlinx.coroutines.delay(500) // Simulación
            _uiState.update { it.copy(currentFileContent = "Código de ejemplo para ${file.path}", isLoading = false) }
        }
    }

    private fun saveComment() {
        _uiState.update {
            val filtered = it.comments.filter { c -> c.fileName != it.currentFileName }
            it.copy(comments = filtered + CodeComment(it.currentFileName, it.currentComment))
        }
    }

    private fun saveToFirestore() {
        val state = _uiState.value
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSaving = true) }
                ensureAuth()
                val item = ReviewHistoryItem(
                    id = UUID.randomUUID().toString(),
                    owner = state.owner,
                    repo = state.repo,
                    projectType = state.projectType.displayName,
                    comments = state.comments
                )
                db.collection("artifacts").document(appId)
                    .collection("public").document("data")
                    .collection("reviews").document(item.id).set(item).await()

                _uiState.update { it.copy(isSaving = false, comments = emptyList(), selectedFiles = emptyList()) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    private fun fetchHistory() {
        viewModelScope.launch {
            try {
                ensureAuth()
                val result = db.collection("artifacts").document(appId)
                    .collection("public").document("data")
                    .collection("reviews").get().await()
                val list = result.toObjects<ReviewHistoryItem>().sortedByDescending { it.date }
                _uiState.update { it.copy(history = list) }
            } catch (e: Exception) { }
        }
    }
}