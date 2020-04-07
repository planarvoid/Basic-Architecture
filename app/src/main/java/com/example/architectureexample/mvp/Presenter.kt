package com.example.architectureexample.mvp

import com.example.architectureexample.MainDomain
import com.example.architectureexample.UIItem
import com.example.architectureexample.mapToUiModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import kotlin.coroutines.CoroutineContext

class Presenter(private val domain: MainDomain) : MVPPresenter, CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Default

    private var view: MVPView? = null
    private val currentData: MutableList<MainDomain.DomainModel> = mutableListOf()
    override fun bind(view: MVPView) {
        this.view = view
        loadItems(false)
    }

    override fun unbind() {
        this.view = null
        this.job.cancel()
    }

    override fun loadItems(forced: Boolean) {
        this.view?.showProgress(true)
        launch {
            val data = domain.fetchData(forced)
            when (data) {
                is MainDomain.Result.Success -> {
                    currentData.clear()
                    currentData.addAll(data.data)
                    val results = currentData.mapToUiModel()
                    view?.showData(results + listOf(UIItem.Loading))
                }
                is MainDomain.Result.Failure -> {
                    if (currentData.isEmpty()) {
                        view?.showFailure(data.message)
                    } else {
                        view?.showToastMessage(data.message)
                    }
                }
            }
        }
    }

    override fun likeItem(id: UUID, isLiked: Boolean) {
        launch {
            // Is the like status local or remote?
            domain.likeItem(id, isLiked)
            view?.showData(domain.loadLocalData().mapToUiModel())
        }
    }

    override fun onNewItem(url: String, title: String, img: String) {
        view?.setItemCreationEnabled(false)
        launch {
            val result = domain.createItem(url, title, img)
            when (result) {
                is MainDomain.Result.Success -> view?.showItemCreated(title)
                is MainDomain.Result.Failure -> view?.showToastMessage(result.message)
            }
            view?.setItemCreationEnabled(true)
        }
    }

    override fun onItemClick(url: String) {
        view?.navigateTo(url)
    }

    // Wrong approach
    override fun onItemLike(uuid: UUID, isLiked: Boolean) {
        currentData.forEach { item ->
            if (item.id == uuid) {
//                item.isLikedByCurrentUser = isLiked
            }
        }
        view?.setLikeStatus(uuid, isLiked)
    }

    override fun loadNextPage() {
        launch {
            val data = domain.fetchData(false, currentData.size)
            when (data) {
                is MainDomain.Result.Success -> {
                    currentData.addAll(data.data)
                    val results = currentData.mapToUiModel()
                    view?.showData(results + listOf(UIItem.Loading))
                }
                is MainDomain.Result.Failure -> {
                    view?.showToastMessage(data.message)
                    val results = currentData.mapToUiModel()
                    view?.showData(results + listOf(UIItem.LoadingNextFailed))
                }
            }
        }
    }
}

interface MVPPresenter {
    fun bind(view: MVPView)
    fun unbind()
    fun loadItems(forced: Boolean = false)
    fun likeItem(id: UUID, isLiked: Boolean)
    fun onNewItem(url: String, title: String, img: String)
    fun onItemClick(url: String)
    fun onItemLike(uuid: UUID, isLiked: Boolean)
    fun loadNextPage()
}
