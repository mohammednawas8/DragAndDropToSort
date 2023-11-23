package com.loc.composethings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class SortableListTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun displaySetOfItemsInTheListAssertItemsAreVisible() {
        val items = (0..500).map { it.toString() }
        val adapter = SortableListAdapter<String>(
            itemContent = { _, _ -> },
            onItemPlacementChange = { from, to -> }
        )
        composeRule.setContent {
            SortableList(
                draggingState = rememberDraggingState(),
                items = items,
                itemContent = { _, item ->
                    Text(text = item, modifier = Modifier.fillMaxWidth())
                },
                onItemPlacementChange = { from, to ->

                },
                rvAdapter = adapter
            )
        }
        composeRule.onNodeWithTag("sortable list").assertExists()
        assert(
            items.size == adapter.itemCount
        )
    }

    @Test
    fun addAnItemToTheListAssertItemIsIncluded() {
        val adapter = SortableListAdapter<String>(
            itemContent = { _, _ -> },
            onItemPlacementChange = { from, to -> }
        )
        composeRule.setContent {
            val items = remember {
                mutableListOf("1", "2", "3", "4")
            }
            Column {
                Button(
                    onClick = {
                        items.add("5")
                    },
                    modifier = Modifier.testTag("add new item")
                ) {}
                SortableList(
                    draggingState = rememberDraggingState(),
                    items = items,
                    itemContent = { _, item ->
                        Text(text = item, modifier = Modifier.fillMaxWidth())
                    },
                    onItemPlacementChange = { from, to ->

                    },
                    rvAdapter = adapter
                )
            }
        }
        composeRule.onNodeWithTag("sortable list").assertExists()
        composeRule.onNodeWithTag("add new item").performClick()
        assert(
            adapter.itemCount == 5
        )
    }

    @Test
    fun moveItemPlaceAssertTheNewOrderedList() {
        val items = mutableListOf<String>("1", "2", "3", "4")
        val oldItems = items.toList()
        var source = -1
        var destination = -1
        val adapter = SortableListAdapter<String>(
            itemContent = { _, _ ->

            },
            onItemPlacementChange = { from, to ->
                source = from
                destination = to
                val temp = items[from]
                items.removeAt(from)
                items.add(to, temp)
            }
        )
        composeRule.setContent {
            Column {
                Button(
                    onClick = {
                        adapter.moveItem(0, 3)
                        adapter.onReorderInternalListDelegate(0, 3)
                    },
                    modifier = Modifier.testTag("move items")
                ) {}
                SortableList(
                    draggingState = rememberDraggingState(),
                    items = items,
                    itemContent = { _, item ->
                        Text(text = item, modifier = Modifier.fillMaxWidth())
                    },
                    onItemPlacementChange = { _, _ ->
                    },
                    rvAdapter = adapter
                )
            }
        }
        composeRule.onNodeWithTag("sortable list").assertExists()
        composeRule.onNodeWithTag("move items").performClick()
        val currentItems = adapter.differ.currentList
        assert(oldItems[source] == currentItems[destination])
    }
}