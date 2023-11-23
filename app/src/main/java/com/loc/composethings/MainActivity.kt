package com.loc.composethings

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loc.composethings.ui.theme.ComposeThingsTheme

class MainActivity : ComponentActivity() {

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel by viewModels<MainViewModel>()
        setContent {
            val items by viewModel.items.collectAsState()
            ComposeThingsTheme {
                val draggingState = rememberDraggingState()
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentColor = Color.White,
                    topBar = {
                        TopAppBar(
                            modifier = Modifier.shadow(14.dp),
                            title = {
                                Text(text = "Drag and drop to resort")
                            },
                            actions = {
                                TextButton(onClick = {
                                    draggingState.enableDragging = !draggingState.enableDragging
                                }) {
                                    Text(text = "Enable/disable")
                                }
                            }
                        )
                    },
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = true,
                                onClick = { /*TODO*/ },
                                icon = { Text(text = "A") }
                            )
                            NavigationBarItem(
                                selected = true,
                                onClick = { /*TODO*/ },
                                icon = { Text(text = "B") }
                            )
                        }
                    }
                ) {
                    SortableList(
                        modifier = Modifier
                            .padding(
                                bottom = it.calculateBottomPadding(), top = it.calculateTopPadding()
                            )
                            .fillMaxSize(),
                        items = items,
                        itemContent = { index, item ->
                            ListItem(item = item)
                        },
                        onItemPlacementChange = { from, to ->
                            viewModel.swapItems(from, to)
                        },
                        itemsVerticalSpacing = 0,
                        draggingProperties = DraggingProperties(
                            alpha = 0.7f,
                            enableScaleAnimation = true,
                            scaleFactor = 1.05f,
                            selectedItemBackgroundColor = Color.LightGray
                        ),
                        draggingState = draggingState,
                    )
                }
            }
        }
    }
}

@Composable
fun ListItem(
    item: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = item,
            color = Color.Black,
            modifier = Modifier.padding(20.dp),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Divider()
    }
}