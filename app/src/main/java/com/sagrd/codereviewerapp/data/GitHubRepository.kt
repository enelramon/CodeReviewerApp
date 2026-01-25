package com.sagrd.codereviewerapp.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Base64

class GitHubRepository(private val api: GitHubApi) {

    suspend fun getBranches(owner: String, repo: String): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val branches = api.getBranches(owner, repo)
            Result.success(branches.map { it.name })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFiles(
        owner: String,
        repo: String,
        branch: String,
        projectType: ProjectType
    ): Result<List<FileItem>> = withContext(Dispatchers.IO) {
        try {
            val tree = api.getTree(owner, repo, branch, 1)
            val filesList = tree.tree
                .filter {
                    it.type == "blob" && when (projectType) {
                        ProjectType.KOTLIN -> it.path.endsWith(".kt")
                        ProjectType.BLAZOR -> it.path.endsWith(".razor") ||
                                it.path.endsWith(".cs") ||
                                it.path.endsWith(".cshtml")
                    }
                }
                .map { FileItem(it.path, it.sha) }
            Result.success(filesList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFileContent(owner: String, repo: String, sha: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val blob = api.getBlob(owner, repo, sha)
            val decoded = if (blob.encoding == "base64") {
                String(Base64.getDecoder().decode(blob.content.replace("\\s".toRegex(), "")))
            } else {
                blob.content
            }
            Result.success(decoded)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
