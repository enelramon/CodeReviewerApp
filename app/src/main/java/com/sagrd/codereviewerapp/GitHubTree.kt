package com.sagrd.codereviewerapp

import kotlinx.serialization.Serializable

// Data Models
@Serializable
data class GitHubTree(
    val tree: List<GitHubTreeItem>
)