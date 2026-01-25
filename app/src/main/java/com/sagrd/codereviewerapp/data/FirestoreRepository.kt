package com.sagrd.codereviewerapp.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sagrd.codereviewerapp.data.ReviewHistoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirestoreRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    // AppId can be configured per build variant or from BuildConfig if needed
    // For now using a constant value. For production, consider making this dynamic.
    private val appId = "code-reviewer-app"

    private suspend fun ensureAuth() {
        if (auth.currentUser == null) {
            auth.signInAnonymously().await()
        }
    }

    suspend fun saveReviewHistory(historyItem: ReviewHistoryItem): Result<String> {
        return try {
            ensureAuth()
            val docRef = firestore
                .collection("artifacts").document(appId)
                .collection("public").document("data")
                .collection("reviews")
                .document()

            withContext(Dispatchers.IO) {
                docRef.set(historyItem.toMap()).await()
            }
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteReviewHistory(id: String): Result<Unit> {
        return try {
            ensureAuth()
            withContext(Dispatchers.IO) {
                firestore
                    .collection("artifacts").document(appId)
                    .collection("public").document("data")
                    .collection("reviews")
                    .document(id)
                    .delete()
                    .await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateReviewHistory(id: String, historyItem: ReviewHistoryItem): Result<Unit> {
        return try {
            ensureAuth()
            withContext(Dispatchers.IO) {
                firestore
                    .collection("artifacts").document(appId)
                    .collection("public").document("data")
                    .collection("reviews")
                    .document(id)
                    .set(historyItem.toMap())
                    .await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loadReviewHistory(): Result<List<ReviewHistoryItem>> {
        return try {
            ensureAuth()
            val snapshot = withContext(Dispatchers.IO) {
                firestore
                    .collection("artifacts").document(appId)
                    .collection("public").document("data")
                    .collection("reviews")
                    .get()
                    .await()
            }

            // Sort in memory by date descending (no orderBy in Firestore query as per requirements)
            // Note: For large datasets, consider implementing pagination or limiting query results
            val historyList = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { data ->
                    ReviewHistoryItem.Companion.fromMap(doc.id, data)
                }
            }.sortedByDescending { it.date }

            Result.success(historyList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
