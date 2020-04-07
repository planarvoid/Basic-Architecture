package com.example.architectureexample.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.architectureexample.MainDomain
import com.example.architectureexample.UIItem
import com.example.architectureexample.mapToUiModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import kotlin.coroutines.CoroutineContext

class MVViewModel(private val domain: MainDomain) : CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Default

    private val _currentData = MutableLiveData<List<UIItem>>()
    val currentData: LiveData<List<UIItem>> = _currentData
    private val _uiState = MutableLiveData<UIState>()
    val uiState: LiveData<UIState> = _uiState
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private val _isItemCreationEnabled = MutableLiveData<Boolean>()
    val isItemCreationEnabled: LiveData<Boolean> = _isItemCreationEnabled

    // Needs a mechanism to only show the message once
    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> = _toastMessage
    private val _navigateTo = MutableLiveData<String>()
    val navigateTo: LiveData<String> = _navigateTo

    fun start() {
        loadItems(false)
    }

    fun loadItems(forced: Boolean) {
        _isLoading.postValue(true)
        launch {
            val data = domain.fetchData(forced)
            _isLoading.postValue(false)
            when (data) {
                is MainDomain.Result.Success -> {
                    val results = data.data.mapToUiModel()
                    _currentData.postValue(results)
                    _uiState.postValue(UIState.Success)
                }
                is MainDomain.Result.Failure -> {
                    if (currentData.value?.isNotEmpty() == true) {
                        _toastMessage.postValue(data.message)
                    } else {
                        _uiState.postValue(
                            UIState.Failure(
                                data.message
                            )
                        )
                    }
                }
            }
        }
    }

    fun likeItem(id: UUID, isLiked: Boolean) {
        launch {
            // Is the like status local or remote?
            domain.likeItem(id, isLiked)
            _currentData.postValue(domain.loadLocalData().mapToUiModel())
        }
    }

    fun onNewItem(url: String, title: String, img: String) {
        _isItemCreationEnabled.value = false
        launch {
            val result = domain.createItem(url, title, img)
            when (result) {
                is MainDomain.Result.Success -> _toastMessage.postValue("Item created: $title")
                is MainDomain.Result.Failure -> _toastMessage.postValue("Item creation failed: ${result.message}")
            }
            _isItemCreationEnabled.value = true
        }
    }

    fun onItemClick(url: String) {
        _navigateTo.value = url
    }

    fun loadNextPage() {
        TODO("Not yet implemented")
    }

    sealed class UIState {
        object Success : UIState()
        object Empty : UIState()
        data class Failure(val message: String) : UIState()
        object Progress : UIState()
    }
}
