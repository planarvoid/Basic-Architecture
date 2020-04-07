package com.example.architectureexample.mvvm

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.architectureexample.ItemAdapter
import com.example.architectureexample.MainDomainMockImpl
import com.example.architectureexample.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MVVMActivity : AppCompatActivity() {
    private val viewModel: MVViewModel =
        MVViewModel(MainDomainMockImpl())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        item_list.adapter = ItemAdapter(
            onClick = {
                viewModel.onItemClick(it)
            },
            onLike = { id, isLiked ->
                viewModel.likeItem(id, isLiked)
            },
            onLoadNextPage = {
                viewModel.loadNextPage()
            })

        fab.setOnClickListener {
            viewModel.onNewItem(
                item_url.text.toString(),
                item_title.text.toString(),
                item_title.text.toString()
            )
        }

        setupObservers()

        viewModel.start()
    }

    private fun setupObservers() {
        viewModel.currentData.observe(this, Observer {
            (item_list.adapter as ItemAdapter).updateData(it)
        })

        viewModel.uiState.observe(this, Observer { uiState ->
            item_list.visibility = View.GONE
            empty_view.visibility = View.GONE
            progress_view.visibility = View.GONE
            error_view.visibility = View.GONE
            when (uiState) {
                MVViewModel.UIState.Success -> item_list.visibility = View.VISIBLE
                MVViewModel.UIState.Empty -> empty_view.visibility = View.VISIBLE
                is MVViewModel.UIState.Failure -> {
                    error_view.visibility = View.VISIBLE
                    error_view.text = uiState.message
                }
                MVViewModel.UIState.Progress -> progress_view.visibility = View.VISIBLE
            }
        })

        viewModel.toastMessage.observe(this, Observer {
            Snackbar.make(main_container, it, Snackbar.LENGTH_LONG).show()
        })

        viewModel.isItemCreationEnabled.observe(this, Observer { enabled ->
            item_url.isEnabled = enabled
            item_title.isEnabled = enabled
            item_title.isEnabled = enabled
            fab.isEnabled = enabled
        })
        viewModel.navigateTo.observe(this, Observer {
            navigateTo(it)
        })
    }

    private fun navigateTo(url: String) {
        Snackbar.make(main_container, "Navigating to url: $url", Snackbar.LENGTH_LONG).show()
    }
}
