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

    var owner by mutableStateOf("google")
    var repo by mutableStateOf("gson")
    var branch by mutableStateOf("main")
    var files by mutableStateOf<List<FileItem>>(emptyList())
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    
    var currentFileContent by mutableStateOf("")
    var currentFileName by mutableStateOf("")
    var currentComment by mutableStateOf("")
    
    val comments = mutableStateListOf<CodeComment>()

    suspend fun loadFiles() {
        isLoading = true
        error = null
        try {
            val tree = api.getTree(owner, repo, branch, 1)
            files = tree.tree
                .filter { it.type == "blob" && it.path.endsWith(".kt") }
                .map { FileItem(it.path, it.sha) }
        } catch (e: Exception) {
            error = e.message ?: "Error loading files"
        } finally {
            isLoading = false
        }
    }

    suspend fun loadFileContent(file: FileItem) {
        isLoading = true
        error = null
        currentFileName = file.path
        try {
            val blob = api.getBlob(owner, repo, file.sha)
            val decoded = if (blob.encoding == "base64") {
                String(Base64.getDecoder().decode(blob.content))
            } else {
                blob.content
            }
            currentFileContent = decoded
        } catch (e: Exception) {
            error = e.message ?: "Error loading file content"
            currentFileContent = ""
        } finally {
            isLoading = false
        }
    }

    fun toggleFileSelection(file: FileItem) {
        files = files.map {
            if (it.path == file.path) it.copy(isSelected = !it.isSelected)
            else it
        }
    }

    fun addComment() {
        if (currentComment.isNotBlank()) {
            comments.add(CodeComment(currentFileName, currentComment))
            currentComment = ""
        }
    }

    fun getSelectedFiles() = files.filter { it.isSelected }
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
    val scope = rememberCoroutineScope()

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
                value = viewModel.owner,
                onValueChange = { viewModel.owner = it },
                label = { Text("Owner") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = viewModel.repo,
                onValueChange = { viewModel.repo = it },
                label = { Text("Repositorio") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = viewModel.branch,
                onValueChange = { viewModel.branch = it },
                label = { Text("Branch") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Load button
            Button(
                onClick = {
                    scope.launch {
                        viewModel.loadFiles()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !viewModel.isLoading
            ) {
                Text(if (viewModel.isLoading) "Cargando..." else "Cargar Archivos")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error message
            viewModel.error?.let { error ->
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
                items(viewModel.files) { file ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleFileSelection(file) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = file.isSelected,
                            onCheckedChange = { viewModel.toggleFileSelection(file) }
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
    val selectedFiles = viewModel.getSelectedFiles()
    var currentFileIndex by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(currentFileIndex) {
        if (selectedFiles.isNotEmpty() && currentFileIndex < selectedFiles.size) {
            viewModel.loadFileContent(selectedFiles[currentFileIndex])
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
                    text = viewModel.currentFileName,
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
                    if (viewModel.isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        SyntaxHighlightedCode(viewModel.currentFileContent)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Comment field
                OutlinedTextField(
                    value = viewModel.currentComment,
                    onValueChange = { viewModel.currentComment = it },
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
                            viewModel.addComment()
                        },
                        enabled = viewModel.currentComment.isNotBlank()
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
            if (viewModel.comments.isEmpty()) {
                Text(
                    text = "No hay comentarios guardados",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(viewModel.comments) { comment ->
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
