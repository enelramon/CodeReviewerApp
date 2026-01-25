package com.sagrd.codereviewerapp.ui.code_review

import com.sagrd.codereviewerapp.data.CodeComment
import com.sagrd.codereviewerapp.data.FileItem
import com.sagrd.codereviewerapp.data.ProjectType
import com.sagrd.codereviewerapp.data.ReviewHistoryItem

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
    val editingReviewId: String? = null,
    val lastDeletedItem: ReviewHistoryItem? = null
) {
    val selectedFiles: List<FileItem>
        get() = files.filter { it.isSelected }
}