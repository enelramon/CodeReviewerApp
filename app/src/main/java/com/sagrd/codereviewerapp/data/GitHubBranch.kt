package com.sagrd.codereviewerapp.data

import kotlinx.serialization.Serializable

@Serializable
data class GitHubBranch(
    val name: String,
    val commit: GitHubCommit
)