package com.sagrd.codereviewerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.sagrd.codereviewerapp.navigation.Destinations
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
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.Base64

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


    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(navController = navController, startDestination = Destinations.Selection) {
            composable<Destinations.Selection> {
                SelectionScreen(
                    viewModel = viewModel,
                    onNavigateToReview = {
                        navController.navigate(Destinations.Review)
                    }
                )
            }
            composable<Destinations.Review> {
                ReviewScreen(
                    viewModel = viewModel,
                    onNavigateToSummary = {
                        navController.navigate(Destinations.Summary)
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable<Destinations.Summary> {
                SummaryScreen(
                    viewModel = viewModel,
                    onNavigateToSelection = {
                        navController.navigate(Destinations.Selection)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionScreen(
    viewModel: CodeReviewViewModel,
    onNavigateToReview: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    SeleccionScreenBody(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateToReview = onNavigateToReview
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeleccionScreenBody(
    uiState: CodeReviewUiState,
    onEvent: (CodeReviewUiEvent) -> Unit,
    onNavigateToReview: () -> Unit
) {
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
            // Repository URL input with search button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.repositoryUrl,
                    onValueChange = { onEvent(CodeReviewUiEvent.UpdateRepositoryUrl(it)) },
                    label = { Text("URL del Repositorio") },
                    placeholder = { Text("https://github.com/owner/repo.git") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Button(
                    onClick = {
                        onEvent(CodeReviewUiEvent.LoadBranches)
                    },
                    enabled = !uiState.isLoadingBranches,
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            /* // Input fields for repo info
             OutlinedTextField(
                 value = uiState.owner,
                 onValueChange = { onEvent(CodeReviewUiEvent.UpdateOwner(it)) },
                 label = { Text("Owner") },
                 leadingIcon = {
                     Icon(
                         imageVector = Icons.Default.Person,
                         contentDescription = "Owner"
                     )
                 },
                 modifier = Modifier.fillMaxWidth()
             )
             Spacer(modifier = Modifier.height(8.dp))
             OutlinedTextField(
                 value = uiState.repo,
                 onValueChange = { onEvent(CodeReviewUiEvent.UpdateRepo(it)) },
                 label = { Text("Repositorio") },
                 leadingIcon = {
                     Icon(
                         imageVector = Icons.Default.Storage,
                         contentDescription = "Repositorio"
                     )
                 },
                 modifier = Modifier.fillMaxWidth()
             )*/
            Spacer(modifier = Modifier.height(8.dp))

            // Branch selection
            BranchSelector(
                branches = uiState.branches,
                selectedBranch = uiState.branch,
                onBranchSelected = { branchName ->
                    onEvent(CodeReviewUiEvent.UpdateBranch(branchName))
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Load button
            Button(
                onClick = {
                    onEvent(CodeReviewUiEvent.LoadFiles)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = "Cargar archivos",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (uiState.isLoading) "Cargando..." else "Cargar Archivos")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error message
            uiState.error?.let { error ->
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Files list
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(uiState.files) { file ->
                    FileListItem(
                        file = file,
                        onToggleSelection = {
                            onEvent(
                                CodeReviewUiEvent.ToggleFileSelection(it)
                            )
                        }
                    )
                    HorizontalDivider()
                }
            }

            // Next button
            Button(
                onClick = {
                    if (uiState.selectedFiles.isNotEmpty()) {
                        onNavigateToReview()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.selectedFiles.isNotEmpty()
            ) {
                Text("Siguiente (${uiState.selectedFiles.size} archivos)")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Siguiente",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BranchSelector(
    branches: List<String>,
    selectedBranch: String,
    onBranchSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (branches.isNotEmpty()) {
        Column(modifier = modifier) {
            Text(
                text = "Seleccionar Branch",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
            ) {
                itemsIndexed(branches) { index, branchName ->
                    ToggleButton(
                        checked = selectedBranch == branchName,
                        onCheckedChange = { onBranchSelected(branchName) },
                        shapes = when (index) {
                            0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                            branches.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                            else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                        },
                        modifier = Modifier.semantics { role = Role.RadioButton }
                    ) {
                        Icon(
                            imageVector = if (selectedBranch == branchName) Icons.Filled.AccountTree else Icons.Outlined.AccountTree,
                            contentDescription = "Branch",
                        )
                        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                        Text(branchName)
                    }
                }
            }
        }
    } else {
        OutlinedTextField(
            value = selectedBranch,
            onValueChange = onBranchSelected,
            label = { Text("Branch") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.AccountTree,
                    contentDescription = "Branch"
                )
            },
            modifier = modifier.fillMaxWidth()
        )
    }
}

@Composable
fun FileListItem(
    file: FileItem,
    onToggleSelection: (FileItem) -> Unit,
    modifier: Modifier = Modifier
) {
    // Separate filename from directory path
    val lastSlashIndex = file.path.lastIndexOf('/')
    val directoryPath = if (lastSlashIndex > 0) file.path.substring(0, lastSlashIndex + 1) else ""
    val fileName = if (lastSlashIndex >= 0) file.path.substring(lastSlashIndex + 1) else file.path

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onToggleSelection(file) }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = file.isSelected,
            onCheckedChange = { onToggleSelection(file) }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            // Filename - bold and more visible with file icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.InsertDriveFile,
                    contentDescription = "File",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            // Directory path - smaller and more subtle with folder icon
            if (directoryPath.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "Folder",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = directoryPath,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    viewModel: CodeReviewViewModel,
    onNavigateToSummary: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedFiles = uiState.selectedFiles
    var currentFileIndex by remember { mutableIntStateOf(0) }

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
                Button(onClick = { onNavigateBack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
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
                        containerColor = Color.White
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
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.onEvent(CodeReviewUiEvent.SuggestComment)
                            },
                            enabled = !uiState.isSuggesting && !uiState.isLoading
                        ) {
                            if (uiState.isSuggesting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = "Sugerir",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.onEvent(CodeReviewUiEvent.AddComment)
                            },
                            enabled = uiState.currentComment.isNotBlank()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Guardar",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Row {
                        if (currentFileIndex > 0) {
                            Button(
                                onClick = { currentFileIndex-- },
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NavigateBefore,
                                    contentDescription = "Anterior",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        if (currentFileIndex < selectedFiles.size - 1) {
                            Button(onClick = { currentFileIndex++ }) {
                                Icon(
                                    imageVector = Icons.Default.NavigateNext,
                                    contentDescription = "Siguiente",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else {
                            Button(onClick = { onNavigateToSummary() }) {
                                Text("Resumen")
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "Resumen",
                                    modifier = Modifier.size(20.dp)
                                )
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
        mutableStateOf(
            Highlights
                .Builder(code = code)
                .theme(SyntaxThemes.atom())
                .language(SyntaxLanguage.KOTLIN)
                .build()
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        CodeTextView(highlights = highlights.value)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    viewModel: CodeReviewViewModel,
    onNavigateToSelection: () -> Unit
) {
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
                onClick = { onNavigateToSelection() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Nueva revisión",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nueva Revisión")
            }
        }
    }
}

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

@Serializable
data class GitHubBranch(
    val name: String,
    val commit: GitHubCommit
)

@Serializable
data class GitHubCommit(
    val sha: String
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
    val repositoryUrl: String = "https://github.com/enelramon/CodeReviewerApp.git",
    val owner: String = "enelramon",
    val repo: String = "CodeReviewerApp",
    val branch: String = "master",
    val branches: List<String> = emptyList(),
    val isLoadingBranches: Boolean = false,
    val files: List<FileItem> = emptyList(),
    val isLoading: Boolean = false,
    val isSuggesting: Boolean = false,
    val error: String? = null,
    val currentFileContent: String = "",
    val currentFileName: String = "",
    val currentComment: String = "",
    val comments: List<CodeComment> = emptyList()
) {
    val selectedFiles: List<FileItem>
        get() = files.filter { it.isSelected }
}

// UI Events
sealed interface CodeReviewUiEvent {
    data class UpdateRepositoryUrl(val url: String) : CodeReviewUiEvent
    data class UpdateOwner(val owner: String) : CodeReviewUiEvent
    data class UpdateRepo(val repo: String) : CodeReviewUiEvent
    data class UpdateBranch(val branch: String) : CodeReviewUiEvent
    data object LoadBranches : CodeReviewUiEvent
    data object LoadFiles : CodeReviewUiEvent
    data class ToggleFileSelection(val file: FileItem) : CodeReviewUiEvent
    data class LoadFileContent(val file: FileItem) : CodeReviewUiEvent
    data class UpdateComment(val comment: String) : CodeReviewUiEvent
    data object AddComment : CodeReviewUiEvent
    data object SuggestComment : CodeReviewUiEvent
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

    @GET("repos/{owner}/{repo}/branches")
    suspend fun getBranches(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): List<GitHubBranch>
}

// ViewModel
class CodeReviewViewModel : ViewModel() {
    // Gemini API key - In production, this should be stored securely
    private val geminiApiKey = BuildConfig.GEMINI_API_KEY.takeIf { it.isNotBlank() } ?: ""

    private val generativeModel = if (geminiApiKey.isNotBlank()) {
        GenerativeModel(
            modelName = "gemini-flash-latest",
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
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build()
        )
        .build()

    private val api = retrofit.create(GitHubApi::class.java)

    private val _uiState = MutableStateFlow(CodeReviewUiState())
    val uiState: StateFlow<CodeReviewUiState> = _uiState.asStateFlow()

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

            is CodeReviewUiEvent.UpdateComment -> {
                _uiState.update { it.copy(currentComment = event.comment) }
            }

            is CodeReviewUiEvent.AddComment -> {
                addComment()
            }

            is CodeReviewUiEvent.SuggestComment -> {
                viewModelScope.launch {
                    suggestComment()
                }
            }
        }
    }

    private suspend fun loadFiles() {
        val owner = _uiState.value.owner
        val repo = _uiState.value.repo
        val branch = _uiState.value.branch
        _uiState.update { it.copy(isLoading = true, error = null) }
        try {
            val tree = withContext(Dispatchers.IO) {
                api.getTree(owner, repo, branch, 1)
            }
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
            val blob = withContext(Dispatchers.IO) {
                api.getBlob(owner, repo, file.sha)
            }
            val decoded = if (blob.encoding == "base64") {
                String(Base64.getDecoder().decode(blob.content.replace("\\s".toRegex(), "")))
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
            val prompt = """
                Eres un experto revisor de código. Analiza el siguiente código y proporciona un comentario de revisión constructivo en español.
                El comentario debe ser breve, específico y enfocarse en mejoras de:
                - Calidad del código
                - Mejores prácticas
                - Posibles bugs
                - Rendimiento
                - Legibilidad
                
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
        try {
            val branchesList = withContext(Dispatchers.IO) {
                api.getBranches(owner, repo)
            }
            val branchNames = branchesList.map { it.name }
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
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    error = e.message ?: "Error al cargar branches",
                    isLoadingBranches = false
                )
            }
        }
    }
}

@Preview
@Composable
private fun SeleccionScreenPreview() {
    CodeReviewerAppTheme {
        SeleccionScreenBody(
            uiState = CodeReviewUiState(
                files = listOf(
                    FileItem("file1.kt", "sha1"),
                    FileItem("file2.kt", "sha2")
                )
            ),
            onEvent = {}
        ) { }
    }
}