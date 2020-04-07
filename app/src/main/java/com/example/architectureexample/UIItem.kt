package com.example.architectureexample

import java.util.*

sealed class UIItem {
    data class Item(val id: UUID, val title: String, var liked: Boolean): UIItem()
    object Loading: UIItem()
    object LoadingNextFailed: UIItem()
}

fun List<MainDomain.DomainModel>.mapToUiModel(): List<UIItem> {
    return this.map {
        UIItem.Item(
            it.id,
            it.title,
            it.isLikedByCurrentUser
        )
    }
}