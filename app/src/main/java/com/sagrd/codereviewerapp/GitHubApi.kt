package com.sagrd.codereviewerapp

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// Retrofit API Interface
interface GitHubApi {
    @GET("repos/{owner}/{repo}/git/trees/{sha}")
    suspend fun getTree(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("sha") sha: String,
        @Query("recursive") recursive: Int = 1
    ): GitHubTree

    @GET("repos/{owner}/{repo}/git/blobs/{sha}")
    suspend fun getBlob(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("sha") sha: String
    ): GitHubBlob

    @GET("repos/{owner}/{repo}/branches")
    suspend fun getBranches(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): List<GitHubBranch>
}