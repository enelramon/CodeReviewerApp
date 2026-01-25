package com.sagrd.codereviewerapp.ui.code_review

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sagrd.codereviewerapp.data.CodeComment
import com.sagrd.codereviewerapp.ui.theme.CodeReviewerAppTheme

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
            putExtra(
                Intent.EXTRA_SUBJECT,
                "Revisión de Código: ${uiState.comments.size} comentarios"
            )
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir comentarios"))
    }

    SummaryContent(
        uiState = uiState,
        onShareComments = { shareComments() },
        onGenerateAISummary = { viewModel.onEvent(CodeReviewUiEvent.GenerateAISummary) },
        onSaveReview = { viewModel.onEvent(CodeReviewUiEvent.SaveReviewToHistory) },
        onNewReview = {
            viewModel.onEvent(CodeReviewUiEvent.ResetState)
            onNavigateToSelection()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryContent(
    uiState: CodeReviewUiState,
    onShareComments: () -> Unit,
    onGenerateAISummary: () -> Unit,
    onSaveReview: () -> Unit,
    onNewReview: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resumen de Comentarios") },
                actions = {
                    if (uiState.comments.isNotEmpty()) {
                        IconButton(onClick = onShareComments) {
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
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    // Generate AI Summary button inside scrollable list
                    item {
                        Button(
                            onClick = onGenerateAISummary,
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
                    }

                    // AI Summary Card inside scrollable list
                    if (uiState.aiSummary.isNotBlank()) {
                        item {
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
                    }

                    // List of comments
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

            // Action buttons remain fixed at the bottom
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onNewReview,
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Cancelar",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancelar")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Button(
                    onClick = onSaveReview,
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
                            contentDescription = "Guardar",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SummaryScreenPreview() {
    CodeReviewerAppTheme {
        SummaryContent(
            uiState = CodeReviewUiState(
                owner = "owner",
                repo = "repo",
                branch = "main",
                comments = listOf(
                    CodeComment(
                        "File1.kt",
                        "Este es un comentario de prueba para el primer archivo."
                    ),
                    CodeComment(
                        "File2.kt",
                        "Este es otro comentario de prueba para el segundo archivo."
                    )
                ),
                aiSummary = "Este es un resumen generado por IA que destaca los puntos clave de la revisión de código."
            ),
            onShareComments = {},
            onGenerateAISummary = {},
            onSaveReview = {},
            onNewReview = {}
        )
    }
}

