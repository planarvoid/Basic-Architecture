package com.example.architectureexample

import kotlinx.coroutines.flow.Flow
import java.util.*

class ReactiveDomainImpl : ReactiveDomain {
    override fun data(): Flow<List<MainDomain.DomainModel>> {
        TODO("Not yet implemented")
    }

    override suspend fun fetchData(forced: Boolean): ReactiveDomain.Result {
        TODO("Not yet implemented")
    }

    override suspend fun fetchMore(): ReactiveDomain.Result {
        TODO("Not yet implemented")
    }

    override suspend fun createItem(
        url: String,
        title: String,
        img: String
    ): ReactiveDomain.Result {
        TODO("Not yet implemented")
    }

    override suspend fun likeItem(id: UUID, isLiked: Boolean) {
        TODO("Not yet implemented")
    }
}
