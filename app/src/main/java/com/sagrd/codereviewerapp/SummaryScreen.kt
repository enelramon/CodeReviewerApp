package com.sagrd.codereviewerapp

import android.content.Intent
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
import androidx.compose.ui.unit.dp

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