package com.loc.composethings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.util.Collections

class MainViewModel : ViewModel() {

    val items = MutableStateFlow((1..300).map { it.toString() })

    fun swapItems(from: Int, to: Int) {
        val currentList = items.value.toMutableList()
        val temp = currentList[from]
        currentList.removeAt(from)
        currentList.add(to,temp)
        items.value = currentList
    }
}

