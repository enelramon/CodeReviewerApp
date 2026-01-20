package com.sagrd.codereviewerapp

data class FileItem(
    val path: String,
    val sha: String,
    var isSelected: Boolean = false
)