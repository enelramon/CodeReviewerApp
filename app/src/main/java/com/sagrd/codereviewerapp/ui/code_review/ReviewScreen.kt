package com.sagrd.codereviewerapp.ui.code_review


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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sagrd.codereviewerapp.data.FileItem
import com.sagrd.codereviewerapp.data.ProjectType
import com.sagrd.codereviewerapp.ui.theme.CodeReviewerAppTheme
import dev.snipme.highlights.Highlights
import dev.snipme.highlights.model.SyntaxThemes
import dev.snipme.kodeview.view.CodeTextView

@Composable
fun ReviewScreen(
    viewModel: CodeReviewViewModel,
    onNavigateToSummary: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    ReviewContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateToSummary = onNavigateToSummary,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewContent(
    uiState: CodeReviewUiState,
    onEvent: (CodeReviewUiEvent) -> Unit,
    onNavigateToSummary: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val selectedFiles = uiState.selectedFiles
    var currentFileIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(currentFileIndex) {
        if (selectedFiles.isNotEmpty() && currentFileIndex < selectedFiles.size) {
            onEvent(CodeReviewUiEvent.LoadFileContent(selectedFiles[currentFileIndex]))
            // Load existing comment for current file
            onEvent(CodeReviewUiEvent.LoadCommentForFile(selectedFiles[currentFileIndex].path))
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
                    onValueChange = { onEvent(CodeReviewUiEvent.UpdateComment(it)) },
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
                                onEvent(CodeReviewUiEvent.SuggestComment)
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
                                onEvent(CodeReviewUiEvent.AddComment)

                                if (currentFileIndex < selectedFiles.size - 1) {
                                    currentFileIndex++
                                } else {
                                    onNavigateToSummary()
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

@Preview(showBackground = true)
@Composable
fun ReviewScreenPreview() {
    val sampleFiles = listOf(
        FileItem("MainActivity.kt", "sha1", true),
        FileItem("ReviewScreen.kt", "sha2", true)
    )
    val uiState = CodeReviewUiState(
        files = sampleFiles,
        currentFileName = "MainActivity.kt",
        currentFileContent = """
            fun main() {
                println("Hello, Code Reviewer!")
            }
        """.trimIndent(),
        currentComment = "Este código se ve bien."
    )

    CodeReviewerAppTheme {
        ReviewContent(
            uiState = uiState,
            onEvent = {},
            onNavigateToSummary = {},
            onNavigateBack = {}
        )
    }
}
