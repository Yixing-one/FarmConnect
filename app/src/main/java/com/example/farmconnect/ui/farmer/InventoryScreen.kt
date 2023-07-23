package com.example.farmconnect.ui.farmer

import android.Manifest
import android.R
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Picture
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.farmconnect.SpeechRecognizerContract
import com.example.farmconnect.ui.theme.FarmConnectTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
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
import com.example.farmconnect.data.Inventory_Item
import com.example.farmconnect.data.Inventory_Items
import java.io.ByteArrayOutputStream


lateinit var cur_context: Context
lateinit var viewModel: MainViewModel

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _items = MutableStateFlow<List<Inventory_Item>>(listOf())
    val items: StateFlow<List<Inventory_Item>> = searchText
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

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            cur_context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                return true
            }
        }
        return false
    }

    private fun imageViewToBitmap(imageView: ImageView): Bitmap {
        val drawable = imageView.drawable
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        // Create a new bitmap and canvas, and draw the drawable onto the canvas
        val bitmap = Bitmap.createBitmap(imageView.width, imageView.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    suspend fun loadItems() {
        isLoading.emit(true) // Start loading
        try {
            if (isNetworkAvailable()) {
                //remove all the item in the local cache
                Inventory_Items.item_list = mutableListOf<Inventory_Item>()

                val documents = db.collection("inventory")
                    .whereEqualTo("userId", currentUserId)
                    .get()
                    .await()

                //load all the items from database to local cache
                for (document in documents) {
                    val docData = document.data
                    val imageUrl = docData.getValue("imageUrl").toString()
                    val storageRef = storage.reference
                    val imageRef = storageRef.child(docData.getValue("imageUrl").toString())
                    val TEN_MEGABYTE: Long = 1024 * 1024 * 10
                    val bytes = imageRef.getBytes(TEN_MEGABYTE).await()

                    //add the item to local cache
                    val name = docData.getValue("name").toString()
                    val price = docData.getValue("price").toString().toDouble()
                    val quantity = docData.getValue("quantity").toString().toInt()
                    val imageBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    Inventory_Items.addItem(name, price, quantity, imageBitmap)
                }
            }
            _items.emit(Inventory_Items.item_list.toList())

        } catch (exception: Exception) {
            isLoading.emit(false)
        } finally {
            isLoading.emit(false)
        }
    }

    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalGlideComposeApi::class)
@Composable
fun ItemCard(item: Inventory_Item, modifier: Modifier = Modifier) {

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
        Column {
            Image(
                painter = rememberAsyncImagePainter(model = item.imageBitmap),
                contentDescription = "image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = ContentScale.Crop
            )
            /*GlideImage(
                model = "$${item.imageUrl}",
                contentDescription = "${item.name}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp), // Modify the image size as needed
                contentScale = ContentScale.Crop){
                // shows a placeholder ImageBitmap when loading.
                // shows an error ImageBitmap when the request failed.
                it.error("$${item.imageUrl}")
                    .placeholder(R.drawable.alert_dark_frame)
                    .load( "$${item.imageUrl}")
            }*/

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
private fun ExtendedFABComponent() {
    val showDialog = remember { mutableStateOf(false) }
    val capturedImage = remember { mutableStateOf<Bitmap?>(null) }
    val itemName = remember { mutableStateOf("") }
    val itemQuantity = remember { mutableStateOf("") }
    val itemPrice = remember { mutableStateOf("") }


    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            showDialog.value = true
            capturedImage.value = imageBitmap
        }
    }

    fun addItemToFirestore(imageBitmap: Bitmap) {
        // add captured image to Firestore Storage
        val storage = Firebase.storage
        val storageRef = storage.reference
        val imagesRef = storageRef.child("images/crops")
        val fileName = "image_${System.currentTimeMillis()}.png"
        val imageRef = imagesRef.child(fileName)

        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val imageData = baos.toByteArray()

        val uploadTask = imageRef.putBytes(imageData)
        uploadTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Image uploaded successfully
                // Create a new doc in the inventory collection
                val db = Firebase.firestore
                val inventoryColRef = db.collection("inventory");
                val data = hashMapOf(
                    "name" to itemName.value,
                    "quantity" to itemQuantity.value,
                    "price" to itemPrice.value,
                    "userId" to FirebaseAuth.getInstance().currentUser?.uid.toString(),
                    "imageUrl" to imageRef.path.toString()
                )
                inventoryColRef.add(data);
            }

            // clear input values
            itemName.value = ""
            itemQuantity.value = ""
            itemPrice.value = ""
        }
    }

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text("Add Item to inventory") },
            text = {
                Column {
                    capturedImage.value?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(200.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = itemName.value,
                        onValueChange = { itemName.value = it },
                        label = { Text("Enter Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = itemQuantity.value,  // This should be a mutable state of String
                        onValueChange = {
                            if (it.toFloatOrNull() != null || it.isEmpty()) {
                                itemQuantity.value = it
                            }
                        },
                        label = { Text("Enter quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = itemPrice.value,  // This should be a mutable state of String
                        onValueChange = {
                            if (it.toFloatOrNull() != null || it.isEmpty()) {
                                itemPrice.value = it
                            }
                        },
                        label = { Text("Enter price") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                }
            },
            confirmButton = {
                Button(onClick = {
                    // Upload the captured image to Firestore Storage
                    capturedImage.value?.let { imageBitmap ->
                        addItemToFirestore(imageBitmap)
                    }

                    // Close the dialog
                    showDialog.value = false
                }) {
                    Text("OK")
                }
            }
        )
    }

    val context = LocalContext.current
    FloatingActionButton(
        onClick = { takePicture(context, cameraLauncher) },
        content = {
            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                    .padding(horizontal = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Item",
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text("Add Item")
            }
        }

    )
}

private fun takePicture(context: Context, cameraLauncher: ActivityResultLauncher<Intent>) {
    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    if (cameraIntent.resolveActivity(context.packageManager) != null) {
        cameraLauncher.launch(cameraIntent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen() {
    cur_context = LocalContext.current
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
    ExtendedFABComponent()
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