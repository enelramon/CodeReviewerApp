package com.sagrd.codereviewerapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.AccountTree
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.sagrd.codereviewerapp.navigation.Destinations
import com.sagrd.codereviewerapp.ui.theme.CodeReviewerAppTheme
import dev.snipme.highlights.Highlights
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
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.Base64
import java.util.Date

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CodeReviewerAppTheme {
                CodeReviewerNavHost()
            }
        }
    }
}


@Composable
fun CodeReviewerNavHost() {
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
                    },
                    onNavigateToHistory = {
                        navController.navigate(Destinations.History)
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
                        navController.navigate(Destinations.Selection) {
                            popUpTo(Destinations.Selection) { inclusive = true }
                        }
                    },
                    onNavigateToHistory = {
                        navController.navigate(Destinations.History) {
                             popUpTo(Destinations.Selection) { inclusive = false }
                        }
                    }
                )
            }
            composable<Destinations.History> {
                HistoryScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToReview = {
                        navController.navigate(Destinations.Review)
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
    onNavigateToReview: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Safety net: If we are in SelectionScreen, we should NOT be editing an existing review.
    // If editingReviewId is not null, it means we came from an abandoned edit session.
    LaunchedEffect(Unit) {
        if (uiState.editingReviewId != null) {
            viewModel.onEvent(CodeReviewUiEvent.ResetState)
        }
    }

    SeleccionScreenBody(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateToReview = onNavigateToReview,
        onNavigateToHistory = onNavigateToHistory
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SeleccionScreenBody(
    uiState: CodeReviewUiState,
    onEvent: (CodeReviewUiEvent) -> Unit,
    onNavigateToReview: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Selección de Archivos") },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Ver Historial",
                            tint = Color.White
                        )
                    }
                },
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
            // Project Type Selector
            Text(
                text = "Tipo de Proyecto",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ProjectType.values().size) { index ->
                    val projectType = ProjectType.values()[index]
                    ToggleButton(
                        checked = uiState.projectType == projectType,
                        onCheckedChange = { onEvent(CodeReviewUiEvent.UpdateProjectType(projectType)) },
                        modifier = Modifier.semantics { role = Role.RadioButton }
                    ) {
                        Text(projectType.displayName)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

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

            Button(
                onClick = {
                    onEvent(CodeReviewUiEvent.LoadFiles)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    LinearWavyProgressIndicator(modifier = Modifier.fillMaxWidth())
                } else {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = "Cargar archivos",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cargar Archivos")
                }
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
            // Load existing comment for current file
            viewModel.onEvent(CodeReviewUiEvent.LoadCommentForFile(selectedFiles[currentFileIndex].path))
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
                        containerColor = MaterialTheme.colorScheme.surface
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
                        SyntaxHighlightedCode(uiState.currentFileContent, uiState.projectType)
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
                                // Guardar comentario y avanzar automáticamente
                                viewModel.onEvent(CodeReviewUiEvent.AddComment)
                                if (uiState.currentComment.isNotBlank()) {
                                    if (currentFileIndex < selectedFiles.size - 1) {
                                        currentFileIndex++
                                    } else {
                                        onNavigateToSummary()
                                    }
                                }
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
fun SyntaxHighlightedCode(code: String, projectType: ProjectType = ProjectType.KOTLIN) {
    val highlights = remember(code, projectType) {
        mutableStateOf(
            Highlights
                .Builder(code = code)
                .theme(SyntaxThemes.atom())
                .language(projectType.syntaxLanguage)
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
    onNavigateToSelection: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.reviewSaved) {
        if (uiState.reviewSaved) {
            viewModel.onEvent(CodeReviewUiEvent.ConsumeReviewSavedEvent)
            onNavigateToHistory()
        }
    }

    fun shareComments() {
        if (uiState.comments.isEmpty()) return
        val shareText = buildString {
            appendLine("Resumen de Comentarios de Revisión")
            appendLine("--------------------------------")
            appendLine("Repositorio: ${uiState.owner}/${uiState.repo}")
            appendLine("Branch: ${uiState.branch}")
            appendLine()
            uiState.comments.forEachIndexed { index, c ->
                appendLine("${index + 1}. Archivo: ${c.fileName}")
                appendLine("   Comentario: ${c.comment}")
                appendLine()
            }
            if (uiState.aiSummary.isNotBlank()) {
                appendLine("Resumen de IA:")
                appendLine(uiState.aiSummary)
            }
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Revisión de Código: ${uiState.comments.size} comentarios")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir comentarios"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resumen de Comentarios") },
                actions = {
                    if (uiState.comments.isNotEmpty()) {
                        IconButton(onClick = { shareComments() }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Compartir comentarios",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
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
                // Generate AI Summary button
                Button(
                    onClick = { viewModel.onEvent(CodeReviewUiEvent.GenerateAISummary) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSuggesting && uiState.aiSummary.isBlank()
                ) {
                    if (uiState.isSuggesting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "Generar Resumen IA",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (uiState.isSuggesting) "Generando..." else "Generar Resumen con IA")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // AI Summary Card
                if (uiState.aiSummary.isNotBlank()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = "IA",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Resumen de IA",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = uiState.aiSummary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

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

            // Finalize and save to Firestore
            Button(
                onClick = {
                    viewModel.onEvent(CodeReviewUiEvent.SaveReviewToHistory)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.comments.isNotEmpty() && !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardando...")
                } else {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Finalizar y Guardar",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Finalizar y Guardar en Historial")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.onEvent(CodeReviewUiEvent.ResetState)
                    onNavigateToSelection()
                },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: CodeReviewViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToReview: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onEvent(CodeReviewUiEvent.LoadHistory)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Revisiones") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
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
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.history.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "Sin historial",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No hay revisiones en el historial",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                LazyColumn {
                    items(uiState.history, key = { it.id }) { historyItem ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            initialValue = SwipeToDismissBoxValue.Settled,
                            confirmValueChange = {
                                if (it == SwipeToDismissBoxValue.EndToStart) {
                                    viewModel.onEvent(CodeReviewUiEvent.DeleteHistoryItem(historyItem))
                                    true
                                } else {
                                    false
                                }
                            },
                            positionalThreshold = { it * 0.5f }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                val color = when (dismissState.dismissDirection) {
                                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                                    else -> Color.Transparent
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(vertical = 8.dp)
                                        .background(color)
                                        .padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Eliminar",
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            },
                            content = {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .clickable {
                                            viewModel.onEvent(CodeReviewUiEvent.EditReview(historyItem))
                                            onNavigateToReview()
                                        },
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${historyItem.owner}/${historyItem.repo}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                                                    .format(historyItem.date),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Branch: ${historyItem.branch}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "${historyItem.comments.size} comentarios",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
        
                                        if (historyItem.aiSummary.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            HorizontalDivider()
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Lightbulb,
                                                    contentDescription = "IA",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Resumen de IA:",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Text(
                                                text = historyItem.aiSummary,
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
        
                                        // Show first 2 comments
                                        historyItem.comments.take(2).forEach { comment ->
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "• ${comment.fileName}: ${comment.comment.take(50)}${if (comment.comment.length > 50) "..." else ""}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
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

data class ReviewHistoryItem(
    val id: String = "",
    val owner: String = "",
    val repo: String = "",
    val branch: String = "",
    val date: Date = Date(),
    val comments: List<CodeComment> = emptyList(),
    val aiSummary: String = "",
    val projectType: String = ProjectType.KOTLIN.name
) {
    // Convert to Map for Firestore
    fun toMap(): Map<String, Any> = mapOf(
        "owner" to owner,
        "repo" to repo,
        "branch" to branch,
        "date" to date,
        "comments" to comments.map { mapOf("fileName" to it.fileName, "comment" to it.comment) },
        "aiSummary" to aiSummary,
        "projectType" to projectType
    )

    companion object {
        // Create from Firestore document
        fun fromMap(id: String, map: Map<String, Any>): ReviewHistoryItem {
            val commentsList = (map["comments"] as? List<*>)?.mapNotNull { item ->
                (item as? Map<*, *>)?.let { commentMap ->
                    CodeComment(
                        fileName = commentMap["fileName"] as? String ?: "",
                        comment = commentMap["comment"] as? String ?: ""
                    )
                }
            } ?: emptyList()

            return ReviewHistoryItem(
                id = id,
                owner = map["owner"] as? String ?: "",
                repo = map["repo"] as? String ?: "",
                branch = map["branch"] as? String ?: "",
                date = (map["date"] as? com.google.firebase.Timestamp)?.toDate() ?: Date(),
                comments = commentsList,
                aiSummary = map["aiSummary"] as? String ?: "",
                projectType = map["projectType"] as? String ?: ProjectType.KOTLIN.name
            )
        }
    }
}

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
    val isSaving: Boolean = false,
    val error: String? = null,
    val currentFileContent: String = "",
    val currentFileName: String = "",
    val currentComment: String = "",
    val comments: List<CodeComment> = emptyList(),
    val projectType: ProjectType = ProjectType.KOTLIN,
    val aiSummary: String = "",
    val history: List<ReviewHistoryItem> = emptyList(),
    val reviewSaved: Boolean = false,
    val editingReviewId: String? = null
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
    data class UpdateProjectType(val projectType: ProjectType) : CodeReviewUiEvent
    data object LoadBranches : CodeReviewUiEvent
    data object LoadFiles : CodeReviewUiEvent
    data class ToggleFileSelection(val file: FileItem) : CodeReviewUiEvent
    data class LoadFileContent(val file: FileItem) : CodeReviewUiEvent
    data class LoadCommentForFile(val fileName: String) : CodeReviewUiEvent
    data class UpdateComment(val comment: String) : CodeReviewUiEvent
    data object AddComment : CodeReviewUiEvent
    data object SuggestComment : CodeReviewUiEvent
    data object GenerateAISummary : CodeReviewUiEvent
    data object SaveReviewToHistory : CodeReviewUiEvent
    data object LoadHistory : CodeReviewUiEvent
    data class DeleteHistoryItem(val item: ReviewHistoryItem) : CodeReviewUiEvent
    data class EditReview(val item: ReviewHistoryItem) : CodeReviewUiEvent
    data object ConsumeReviewSavedEvent : CodeReviewUiEvent
    data object ResetState : CodeReviewUiEvent
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

// Firestore Repository
class FirestoreRepository(private val firestore: FirebaseFirestore) {
    // AppId can be configured per build variant or from BuildConfig if needed
    // For now using a constant value. For production, consider making this dynamic.
    private val appId = "code-reviewer-app"

    suspend fun saveReviewHistory(historyItem: ReviewHistoryItem): Result<String> {
        return try {
            val docRef = firestore
                .collection("artifacts").document(appId)
                .collection("public").document("data")
                .collection("reviews")
                .document()

            withContext(Dispatchers.IO) {
                docRef.set(historyItem.toMap()).await()
            }
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteReviewHistory(id: String): Result<Unit> {
        return try {
            withContext(Dispatchers.IO) {
                firestore
                    .collection("artifacts").document(appId)
                    .collection("public").document("data")
                    .collection("reviews")
                    .document(id)
                    .delete()
                    .await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateReviewHistory(id: String, historyItem: ReviewHistoryItem): Result<Unit> {
        return try {
            withContext(Dispatchers.IO) {
                firestore
                    .collection("artifacts").document(appId)
                    .collection("public").document("data")
                    .collection("reviews")
                    .document(id)
                    .set(historyItem.toMap())
                    .await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loadReviewHistory(): Result<List<ReviewHistoryItem>> {
        return try {
            val snapshot = withContext(Dispatchers.IO) {
                firestore
                    .collection("artifacts").document(appId)
                    .collection("public").document("data")
                    .collection("reviews")
                    .get()
                    .await()
            }

            // Sort in memory by date descending (no orderBy in Firestore query as per requirements)
            // Note: For large datasets, consider implementing pagination or limiting query results
            val historyList = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { data ->
                    ReviewHistoryItem.fromMap(doc.id, data)
                }
            }.sortedByDescending { it.date }

            Result.success(historyList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// ViewModel
class CodeReviewViewModel : ViewModel() {
    // Gemini API key - In production, this should be stored securely
    private val geminiApiKey: String = com.sagrd.codereviewerapp.BuildConfig.GEMINI_API_KEY

    private val generativeModel = if (geminiApiKey.isNotBlank()) {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
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

    private suspend fun deleteHistoryItem(item: ReviewHistoryItem) {
        _uiState.update { it.copy(isLoading = true) }
        val result = firestoreRepository.deleteReviewHistory(item.id)
        if (result.isSuccess) {
            _uiState.update { state ->
                state.copy(
                    history = state.history.filter { it.id != item.id },
                    isLoading = false
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
                        ))
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
            onEvent = {},
            onNavigateToReview = {},
            onNavigateToHistory = {}
        )
    }
}