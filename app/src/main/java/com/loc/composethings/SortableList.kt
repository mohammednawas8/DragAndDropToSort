package com.loc.composethings

import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_DRAG
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

/**
 * In order to make [selectedItemBackgroundColor] work, the itemContent design should not have a background color.
 */
data class DraggingProperties(
    val alpha: Float = 0.85f,
    val enableScaleAnimation: Boolean = true,
    val scaleFactor: Float = 1.05f,
    val selectedItemBackgroundColor: Color? = null,
)

class DraggingState() {
    var enableDragging by mutableStateOf(true)

    private var _isThereAnItemBeingDragged by mutableStateOf(false)
    val isThereAnItemBeingDragged: Boolean
        get() = _isThereAnItemBeingDragged

    fun setItemBeingDragged(value: Boolean) {
        _isThereAnItemBeingDragged = value
    }
}

@Composable
fun rememberDraggingState(): DraggingState {
    val draggingState = remember {
        DraggingState()
    }
    return draggingState
}

private var firstCompletelyVisibleItemPosition = 0

@Composable
fun <T> SortableList(
    modifier: Modifier = Modifier,
    draggingState: DraggingState,
    draggingProperties: DraggingProperties = DraggingProperties(),
    items: List<T>,
    itemsVerticalSpacing: Int = 0,
    itemContent: @Composable (Int, T) -> Unit,
    onItemPlacementChange: (from: Int, to: Int) -> Unit,
    rvAdapter: SortableListAdapter<T> = remember {
        SortableListAdapter(
            itemContent = itemContent,
            onItemPlacementChange = onItemPlacementChange,
        )
    },
) {
    var rv by remember {
        mutableStateOf<RecyclerView?>(null)
    }
    val itemTouchHelper = remember {
        itemTouchHelper(
            adapter = rvAdapter,
            alpha = draggingProperties.alpha,
            enableScaleAnimation = draggingProperties.enableScaleAnimation,
            scaleFactor = draggingProperties.scaleFactor,
            isThereAnItemBeingDragged = {
                draggingState.setItemBeingDragged(it)
            },
            selectedItemBackgroundColor = draggingProperties.selectedItemBackgroundColor
        )
    }
    LaunchedEffect(items, rvAdapter, rv) {
        if (rv != null)
            rvAdapter.display(items)
    }
    LaunchedEffect(draggingState.enableDragging) {
        if (draggingState.enableDragging) {
            itemTouchHelper.attachToRecyclerView(rv)
        } else {
            itemTouchHelper.attachToRecyclerView(null)
        }
    }
    DisposableEffect(key1 = rv) {
        rv?.let {
            it.setOnScrollChangeListener { _, _, _, _, _ ->
                firstCompletelyVisibleItemPosition =
                    (it.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
            }
        }
        onDispose {
            rv?.clearOnScrollListeners()
        }
    }
    LaunchedEffect(key1 = rv) {
        rv?.scrollToPosition(firstCompletelyVisibleItemPosition)
    }
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .testTag("sortable list"),
        factory = { context ->
            RecyclerView(context).apply {
                adapter = rvAdapter
                layoutManager = LinearLayoutManager(context)
                if (draggingState.enableDragging) {
                    itemTouchHelper.attachToRecyclerView(this)
                }
                addItemDecoration(ItemsVerticalSpacing(itemsVerticalSpacing))
            }
        },
        update = {
            rv = it
        },
    )
}

class SortableListAdapter<T>(
    private val itemContent: @Composable (Int, T) -> Unit,
    private val onItemPlacementChange: (from: Int, to: Int) -> Unit,
) : RecyclerView.Adapter<SortableListAdapter<T>.ItemViewHolder>() {
    private var isItemMoved = false

    // Returning true for both of these functions makes sure to discard the default animation in DiffUtil and also solves the blinking issue
    // however don't worry :), the recyclerView will stay up to date with each change, because the display() function will update the List each time the state of the data list changes.
    private val differCallBack = object : DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(oldItem: T & Any, newItem: T & Any): Boolean {
            return true
        }

        override fun areContentsTheSame(oldItem: T & Any, newItem: T & Any): Boolean {
            return true
        }
    }

    val differ = AsyncListDiffer(this, differCallBack)

    fun display(data: List<T>) {
        differ.submitList(data)
    }

    fun moveItem(from: Int, to: Int) {
        notifyItemMoved(from, to)
        isItemMoved = true
    }

    fun onReorderInternalListDelegate(from: Int, to: Int) {
        if (isItemMoved) {
            onItemPlacementChange(from, to)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(ComposeView(parent.context))
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = differ.currentList[position]
        holder.display(
            item = item,
            itemContent = itemContent
        )
    }

    inner class ItemViewHolder(view: View) : ViewHolder(view) {
        fun display(
            item: T,
            itemContent: @Composable (Int, T) -> Unit,
        ) {
            (itemView as ComposeView).setContent {
                itemContent(bindingAdapterPosition, item)
            }
        }
    }
}

private fun <T> itemTouchHelper(
    adapter: SortableListAdapter<T>,
    alpha: Float,
    enableScaleAnimation: Boolean,
    scaleFactor: Float,
    isThereAnItemBeingDragged: (Boolean) -> Unit,
    selectedItemBackgroundColor: Color?
): ItemTouchHelper {
    val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
    ) {
        var from: Int? = null
        var to: Int? = null
        val animationDuration: Long = 150

        //An item has been moved from its position either up or down
        override fun onMove(
            recyclerView: RecyclerView,
            source: ViewHolder,
            target: ViewHolder
        ): Boolean {
            // Changing the UI
            val fromPosition = source.bindingAdapterPosition
            val toPosition = target.bindingAdapterPosition
            adapter.moveItem(from = fromPosition, to = toPosition)
            // Getting the first initial position of the item being moved, and save it
            if (from == null) {
                from = fromPosition
            }
            to = toPosition
            return true
        }

        //when an item changes its location that is currently selected
        override fun onSelectedChanged(viewHolder: ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)
            viewHolder?.let {
                isThereAnItemBeingDragged(true)
                if (actionState == ACTION_STATE_DRAG) {
                    viewHolder.itemView.alpha = alpha
                    if (enableScaleAnimation) {
                        animateViewScale(
                            viewHolder = viewHolder,
                            scale = scaleFactor,
                            animationDuration = animationDuration,
                        )
                    }
                    selectedItemBackgroundColor?.let {
                        viewHolder.itemView.setBackgroundColor(it.toArgb())
                    }
                }
            }
        }

        override fun onSwiped(viewHolder: ViewHolder, direction: Int) = Unit

        // When we stop dragging
        override fun clearView(recyclerView: RecyclerView, viewHolder: ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            validateIndices(
                itemsSize = adapter.itemCount,
                from = from,
                to = to
            ) { from, to ->
                adapter.onReorderInternalListDelegate(from, to)
            }
            viewHolder.itemView.alpha = 1f
            from = null; to = null // Reset the indices
            if (enableScaleAnimation) {
                animateViewScale(
                    viewHolder = viewHolder,
                    scale = 1f,
                    animationDuration = animationDuration,
                )
            }
            viewHolder.itemView.background = null
            isThereAnItemBeingDragged(false)
        }
    }
    return ItemTouchHelper(itemTouchHelperCallback)
}

private class ItemsVerticalSpacing(private val amount: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.bottom = amount
    }
}
