package com.example.farmconnect.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
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
import com.example.farmconnect.ui.theme.FarmConnectTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

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
private val allItems = listOf(
    Item(
        name = "Carrots",
        price = 1.55,
        quantity = 71,
        imageId = R.drawable.carrot
    ),
    Item(
        name = "Tomatoes",
        price = 3.89,
        quantity = 93,
        imageId = R.drawable.tomatoes
    ),
    Item(
        name = "Corn",
        price = 2.29,
        quantity = 87,
        imageId = R.drawable.corn
    ),
    Item(
        name = "Bell Peppers",
        price = 0.99,
        quantity = 22,
        imageId = R.drawable.bell_pepper
    ),
    Item(
        name = "Potatoes",
        price = 1.25,
        quantity = 103,
        imageId = R.drawable.potatoes
    ),
    Item(
        name = "Onions",
        price = 4.23,
        quantity = 98,
        imageId = R.drawable.onions
    ),
    Item(
        name = "Iceberg Lettuce",
        price = 1.46,
        quantity = 19,
        imageId = R.drawable.iceberg_lettuce
    ),
    Item(
        name = "Cucumbers",
        price = 5.89,
        quantity = 34,
        imageId = R.drawable.cucumber
    ),
    Item(
        name = "Romaine Lettuce",
        price = 3.29,
        quantity = 45,
        imageId = R.drawable.romaine_lettuce
    ),
)


@Composable
fun ItemCard(item: Item, modifier: Modifier = Modifier){
    Card(
        modifier = modifier,
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