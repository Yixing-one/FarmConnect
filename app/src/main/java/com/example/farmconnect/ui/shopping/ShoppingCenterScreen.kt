package com.example.farmconnect.ui.shopping

import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.farmconnect.ui.theme.FarmConnectTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class MainViewModel: ViewModel() {
    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _items = MutableStateFlow<List<MarketplaceItem>>(listOf())
    val items: StateFlow<List<MarketplaceItem>> = searchText
        .combine(_items) { text, items ->
            if (text.isBlank()) {
                items
            } else {
                items.filter {
                    it.doesMatchSearchQuery(text)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = _items.value
        )


    val isLoading = MutableStateFlow(true)


    init {
        viewModelScope.launch {
            loadItems()
        }
    }

    private suspend fun loadItems() {
        isLoading.emit(true) // Start loading
        try {
            val documents = db.collection("marketplace")
                .get()
                .await()

            // Initialize list to store Jobs
            val jobs = mutableListOf<Job>()

            // Mutable list to store MarketplaceItem
            val marketItems = mutableListOf<MarketplaceItem>()

            for (document in documents) {
                jobs.add(GlobalScope.launch {
                    val docData = document.data
                    val storageRef = storage.reference
                    val imageRef = storageRef.child(docData.getValue("imageUrl").toString())

                    val TEN_MEGABYTE:Long = 1024 * 1024 * 10
                    val bytes = imageRef.getBytes(TEN_MEGABYTE).await()
                    val imageBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                    // Ensure that the items are added in the Main thread if you plan to update the UI immediately
                    withContext(Dispatchers.Main) {
                        if(docData.getValue("quantityRemaining").toString().toInt() > 0){
                            marketItems.add(
                                MarketplaceItem(
                                    id = document.id.toString(),
                                    name = docData.getValue("name").toString(),
                                    price = docData.getValue("price").toString().toDouble(),
                                    quantityRemaining = docData.getValue("quantityRemaining").toString().toInt(),
                                    imageBitmap = imageBitmap,
                                    userId = docData.getValue("userId").toString()
                                )
                            )
                        }
                    }
                })
            }

            // Wait for all jobs to complete
            jobs.joinAll()

            _items.emit(marketItems.toList())

        } catch (exception: Exception) {
            Log.d("error", exception.message.toString())
            isLoading.emit(false)
        } finally {
            isLoading.emit(false)
        }
    }


    fun onSearchTextChange(text: String){
        _searchText.value = text
    }

}
data class MarketplaceItem(
    val id: String,
    val name: String,
    val price: Double,
    val quantityRemaining: Int,
    val imageBitmap: Bitmap,
    val userId: String
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


@Composable
fun ItemCard(item: MarketplaceItem, modifier: Modifier = Modifier, cartViewModel: CartViewModel){
    fun isEnabled(): Boolean {
        val grouped = cartViewModel.items.groupBy { it.id }
        if(!grouped.containsKey(item.id)){
            return true;
        }
        val addedQuantity = grouped[item.id]?.size
        if (addedQuantity != null) {
            return addedQuantity <= item.quantityRemaining - 1
        }
        return true;
    }

    Card(
        modifier = modifier.width(150.dp).height(260.dp)
    ) {
        Column{
            Image(
                painter = rememberAsyncImagePainter(model = item.imageBitmap),
                contentDescription = "image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = ContentScale.Crop
            )
            Text(
                text = "$${item.price}/lb",
                modifier = Modifier.padding(start = 13.dp, end = 10.dp, top = 10.dp, bottom = 7.dp),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "${item.name}",
                modifier = Modifier.padding(start = 13.dp, end = 10.dp, top = 3.dp, bottom = 3.dp),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = "${item.quantityRemaining} lb available",
                modifier = Modifier.padding(start = 13.dp, end = 10.dp, top = 0.dp, bottom = 10.dp),
                style = MaterialTheme.typography.bodySmall,
            )
            Button(
                onClick = { cartViewModel.addToCart(item) },
                enabled = isEnabled(),
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(7.5.dp)
            ) {
                Text(text = "Add to Cart", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingCenterScreen(cartViewModel: CartViewModel){
    val viewModel = viewModel<MainViewModel>()
    val searchText by viewModel.searchText.collectAsState()
    val theFoodItems by viewModel.items.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ){
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator() // Show loading indicator
            }

        } else {
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
                        modifier = Modifier.padding(8.dp),
                        cartViewModel = cartViewModel
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ShoppingCenterScreenPreview() {
    val cartViewModel = viewModel<CartViewModel>();
    FarmConnectTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            ShoppingCenterScreen(cartViewModel)
        }
    }
}