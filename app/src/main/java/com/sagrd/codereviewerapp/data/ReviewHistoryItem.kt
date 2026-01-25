package com.sagrd.codereviewerapp.data

import com.google.firebase.Timestamp
import com.sagrd.codereviewerapp.data.CodeComment
import java.util.Date
import kotlin.collections.get

data class ReviewHistoryItem(
    val id: String = "",
    val owner: String = "",
    val repo: String = "",
    val branch: String = "",
    val date: Date = Date(),
    val comments: List<CodeComment> = emptyList(),
    val aiSummary: String = "",
    val projectType: String = ProjectType.KOTLIN.name
) {
    // Convert to Map for Firestore
    fun toMap(): Map<String, Any> = mapOf(
        "owner" to owner,
        "repo" to repo,
        "branch" to branch,
        "date" to date,
        "comments" to comments.map { mapOf("fileName" to it.fileName, "comment" to it.comment) },
        "aiSummary" to aiSummary,
        "projectType" to projectType
    )

    companion object {
        // Create from Firestore document
        fun fromMap(id: String, map: Map<String, Any>): ReviewHistoryItem {
            val commentsList = (map["comments"] as? List<*>)?.mapNotNull { item ->
                (item as? Map<*, *>)?.let { commentMap ->
                    CodeComment(
                        fileName = commentMap["fileName"] as? String ?: "",
                        comment = commentMap["comment"] as? String ?: ""
                    )
                }
            } ?: emptyList()

            return ReviewHistoryItem(
                id = id,
                owner = map["owner"] as? String ?: "",
                repo = map["repo"] as? String ?: "",
                branch = map["branch"] as? String ?: "",
                date = (map["date"] as? Timestamp)?.toDate() ?: Date(),
                comments = commentsList,
                aiSummary = map["aiSummary"] as? String ?: "",
                projectType = map["projectType"] as? String ?: ProjectType.KOTLIN.name
            )
        }
    }
}