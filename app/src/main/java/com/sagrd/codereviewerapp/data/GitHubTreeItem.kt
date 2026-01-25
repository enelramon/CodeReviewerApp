package com.sagrd.codereviewerapp.data

import kotlinx.serialization.Serializable

@Serializable
data class GitHubTreeItem(
    val path: String,
    val type: String,
    val sha: String
)