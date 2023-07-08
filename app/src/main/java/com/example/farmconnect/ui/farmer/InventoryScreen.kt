package com.example.farmconnect.ui.farmer

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.farmconnect.R
import com.example.farmconnect.SpeechRecognizerContract
import com.example.farmconnect.ui.theme.FarmConnectTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import com.example.farmconnect.data.allItems
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

class MainViewModel: ViewModel() {
    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val _items = MutableStateFlow(allItems)
    val items = searchText
        .combine(_items){ text, items ->
            if(text.isBlank()){
                items
            }
            else{
                items.filter{
                    it.doesMatchSearchQuery(text)
                }
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            _items.value
        )

    fun onSearchTextChange(text: String){
        _searchText.value = text
    }

}
data class Item(
    val name: String,
    val price: Double,
    val quantity: Int,
    @DrawableRes val imageId: Int
) {
    fun doesMatchSearchQuery(query: String): Boolean {
        val matchingCombinations = listOf(
            "$name",
        )
        return matchingCombinations.any {
            it.contains(query, ignoreCase = true)
        }
    }
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ItemCard(item: Item, modifier: Modifier = Modifier){

    val permissionState = rememberPermissionState(
        permission = Manifest.permission.RECORD_AUDIO
        )
        SideEffect {
            permissionState.launchPermissionRequest()
        }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = SpeechRecognizerContract(),
        onResult = {
            //make DB call to update inventory for specific item
            // add toast message to show updated item value
            Log.d("TAG,", "val is: " + it.toString());
            //#viewModel.changeTextValue(it.toString())
        }
    )


    Card(
        modifier = modifier
            .width(150.dp)
            .height(230.dp)
            .clickable {
                if (permissionState.status.isGranted) {
                    speechRecognizerLauncher.launch(Unit)
                } else
                    permissionState.launchPermissionRequest()
            }
    ) {
        Column{
           Image(
                painter = painterResource(id = item.imageId),
                contentDescription = "image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = ContentScale.Crop
            )
            Text(
                text = "$${item.price}",
                modifier = Modifier.padding(start = 13.dp, end = 10.dp, top = 10.dp, bottom = 7.dp),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "${item.name}",
                modifier = Modifier.padding(start = 13.dp, end = 10.dp, top = 3.dp, bottom = 3.dp),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = "${item.quantity} lb",
                modifier = Modifier.padding(start = 13.dp, end = 10.dp, top = 0.dp, bottom = 10.dp),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(){
    val viewModel = viewModel<MainViewModel>()
    val searchText by viewModel.searchText.collectAsState()
    val theFoodItems by viewModel.items.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ){
        Row{
            TextField(
                value = searchText,
                onValueChange = viewModel::onSearchTextChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {Text(text = "Search")},
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 128.dp)
        ) {
            items(theFoodItems.size) { item ->
                ItemCard(
                    item = theFoodItems.get(item),
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InventoryScreenPreview() {
    FarmConnectTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            InventoryScreen()
        }
    }
}