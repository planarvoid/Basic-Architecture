package com.example.architectureexample.mvp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.architectureexample.ItemAdapter
import com.example.architectureexample.MainDomainMockImpl
import com.example.architectureexample.R
import com.example.architectureexample.UIItem
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.*

class MVPActivity : AppCompatActivity(),
    MVPView {
    private val presenter: MVPPresenter =
        Presenter(MainDomainMockImpl())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        item_list.adapter = ItemAdapter(
            onClick = {
                presenter.onItemClick(it)
            },
            onLike = { id, isLiked ->
                presenter.likeItem(id, isLiked)
            },
            onLoadNextPage = {
                presenter.loadNextPage()
            })

        fab.setOnClickListener {
            presenter.onNewItem(
                item_url.text.toString(),
                item_title.text.toString(),
                item_title.text.toString()
            )
        }

        presenter.bind(this)
    }

    override fun onDestroy() {
        presenter.unbind()
        super.onDestroy()
    }

    override fun showData(results: List<UIItem>) {
        item_list.visibility = View.VISIBLE
        empty_view.visibility = View.GONE
        progress_view.visibility = View.GONE
        error_view.visibility = View.GONE
        (item_list.adapter as ItemAdapter).updateData(results)
    }

    override fun showProgress(progress: Boolean) {
        item_list.visibility = View.GONE
        empty_view.visibility = View.GONE
        progress_view.visibility = View.VISIBLE
        error_view.visibility = View.GONE
    }

    override fun showEmptyScreen() {
        item_list.visibility = View.GONE
        empty_view.visibility = View.VISIBLE
        progress_view.visibility = View.GONE
        error_view.visibility = View.GONE
    }

    override fun showFailure(message: String) {
        item_list.visibility = View.GONE
        empty_view.visibility = View.GONE
        progress_view.visibility = View.GONE
        error_view.visibility = View.VISIBLE
        error_view.text = message
    }

    override fun setLikeStatus(id: UUID, isLiked: Boolean) {
        (item_list.adapter as ItemAdapter).likeItem(id, isLiked)
    }

    override fun navigateTo(url: String) {
        Snackbar.make(main_container, "Navigating to url: $url", Snackbar.LENGTH_LONG).show()
    }

    override fun showItemCreated(title: String) {
        Snackbar.make(main_container, "Item created: $title", Snackbar.LENGTH_LONG).show()
    }

    override fun setItemCreationEnabled(enabled: Boolean) {
        item_url.isEnabled = enabled
        item_title.isEnabled = enabled
        item_title.isEnabled = enabled
        fab.isEnabled = enabled
    }

    override fun showToastMessage(message: String) {
        Snackbar.make(main_container, message, Snackbar.LENGTH_LONG).show()
    }
}

interface MVPView {
    fun showData(results: List<UIItem>)
    fun showProgress(progress: Boolean)
    fun showEmptyScreen()
    fun showFailure(message: String)
    fun setLikeStatus(id: UUID, isLiked: Boolean)
    fun navigateTo(url: String)
    fun showItemCreated(title: String)
    fun setItemCreationEnabled(enabled: Boolean)
    fun showToastMessage(message: String)
}
