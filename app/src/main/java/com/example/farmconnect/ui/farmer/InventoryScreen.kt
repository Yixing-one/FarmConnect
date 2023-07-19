package com.example.farmconnect.ui.farmer

import android.Manifest
import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.annotation.DrawableRes
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
import com.google.android.gms.common.util.JsonUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import io.grpc.internal.JsonUtil
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.AccessController.getContext
import com.example.farmconnect.R
import org.json.JSONArray
import org.json.JSONException
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import java.io.IOException
import androidx.compose.material3.TextField


class MainViewModel(application: Application) : AndroidViewModel(application) {

    //private val db = Firebase.firestore
    //private val storage = Firebase.storage
    //private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()

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
    var invItems = ArrayList<Item>()

    fun startViewModel(context: Context) {
        println("startViewModel")
        viewModelScope.launch {
            //loadItems()
            loadItems_local_cache(context)
        }
    }

    private fun readJsonDataFromAssets(context: Context, fileName: String): JSONArray? {
        var jsonArray: JSONArray? = null
        try {
            val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
            jsonArray = JSONArray(jsonString)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return jsonArray
    }

    private suspend fun loadItems_local_cache(context: Context) {
        println("loadItems_local_cache")
        // Read the JSON data from the fill
        val jsonArray = readJsonDataFromAssets(context, "inventory_items.json")

        //debug
        if (jsonArray != null) {
            for (i in 0 until jsonArray.length()) {
                val element = jsonArray.get(i)
                println(element)
            }
        }

        if (jsonArray != null) {
            for (i in 0 until jsonArray.length()) {
                val itemJsonObject = jsonArray.getJSONObject(i)
                val item_name = itemJsonObject.getString("name")
                val item_price = itemJsonObject.getString("price")
                val item_quantity = itemJsonObject.getString("quantity")
                val item_image = R.drawable.carrot

                invItems.add(
                    Item(
                        name = item_name,
                        price = item_price.toDouble(),
                        quantity = item_quantity.toInt(),
                        imageId = item_image
                    )
                )
            }
        }
        _items.emit(invItems.toList())
        println(_items)
        println("loadItems_local_cache ends")
    }

    fun onSearchTextChange(text: String){
        _searchText.value = text
    }

    /*
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

                val TEN_MEGABYTE:Long = 1024 * 1024 * 10
                val bytes = imageRef.getBytes(TEN_MEGABYTE).await()
                val imageBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                invItems.add(
                    Item(
                        name = docData.getValue("name").toString(),
                        price = docData.getValue("price").toString().toDouble(),
                        quantity = docData.getValue("quantity").toString().toInt(),
                        xd = imageBitmap
                    )
                )
            }

            _items.emit(invItems.toList())

        } catch (exception: Exception) {
            isLoading.emit(false)
        } finally {
            isLoading.emit(false)
        }
    }*/
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

    val permissionState = rememberPermissionState(permission = Manifest.permission.RECORD_AUDIO)
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
    val context = LocalContext.current // Access the Context using LocalContext.current
    val viewModel = viewModel<MainViewModel>()
    viewModel.startViewModel(context)
    val searchText by viewModel.searchText.collectAsState()
    val theFoodItems by viewModel.items.collectAsState()
    println(theFoodItems)
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
                    placeholder = {Text(text = "Search") },
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 128.dp)
            ) {
                items(theFoodItems.size) { item ->
                    println(item)
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
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            InventoryScreen()
        }
    }
}