package com.sagrd.codereviewerapp

import kotlinx.serialization.Serializable

@Serializable
data class GitHubBlob(
    val content: String,
    val encoding: String
)