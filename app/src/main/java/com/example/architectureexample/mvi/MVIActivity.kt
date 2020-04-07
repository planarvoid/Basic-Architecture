package com.example.architectureexample.mvi

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.architectureexample.ItemAdapter
import com.example.architectureexample.R
import com.example.architectureexample.ReactiveDomainImpl
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import java.util.*

@ExperimentalCoroutinesApi
@FlowPreview
class MVIActivity : AppCompatActivity() {
    private val viewModel: MVIViewModel =
        MVIViewModel(ReactiveDomainImpl())
    private val actionChannel = BroadcastChannel<UIAction>(5)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        item_list.adapter = ItemAdapter(actionChannel = actionChannel)

        fab.setOnClickListener {
            actionChannel.offer(
                UIAction.NewItem(
                    item_url.text.toString(),
                    item_title.text.toString(),
                    item_img.text.toString()
                )
            )
        }

        viewModel.uiState.observe(this, Observer { state ->
            item_list.visibility = if (state.hasData()) View.VISIBLE else View.GONE
            empty_view.visibility = if (state.isEmpty()) View.VISIBLE else View.GONE
            progress_view.visibility = if (state.isInProgress()) View.VISIBLE else View.GONE
            error_view.visibility = if (state.isFullScreenFailure()) View.VISIBLE else View.GONE
            state.data?.let {
                (item_list.adapter as ItemAdapter).updateData(it)
            }
            setItemCreationEnabled(state.isItemCreationEnabled)
        })

        viewModel.toastMessage.observe(this, Observer {
            Snackbar.make(main_container, it, Snackbar.LENGTH_LONG).show()
        })

        viewModel.navigateTo.observe(this, Observer {
            navigateTo(it)
        })

        viewModel.start(actionChannel.openSubscription())
    }

    private fun setItemCreationEnabled(itemCreationEnabled: Boolean) {
        item_url.isEnabled = itemCreationEnabled
        item_title.isEnabled = itemCreationEnabled
        item_title.isEnabled = itemCreationEnabled
        fab.isEnabled = itemCreationEnabled
    }

    fun navigateTo(url: String) {
        Snackbar.make(main_container, "Navigating to url: $url", Snackbar.LENGTH_LONG).show()
    }

    sealed class UIAction {
        data class LoadData(val filter: String? = null) : UIAction()
        object RefreshData : UIAction()
        object NextPage : UIAction()
        data class NewItem(val url: String, val title: String, val img: String) : UIAction()
        data class ItemClick(val url: String) : UIAction()
        data class ItemLike(val id: UUID, val isLiked: Boolean) : UIAction()
    }
}
