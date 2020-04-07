package com.example.architectureexample

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*

interface ReactiveDomain {
    fun data(filter: String? = null): Flow<List<MainDomain.DomainModel>>
    suspend fun fetchData(forced: Boolean): Result
    suspend fun fetchMore(): Result
    suspend fun createItem(url: String, title: String, img: String): Result
    suspend fun likeItem(id: UUID, isLiked: Boolean)

    sealed class Result {
        object Success : Result()
        data class Failure(val message: String) : Result()
    }
}
