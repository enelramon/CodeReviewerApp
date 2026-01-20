package com.sagrd.codereviewerapp

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