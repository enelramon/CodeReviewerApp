package com.sagrd.codereviewerapp.di

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.sagrd.codereviewerapp.BuildConfig
import com.sagrd.codereviewerapp.data.FirestoreRepository
import com.sagrd.codereviewerapp.data.GeminiRepository
import com.sagrd.codereviewerapp.data.GitHubApi
import com.sagrd.codereviewerapp.data.GitHubRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseFirestore() = Firebase.firestore

    @Provides
    @Singleton
    fun provideFirebaseAuth() = Firebase.auth

    @Provides
    @Singleton
    fun provideFirestoreRepository(db: FirebaseFirestore, auth: FirebaseAuth) = FirestoreRepository(db, auth)

    @Provides
    @Singleton
    fun provideGitHubApi(): GitHubApi {
        val json = Json { ignoreUnknownKeys = true }
        return Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .client(
                OkHttpClient.Builder()
                    .apply {
                        if (BuildConfig.DEBUG) {
                            addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                        }
                    }
                    .build()
            )
            .build()
            .create(GitHubApi::class.java)
    }

    @Provides
    @Singleton
    fun provideGitHubRepository(api: GitHubApi) = GitHubRepository(api)

    @Provides
    @Singleton
    fun provideGenerativeModel(): GenerativeModel? {
        val apiKey = BuildConfig.GEMINI_API_KEY
        return if (apiKey.isNotBlank()) {
            GenerativeModel(
                modelName = "gemini-2.0-flash",
                apiKey = apiKey,
                generationConfig = generationConfig {
                    temperature = 0.7f
                    topK = 40
                    topP = 0.95f
                    maxOutputTokens = 1024
                }
            )
        } else null
    }

    @Provides
    @Singleton
    fun provideGeminiRepository(generativeModel: GenerativeModel?) = GeminiRepository(generativeModel)
}
