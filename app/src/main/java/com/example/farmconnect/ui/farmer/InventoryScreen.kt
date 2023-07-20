package com.example.farmconnect.ui.farmer

import android.Manifest
import android.app.Application
import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.farmconnect.SpeechRecognizerContract
import com.example.farmconnect.ui.theme.FarmConnectTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _items = MutableStateFlow<List<Item>>(listOf())
    val items: StateFlow<List<Item>> = searchText
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
            val documents = db.collection("inventory")
                .whereEqualTo("userId", currentUserId)
                .get()
                .await()

            val invItems = ArrayList<Item>()

            for (document in documents) {
                val docData = document.data
                val storageRef = storage.reference
                val imageRef = storageRef.child(docData.getValue("imageUrl").toString())

                val TEN_MEGABYTE: Long = 1024 * 1024 * 10
                val bytes = imageRef.getBytes(TEN_MEGABYTE).await()
                val imageBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                invItems.add(
                    Item(
                        name = docData.getValue("name").toString(),
                        price = docData.getValue("price").toString().toDouble(),
                        quantity = docData.getValue("quantity").toString().toInt(),
                        imageBitmap = imageBitmap
                    )
                )
            }

            _items.emit(invItems.toList())

        } catch (exception: Exception) {
            isLoading.emit(false)
        } finally {
            isLoading.emit(false)
        }
    }

    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }


    data class Item(
        val name: String,
        val price: Double,
        val quantity: Int,
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


    @OptIn(ExperimentalPermissionsApi::class)
    @Composable

    fun ItemCard(item: Item, modifier: Modifier = Modifier) {
        val permissionState = rememberPermissionState(permission = Manifest.permission.RECORD_AUDIO)
        fun updateInventory(itemName: String, increment: Int) {
            viewModelScope.launch {
                // Query Firestore to find the document that contains the item and belongs to the current user
                val querySnapshot = db.collection("inventory")
                    .whereEqualTo("userID", currentUserId)
                    .whereEqualTo("name", itemName)
                    .get()
                    .await()
                Log.d(TAG, "Increment Value: $increment")
                // Check if a document was found
                if (!querySnapshot.isEmpty) {
                    // Get the first document from the results (there should be only one document that matches the query)
                    val doc = querySnapshot.documents[0]

                    // Update the quantity of the item
                    doc.reference.update(
                        "items.$itemName",
                        FieldValue.increment(increment.toLong())
                    )
                } else {
                    Log.d("TAG,", "No document found for item: $itemName")
                }
            }
        }


        SideEffect {
            permissionState.launchPermissionRequest()
        }

        val speechRecognizerLauncher = rememberLauncherForActivityResult(
            contract = SpeechRecognizerContract(),
            onResult = { result ->
                val spokenText = result?.get(0)
                val increment = spokenText?.toInt()
                if (increment != null) {
                    updateInventory(itemName = item.name, increment = increment)
                };
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
            Column {
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
                    modifier = Modifier.padding(
                        start = 13.dp,
                        end = 10.dp,
                        top = 10.dp,
                        bottom = 7.dp
                    ),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "${item.name}",
                    modifier = Modifier.padding(
                        start = 13.dp,
                        end = 10.dp,
                        top = 3.dp,
                        bottom = 3.dp
                    ),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = "${item.quantity} lb",
                    modifier = Modifier.padding(
                        start = 13.dp,
                        end = 10.dp,
                        top = 0.dp,
                        bottom = 10.dp
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun InventoryScreen() {
        val viewModel = viewModel<MainViewModel>()
        val searchText by viewModel.searchText.collectAsState()
        val theFoodItems by viewModel.items.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
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
                Row {
                    TextField(
                        value = searchText,
                        onValueChange = viewModel::onSearchTextChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(text = "Search") },
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
    }

    @Preview(showBackground = true)
    @Composable
    fun InventoryScreenPreview() {
        FarmConnectTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                InventoryScreen()
            }
        }
    }
