package com.example.farmconnect.ui.farmer

import android.Manifest
import android.R
import android.app.Activity
import android.app.Application
import android.content.ContentValues.TAG
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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import java.io.ByteArrayOutputStream
import java.util.concurrent.CompletableFuture
import kotlinx.coroutines.*


lateinit var cur_context: Context
lateinit var viewModel: MainViewModel
var internet_available = false

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private var db = Firebase.firestore
    private var storage = Firebase.storage
    private var currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()

    private var _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private var _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    var _items = MutableStateFlow<List<Inventory_Item>>(listOf())
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

    val isLoading = MutableStateFlow(false)

    init{
        viewModelScope.launch {
            loadItems()
        }
    }

    fun isNetworkAvailable() {
        val connectivityManager =
            cur_context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                internet_available = true
                return
            }
        }
        internet_available = false
    }

    suspend fun loadItems() {
        internet_available = false
        isNetworkAvailable()

        if (isLoading.value == true) {
            return
        }
        isLoading.emit(true)

        try {
            db = Firebase.firestore
            storage = Firebase.storage

            if (internet_available && (Inventory_Items.item_list.size == 0) && (Inventory_Items.update_item_list.size == 0)) {
                // remove all the items in the local cache
                Inventory_Items.item_list = mutableListOf<Inventory_Item>()

                val documents = db.collection("inventory")
                    .whereEqualTo("userId", currentUserId)
                    .get()
                    .await()

                val jobs = launchImageDownloadJobs(documents)
                jobs.joinAll() // Wait for all image downloads to complete
                _items.emit(Inventory_Items.item_list)
            } else if (internet_available && (Inventory_Items.update_item_list.size != 0)) {
                for (item in Inventory_Items.update_item_list) {
                    addItemToFirestore(item.name, item.price, item.quantity, item.imageBitmap)
                }

                // remove all the items in the local cache
                Inventory_Items.update_item_list = mutableListOf<Inventory_Item>()
                Inventory_Items.item_list = mutableListOf<Inventory_Item>()

                val documents = db.collection("inventory")
                    .whereEqualTo("userId", currentUserId)
                    .get()
                    .await()

                val jobs = launchImageDownloadJobs(documents)
                jobs.joinAll() // Wait for all image downloads to complete
                _items.emit(Inventory_Items.item_list)
            } else if (!internet_available) {
                _items.value = Inventory_Items.item_list.toList()
            } else {
                // remove all the items in the local cache
                Inventory_Items.item_list = mutableListOf<Inventory_Item>()

                val documents = db.collection("inventory")
                    .whereEqualTo("userId", currentUserId)
                    .get()
                    .await()

                val jobs = launchImageDownloadJobs(documents)
                jobs.joinAll() // Wait for all image downloads to complete
                _items.emit(Inventory_Items.item_list)
            }

        } catch (exception: Exception) {
            isLoading.emit(false)

        } finally {
            isLoading.emit(false)
        }
    }

