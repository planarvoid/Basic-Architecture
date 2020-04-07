package com.example.architectureexample

import java.util.*

interface MainDomain {
    suspend fun fetchData(forced: Boolean, offset: Int = 0): Result<List<DomainModel>>
    suspend fun loadLocalData(): List<DomainModel>
    suspend fun createItem(url: String, title: String, img: String): Result<UUID>
    suspend fun likeItem(id: UUID, isLiked: Boolean)

    data class DomainModel(
        val id: UUID,
        val url: String,
        val title: String,
        val img: String,
        val isLikedByCurrentUser: Boolean,
        val likes: Int
    )

    sealed class Result<T> {
        data class Success<T>(val data: T) : Result<T>()
        data class Failure<T>(val message: String) : Result<T>()
    }
}
