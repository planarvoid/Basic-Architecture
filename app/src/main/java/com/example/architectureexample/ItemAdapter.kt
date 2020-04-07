package com.example.architectureexample

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.architectureexample.mvi.MVIActivity
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import java.util.*

class ItemAdapter(
    val onClick: ((url: String) -> Unit)? = null,
    val onLike: ((id: UUID, isLiked: Boolean) -> Unit)? = null,
    val onLoadNextPage: (() -> Unit)? = null,
    val actionChannel: BroadcastChannel<MVIActivity.UIAction>? = null
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {
    private val listItems = mutableListOf<UIItem>()

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view)

    fun updateData(items: List<UIItem>) {
        listItems.clear()
        listItems.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int = listItems.size

    fun likeItem(id: UUID, isLiked: Boolean) {
        val position = listItems.indexOfFirst {
            if (it is UIItem.Item) {
                it.liked = isLiked
                it.id == id
            } else {
                false
            }
        }
        notifyItemChanged(position)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        TODO("Not yet implemented")
    }
}