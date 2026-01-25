package com.sagrd.codereviewerapp.data

import kotlinx.serialization.Serializable

// Data Models
@Serializable
data class GitHubTree(
    val tree: List<GitHubTreeItem>
)