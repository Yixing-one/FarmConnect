package com.example.farmconnect.ui.farmer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.compose.material3.TextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.farmconnect.R
import com.example.farmconnect.ui.theme.FarmConnectTheme
import com.example.farmconnect.ui.theme.darkGreen
import com.example.farmconnect.view.Screens
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MarketPlaceViewModel: ViewModel() {
    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _items = MutableStateFlow<List<MarketPlaceItem>>(listOf())
    val items: StateFlow<List<MarketPlaceItem>> = searchText
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
                .whereEqualTo("userId", currentUserId)
                .get()
                .await()

            val marketItems = ArrayList<MarketPlaceItem>()

            for (document in documents) {
                val docData = document.data
                val storageRef = storage.reference
                val imageRef = storageRef.child(docData.getValue("imageUrl").toString())

                val TEN_MEGABYTE:Long = 1024 * 1024 * 10
                val bytes = imageRef.getBytes(TEN_MEGABYTE).await()
                val imageBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                marketItems.add(
                    MarketPlaceItem(
                        documentId = document.id,
                        name = docData.getValue("name").toString(),
                        price = docData.getValue("price").toString().toDouble(),
                        quantityRemaining = docData.getValue("quantityRemaining").toString().toInt(),
                        quantitySold = docData.getValue("quantitySold").toString().toInt(),
                        imageBitmap = imageBitmap
                    )
                )
            }

            _items.emit(marketItems.toList())

        } catch (exception: Exception) {
            isLoading.emit(false)
        } finally {
            isLoading.emit(false)
        }
    }

    fun onSearchTextChange(text: String){
        _searchText.value = text
    }
}

data class MarketPlaceItem(
    val documentId: String,
    val name: String,
    val price: Double,
    val quantityRemaining: Int,
    val quantitySold: Int,
    val imageBitmap: Bitmap
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
fun MarketItemCard(item: MarketPlaceItem, modifier: Modifier = Modifier){
    Card(
        modifier = modifier
            .width(350.dp)
            .height(270.dp)
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
                text = "${item.quantityRemaining} lb",
                modifier = Modifier.padding(start = 13.dp, end = 10.dp, top = 0.dp, bottom = 10.dp),
                style = MaterialTheme.typography.bodySmall,
            )
            if(item.quantitySold != 0){
                Image(
                    painter = painterResource(R.drawable.plus_sign),
                    contentDescription = "image",
                    modifier = Modifier
                        .width(40.dp)
                        .height(40.dp)
                        .padding(5.dp, 0.dp),
                    contentScale = ContentScale.Fit
                )
                Text(
                    text = "Sold ${item.quantitySold} lb",
                    modifier = Modifier.padding(start = 0.dp, end = 0.dp, top = 0.dp, bottom = 0.dp),
                    style = TextStyle(
                        fontSize = 15.sp,
                        color = Color.Blue,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen(navController: NavController){
    val viewModel = viewModel<MarketPlaceViewModel>()
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

        Row {
            Button(
                onClick = { navController.navigate(Screens.EditMarketplace.name) },
                colors = ButtonDefaults.buttonColors(containerColor = darkGreen),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Text(text = "Edit Postings")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = { navController.navigate(Screens.AddPostingMarketplace.name) },
                colors = ButtonDefaults.buttonColors(containerColor = darkGreen),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Text(text = "Add Postings")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 128.dp)
        ) {
            items(theFoodItems.size) { item ->
                MarketItemCard(
                    item = theFoodItems.get(item),
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(150.dp))

        Row(verticalAlignment = Alignment.Bottom) {
//            Spacer(modifier = Modifier.width(150.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Your other content here...

                    Spacer(modifier = Modifier.height(10.dp))

                    // The two Text elements in the Column
                    Text(
                        text = "Total earning on June 26:",
                        style = TextStyle(
                            fontSize = 20.sp,
                            color = Color.Blue,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp, 3.dp, 5.dp, 0.dp),
                    )

                    Text(
                        text = "   610 CAD",
                        style = TextStyle(
                            fontSize = 20.sp,
                            color = Color.Cyan,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp, 3.dp, 5.dp, 0.dp),
                    )
                }
            }
        }
    }


}

@Composable
fun MarketplaceScreen(){
    val navController = rememberNavController()
    FarmConnectTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            MarketScreen(navController)
        }
    }
}