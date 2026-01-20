package com.sagrd.codereviewerapp

import kotlinx.serialization.Serializable

@Serializable
data class GitHubCommit(
    val sha: String
)