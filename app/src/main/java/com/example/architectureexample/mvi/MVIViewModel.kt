package com.example.architectureexample.mvi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.example.architectureexample.ReactiveDomain
import com.example.architectureexample.UIItem
import com.example.architectureexample.mapToUiModel
import com.example.architectureexample.mvi.MVIActivity.UIAction
import com.example.architectureexample.mvi.MVIViewModel.SingleState.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.scan
import kotlin.coroutines.CoroutineContext

@FlowPreview
@ExperimentalCoroutinesApi
class MVIViewModel(private val domain: ReactiveDomain) : CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Default

    private val channel = BroadcastChannel<PartialState>(1)
    private var dataLoadingJob: Job? = null
    val uiState = channel.asFlow()
        .scan(SingleState()) { currentState, partialState ->
            currentState.update(partialState)
        }
        .asLiveData(coroutineContext)

    // Needs a mechanism to only show the message once
    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> = _toastMessage
    private val _navigateTo = MutableLiveData<String>()
    val navigateTo: LiveData<String> = _navigateTo

    fun start(actionChannel: ReceiveChannel<UIAction>) {
        launch {
            if (uiState.value == null) {
                onAction(UIAction.LoadData())
            }
            for (uiAction in actionChannel) {
                onAction(uiAction)
            }
        }
    }

    private suspend fun onAction(action: UIAction) {
        when (action) {
            is UIAction.LoadData -> {
                reattachData(action.filter)
                channel.send(
                    PartialState.Loading(
                        isLoadingNextPage = false
                    )
                )
                when (val result = domain.fetchData(false)) {
                    ReactiveDomain.Result.Success -> channel.send(
                        PartialState.Success
                    )
                    is ReactiveDomain.Result.Failure -> channel.send(
                        PartialState.Error(
                            result.message
                        )
                    )
                }
            }
            UIAction.RefreshData -> {
                channel.send(PartialState.Refreshing)
                when (val result = domain.fetchData(true)) {
                    ReactiveDomain.Result.Success -> channel.send(
                        PartialState.Success
                    )
                    is ReactiveDomain.Result.Failure -> channel.send(
                        PartialState.Error(
                            result.message
                        )
                    )
                }
            }
            UIAction.NextPage -> {
                channel.send(
                    PartialState.Loading(
                        isLoadingNextPage = true
                    )
                )
                when (val result = domain.fetchMore()) {
                    ReactiveDomain.Result.Success -> channel.send(
                        PartialState.Success
                    )
                    is ReactiveDomain.Result.Failure -> channel.send(
                        PartialState.Error(
                            result.message
                        )
                    )
                }
            }
            is UIAction.NewItem -> {
                channel.send(
                    PartialState.CreatingNewItem(
                        true
                    )
                )
                when (val result = domain.createItem(action.url, action.title, action.img)) {
                    ReactiveDomain.Result.Success -> channel.send(
                        PartialState.Success
                    )
                    is ReactiveDomain.Result.Failure -> channel.send(
                        PartialState.Error(
                            result.message
                        )
                    )
                }
                channel.send(
                    PartialState.CreatingNewItem(
                        false
                    )
                )
            }
            is UIAction.ItemClick -> _navigateTo.postValue(action.url)
            is UIAction.ItemLike -> domain.likeItem(action.id, true)
        }
    }

    private suspend fun reattachData(filter: String?) {
        coroutineScope {
            dataLoadingJob?.cancel()
            dataLoadingJob = launch {
                domain.data(filter).collect {
                    channel.send(PartialState.Data(it.mapToUiModel()))
                }
            }
        }
    }

    data class SingleState(
        val data: List<UIItem>? = null,
        val errorMessage: String? = null,
        val isItemCreationEnabled: Boolean = true,
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false
    ) {
        fun isInProgress() = isLoading && !hasData()
        fun isLoadingNextPage() = isLoading && hasData()
        fun isFullScreenFailure() = errorMessage != null && !hasData()
        fun isEmpty() = !hasData()
        fun hasData() = data?.isNotEmpty() == true

        fun update(partialState: PartialState): SingleState {
            return when (partialState) {
                is PartialState.Data -> this.copy(data = partialState.data)
                is PartialState.Error -> this.copy(
                    errorMessage = partialState.message,
                    isLoading = false,
                    isRefreshing = false
                )
                is PartialState.Loading -> this.copy(
                    errorMessage = null,
                    isLoading = true,
                    isRefreshing = false
                )
                is PartialState.Success -> this.copy(
                    errorMessage = null,
                    isLoading = false,
                    isRefreshing = false
                )
                PartialState.Refreshing -> this.copy(
                    errorMessage = null,
                    isLoading = false,
                    isRefreshing = true
                )
                is PartialState.CreatingNewItem -> this.copy(
                    isItemCreationEnabled = partialState.creationEnabled
                )
            }
        }

        sealed class PartialState {
            data class Data(val data: List<UIItem>) : PartialState()
            data class Error(val message: String) : PartialState()
            data class Loading(val isLoadingNextPage: Boolean) : PartialState()
            object Refreshing : PartialState()
            object Success : PartialState()
            data class CreatingNewItem(val creationEnabled: Boolean) : PartialState()
        }
    }
}
