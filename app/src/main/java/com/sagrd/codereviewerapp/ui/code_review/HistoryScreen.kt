package com.sagrd.codereviewerapp.ui.code_review

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sagrd.codereviewerapp.data.CodeComment
import com.sagrd.codereviewerapp.data.ProjectType
import com.sagrd.codereviewerapp.data.ReviewHistoryItem
import com.sagrd.codereviewerapp.ui.theme.CodeReviewerAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: CodeReviewViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToReview: () -> Unit,
    onNavigateToSummary: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onEvent(CodeReviewUiEvent.LoadHistory)
    }

    HistoryScreenBody(
        uiState = uiState,
        onEvent = { viewModel.onEvent(it) },
        onNavigateBack = onNavigateBack,
        onNavigateToReview = onNavigateToReview,
        onNavigateToSummary = onNavigateToSummary,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreenBody(
    uiState: CodeReviewUiState,
    onEvent: (CodeReviewUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToReview: () -> Unit,
    onNavigateToSummary: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading || isRefreshing,
            onRefresh = {
                scope.launch {
                    isRefreshing = true
                    onEvent(CodeReviewUiEvent.LoadHistory)
                    delay(1000) // Artificial delay to improve perceived feedback
                    isRefreshing = false
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.history.isEmpty() && !uiState.isLoading) {
                // Se usa LazyColumn para permitir el gesto de refresh cuando la lista está vacía
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyHistory()
                        }
                    }
                }
            } else if (uiState.history.isEmpty() && uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(uiState.history, key = { it.id }) { historyItem ->
                        val dismissState = rememberSwipeToDismissBoxState()
                        SwipeToDismissBox(
                            state = dismissState,
                            onDismiss = {
                                onEvent(CodeReviewUiEvent.DeleteHistoryItem(historyItem))
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Revisión eliminada",
                                        actionLabel = "undo",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        onEvent(CodeReviewUiEvent.UndoDeleteHistoryItem)
                                    }
                                }
                            },
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
                                ReviewItem(
                                    historyItem = historyItem,
                                    onEvent = onEvent,
                                    onNavigateToReview = onNavigateToReview,
                                    onNavigateToSummary = onNavigateToSummary
                                )
                            },
                            enableDismissFromStartToEnd = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyHistory() {
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
}

@Composable
private fun ReviewItem(
    historyItem: ReviewHistoryItem,
    onEvent: (CodeReviewUiEvent) -> Unit,
    onNavigateToReview: () -> Unit,
    onNavigateToSummary: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                onEvent(CodeReviewUiEvent.EditReview(historyItem))
                onNavigateToReview()
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "${historyItem.owner}/${historyItem.repo}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Branch: ${historyItem.branch}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        .format(historyItem.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${historyItem.comments.size} comentarios",
                    style = MaterialTheme.typography.bodyMedium
                )
                IconButton(
                    onClick = {
                        onEvent(CodeReviewUiEvent.EditReview(historyItem))
                        onNavigateToSummary()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "Ver Summary",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            if (historyItem.aiSummary.isNotBlank()) {
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

@Preview(showBackground = true)
@Composable
fun HistoryScreenPreview() {
    CodeReviewerAppTheme {
        HistoryScreenBody(
            uiState = CodeReviewUiState(
                history = listOf(
                    ReviewHistoryItem(
                        id = "1",
                        owner = "enelramon",
                        repo = "CodeReviewerApp",
                        branch = "master",
                        date = Date(),
                        comments = listOf(
                            CodeComment(
                                "MainActivity.kt",
                                "Good job"
                            )
                        ),
                        aiSummary = "Summary test",
                        projectType = ProjectType.KOTLIN.name
                    )
                )
            ),
            onEvent = {},
            onNavigateBack = {},
            onNavigateToReview = {},
            onNavigateToSummary = {}
        )
    }
}
