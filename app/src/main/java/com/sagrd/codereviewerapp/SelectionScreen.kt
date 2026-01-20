package com.sagrd.codereviewerapp


import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sagrd.codereviewerapp.ui.theme.CodeReviewerAppTheme


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