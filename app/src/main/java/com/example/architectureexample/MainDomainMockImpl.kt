package com.example.architectureexample

import java.util.*

class MainDomainMockImpl : MainDomain {
    override suspend fun fetchData(
        forced: Boolean,
        offset: Int
    ): MainDomain.Result<List<MainDomain.DomainModel>> {
        TODO("Not yet implemented")
    }

    override suspend fun loadLocalData(): List<MainDomain.DomainModel> {
        TODO("Not yet implemented")
    }

    override suspend fun createItem(
        url: String,
        title: String,
        img: String
    ): MainDomain.Result<UUID> {
        TODO("Not yet implemented")
    }

    override suspend fun likeItem(id: UUID, isLiked: Boolean) {
        TODO("Not yet implemented")
    }
}
