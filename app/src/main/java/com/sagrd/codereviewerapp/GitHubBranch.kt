package com.sagrd.codereviewerapp

import kotlinx.serialization.Serializable

@Serializable
data class GitHubBranch(
    val name: String,
    val commit: GitHubCommit
)