//    fun updateInventory(itemName: String, increment: Int) {
//        viewModelScope.launch {
//            // Query Firestore to find the document that contains the item and belongs to the current user
//            val querySnapshot = db.collection("inventory")
//                .whereEqualTo("userID", currentUserId)
////                .whereEqualTo("name", itemName)
//                .get()
//                .await()
//            Log.d(TAG, "Increment Value: $increment")
//            // Check if a document was found
//            if (!querySnapshot.isEmpty) {
//                // Get the first document from the results (there should be only one document that matches the query)
//                val doc = querySnapshot.documents[0]
//
//                // Update the quantity of the item
//                doc.reference.update(
//                    "items.$itemName",
//                    FieldValue.increment(increment.toLong())
//                )
//            } else {
//                Log.d("TAG,", "No document found for item: $itemName")
//            }
//        }
//    }
suspend fun updateQuanity(documentID: String, increment: Int) {
    val inventoryDocuments = db.collection("inventory")

    if (documentID != null) {
        val docData = inventoryDocuments.document(documentID).get().await().data;
        var quantity = docData?.getValue("quantity").toString().toInt()
        quantity += increment
        inventoryDocuments.document(documentID).update("quantity", quantity).await();
        this.loadItems()
    }
}

    private fun launchImageDownloadJobs(documents: QuerySnapshot): MutableList<Job> {
        val jobs = mutableListOf<Job>()
        for (i in 0 until documents.size() step 1) {
            jobs.add(GlobalScope.launch {
                val document = documents.documents[i]
                val docData = document.data
                val storageRef = storage.reference
                val imageRef = storageRef.child(docData?.getValue("imageUrl").toString())
                val TEN_MEGABYTE: Long = 1024 * 1024 * 10
                val bytes = imageRef.getBytes(TEN_MEGABYTE).await()

                // add the item to local cache
                val docId = document.id
                val name = docData?.getValue("name").toString()
                val price = docData?.getValue("price").toString().toDouble()
                val quantity = docData?.getValue("quantity").toString().toInt()
                val imageBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                withContext(Dispatchers.Main) {
                    Inventory_Items.addItem(name, price, quantity, imageBitmap, docId)
                }
            })
        }
        return jobs
    }

    fun onSearchTextChange(text: String) {
        _searchText.value = text
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
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalGlideComposeApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun ItemCard(item: Inventory_Item, viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val theMainViewModel = viewModel<MainViewModel>()

    val permissionState = rememberPermissionState(permission = Manifest.permission.RECORD_AUDIO)
    SideEffect {
        permissionState.launchPermissionRequest()
    }


//    theMainViewModel.viewModelScope.launch {
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = SpeechRecognizerContract(),
        onResult = { result ->
            println(result)
            println("dsfhkghifshjgiosdgiosjgdfiosjgdiodsjgiosdjgiodjfgiosdgj")
            val spokenText = result?.get(0)
            val increment = spokenText!!.split(" ")[0].toInt()
            if (increment != null && item.documentId != null) {
                viewModel.viewModelScope.launch {
                    viewModel.updateQuanity(item.documentId, increment)
                }

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
                text = "${item.quantity} lb",
                modifier = Modifier.padding(start = 13.dp, end = 10.dp, top = 0.dp, bottom = 10.dp),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

suspend fun addItemToFirestore(name:String, price:Double, quantity:Int, imageBitmap: Bitmap) {
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
                "name" to name,
                "quantity" to quantity,
                "price" to price,
                "userId" to FirebaseAuth.getInstance().currentUser?.uid.toString(),
                "imageUrl" to imageRef.path.toString()
            )
            inventoryColRef.add(data);
        }
    }
    Log.d("TAG,", "option 2_2");
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExtendedFABComponent(viewModel: MainViewModel) {
    val showDialog = remember { mutableStateOf(false) }
    val fieldMissingDialog = remember { mutableStateOf(false) }
    var capturedImage = remember { mutableStateOf<Bitmap?>(null) }
    var itemName = remember { mutableStateOf("") }
    var itemQuantity = remember { mutableStateOf("") }
    var itemPrice = remember { mutableStateOf("") }
    var message = remember { mutableStateOf("") }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            print("picture successfully taken")
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            showDialog.value = true
            capturedImage.value = imageBitmap
        }
    }
    val activity = LocalContext.current as Activity

    //pop up a dialog telling user that some fields are missing
    if (fieldMissingDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text("Fail To Add Item!",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red)},
            text = {
                Text(message.value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray)
            },
            confirmButton = {
                Button(onClick = {
                    fieldMissingDialog.value = false
                    message.value = ""
                }) {
                    Text("OK")
                }
            }
        )
    }

    //pop up a dialog for user to add item to the inventory
    if (showDialog.value) {
        val context = LocalContext.current
        AlertDialog(
            onDismissRequest = { showDialog.value = false },

            title = { Text("Add Item to inventory",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray)},

            text = {
                Column(modifier = Modifier.padding(horizontal = 10.dp)) {
                    if(capturedImage.value == null) {
                        Image(
                            painter = painterResource(id = com.example.farmconnect.R.drawable.no_item_image), // Replace "your_image" with the actual image resource ID
                            contentDescription = null,
                            modifier = Modifier
                                .size(200.dp)
                                .padding(20.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        capturedImage.value?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(200.dp)
                                    .padding(20.dp),
                            )
                        }
                    }
                    if(capturedImage.value == null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                                .padding(horizontal = 0.dp)
                        ) {
                            FloatingActionButton(
                                onClick = {
                                    takePicture(activity, context, cameraLauncher)
                                },
                                content = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .padding(horizontal = 10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "",
                                            modifier = Modifier.padding(end = 4.dp)
                                        )
                                        Text("Take Picture For The New Item")
                                    }
                                }
                            )
                        }
                    } else {
                        FloatingActionButton(
                            onClick = {
                                takePicture(activity, context, cameraLauncher)
                            },
                            content = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .padding(horizontal = 10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "",
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                    Text("Re-take Picture For The Item")
                                }
                            }
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
                    message.value = checkMissingField(itemName.value, itemQuantity.value, itemPrice.value, capturedImage.value)

                    //check if there is any field missing
                    if(message.value == "Fields missing valid entries: ") {
                        // Upload the captured image to Firestore Storage
                        capturedImage.value?.let { imageBitmap ->
                            viewModel.isNetworkAvailable()
                            Inventory_Items.addItem(itemName.value, itemPrice.value.toDouble(), itemQuantity.value.toInt(), capturedImage.value as Bitmap)
                            Inventory_Items.update_item_list.add(Inventory_Item(itemName.value, itemPrice.value.toDouble(), itemQuantity.value.toInt(), capturedImage.value as Bitmap))
                            CoroutineScope(Dispatchers.Main).launch {
                                viewModel.loadItems()
                            }
                            CoroutineScope(Dispatchers.Main).launch {
                                //viewModel.isLoading.emit(true)
                                delay(5000)
                                //viewModel.isLoading.emit(false)
                                viewModel.loadItems()
                            }
                        }
                        // Close the dialog
                        showDialog.value = false
                    } else {
                        fieldMissingDialog.value = true
                    }
                }) {
                    Text("OK")
                }
            }
        )
    }

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .padding(bottom = 16.dp) // Add any desired padding
    ) {
        FloatingActionButton(
            onClick = {
                capturedImage.value = null
                itemName.value = ""
                itemQuantity.value = ""
                itemPrice.value = ""
                showDialog.value = true
            },
            content = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Item",
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text("Add Item")
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter) // Align the FloatingActionButton within the Box
        )
    }
}

fun checkMissingField(itemName:String, itemQuantity:String, itemPrice:String, capturedImage:Bitmap?): String{
    var msg = "Fields missing valid entries: "
    if(itemName == "" ){
        msg += " name, "
    }
    if(itemQuantity == "") {
        msg += " quantity, "
    }
    if(itemPrice == "") {
        msg += " price, "
    }
    if(capturedImage == null) {
        msg += " picture, "
    }
    return msg
}


@OptIn(ExperimentalPermissionsApi::class, ExperimentalGlideComposeApi::class)
fun takePicture(activity: Activity, context: Context, cameraLauncher: ActivityResultLauncher<Intent>) {
    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    cameraLauncher.launch(cameraIntent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(){
    cur_context = LocalContext.current
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

            Log.d("TAGitemsize,", theFoodItems.size.toString());
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 128.dp)
            ) {
                items(viewModel.items.value.size) { item ->
                    ItemCard(
                        item = viewModel.items.value.get(item),
                        viewModel,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
    ExtendedFABComponent(viewModel)
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