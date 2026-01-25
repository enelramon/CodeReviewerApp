package com.sagrd.codereviewerapp.data

import kotlinx.serialization.Serializable

@Serializable
data class GitHubBlob(
    val content: String,
    val encoding: String
)