package com.example.farmconnect.ui.farmer

import android.app.Application
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.util.Log
import android.widget.ImageView
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.farmconnect.R
import com.example.farmconnect.data.Inventory_Item
import com.example.farmconnect.data.Inventory_Items
import com.example.farmconnect.ui.theme.FarmConnectTheme
import com.example.farmconnect.ui.theme.darkGreen
import com.example.farmconnect.view.Screens
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
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
import java.io.ByteArrayOutputStream
lateinit var cur_context_2: Context

class AddPostingViewModel() : ViewModel() {

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

    val isLoading = MutableStateFlow(false)
    var internet_available = false

    init {
        viewModelScope.launch {
            loadItems()
        }
    }

    fun isInvalidQuantity(quantity: String, maxQuantity: Int): Boolean {
        val numericQuantity = quantity.toIntOrNull()
        return numericQuantity == null || numericQuantity <= 0 || numericQuantity > maxQuantity
    }

    fun isInvalidPrice(price: String): Boolean {
        val numericPrice = price.toDoubleOrNull()
        return numericPrice == null || numericPrice <= 0.0
    }

    fun addMarketplaceItem(
        docId: String,
        name: String,
        price: Double,
        inventoryQuantity: Int,
        quantity: Int,
        imageBitmap: Bitmap,
        viewModel: AddPostingViewModel
    ) {
        viewModelScope.launch {
            addMarketplaceItemToFirestore(docId, name, price, inventoryQuantity, quantity, imageBitmap, viewModel)
        }
    }

    fun isNetworkAvailable() {
        val connectivityManager =
            cur_context_2.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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

    fun updateInventoryQuantity(docId: String, quantityRemaining: Int) {
        viewModelScope.launch {
            isLoading.emit(true) // Start loading
            try {
                Log.d(TAG, "This is the quantity remaining " + quantityRemaining)
                db.collection("inventory")
                    .document(docId)
                    .update("quantity", quantityRemaining)
                    .addOnSuccessListener {
                        // Update the item locally in the _items StateFlow
//                        _items.value = _items.value.map { if (it.documentId == docId) it.copy(quantity = quantityRemaining.toInt()) else it }
                    }
                    .addOnFailureListener { exception ->
                        // Handle failure
                    }
            } catch (exception: Exception) {
                // Handle exception
            } finally {
                isLoading.emit(false)
            }
        }
    }

    suspend fun loadItems() {
        isLoading.emit(true) // Start loading
        isNetworkAvailable()
        try {
            //remove all the item in the local cache
            var item_list = mutableListOf<Inventory_Item>()
            if (internet_available) {
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
                    val docId = document.id
                    val name = docData.getValue("name").toString()
                    val price = docData.getValue("price").toString().toDouble()
                    val quantity = docData.getValue("quantity").toString().toInt()
                    val imageBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    item_list.add(
                        Inventory_Item(
                            name = name,
                            price = price,
                            quantity = quantity,
                            imageBitmap = imageBitmap,
                            documentId = docId
                        )
                    )
                }
            }
            _items.emit(item_list.toList())

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

fun checkMissingField(itemName:String, itemQuantity:String, itemPrice:String): String{
    var msg = "Fields missing valid entries: "
    if(itemName == "" ){
        msg += " name, "
    }
    if(itemQuantity == "") {
        msg += " quantity, "
    }
    if(itemPrice == "") {
        msg += " price"
    }
    return msg
}

fun addMarketplaceItemToFirestore(
    docId: String,
    name:String,
    price:Double,
    inventoryQuantity: Int,
    quantity:Int,
    imageBitmap: Bitmap,
    viewModel: AddPostingViewModel
) {
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
            val marketplaceColRef = db.collection("marketplace");
            val data = hashMapOf(
                "name" to name,
                "quantityRemaining" to quantity,
                "quantitySold" to 0,
                "price" to price,
                "userId" to FirebaseAuth.getInstance().currentUser?.uid.toString(),
                "imageUrl" to imageRef.path.toString()
            )
            marketplaceColRef.add(data).addOnSuccessListener {
                viewModel.updateInventoryQuantity(docId, inventoryQuantity - quantity)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPostingsMarketItemCard(viewModel: AddPostingViewModel, item: Inventory_Item, modifier: Modifier = Modifier) {

    // State to control whether the popup is shown or not
    var showDialog by remember { mutableStateOf(false) }
    var fieldMissingDialog by remember { mutableStateOf(false) }
    // States to hold error
    var postingQuantityError by remember { mutableStateOf(false) }
    var postingPriceError by remember { mutableStateOf(false) }
    // State to hold the user input for the posting name
    var postingName by remember { mutableStateOf("") }
    var postingPrice by remember { mutableStateOf("") }
    var postingQuantity by remember { mutableStateOf("") }
    var message = remember { mutableStateOf("") }

    if (fieldMissingDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Failed To Add Posting!",
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
                    fieldMissingDialog = false
                    message.value = ""
                }) {
                    Text("OK")
                }
            }
        )
    }

    if (postingQuantityError) {
        AlertDialog(
            onDismissRequest = { postingQuantityError = false },
            title = {
                Text(
                    "Invalid Quantity",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
            },
            text = {
                Text(
                    "Quantity should be between 1 and ${item.quantity}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        postingQuantityError = false
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }

    Card(
        modifier = modifier
            .width(350.dp)
            .height(270.dp)
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

            Button(
                onClick = { showDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = darkGreen),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(text = "Add posting")
            }

        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add Posting to Marketplace",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray)},
            confirmButton = {
                Button(
                    onClick = {
                        if (postingQuantity.isNotEmpty()) {
                            if (viewModel.isInvalidQuantity(postingQuantity, item.quantity)) {
                                postingQuantityError = true
                                return@Button
                            }
                        }

                        if (postingPrice.isNotEmpty()) {
                            if (viewModel.isInvalidPrice(postingPrice)) {
                                postingPriceError = true
                                return@Button
                            }
                        }

                        message.value = checkMissingField(postingName, postingQuantity, postingPrice)

                        //check if there is any field missing
                        if(message.value == "Fields missing valid entries: ") {
                            viewModel.addMarketplaceItem(item.documentId!!, postingName, postingPrice.toDouble(), item.quantity, postingQuantity.toInt(), item.imageBitmap, viewModel)
                            showDialog = false
                        } else {
                            fieldMissingDialog = true
                        }
                    }
                ) {
                    Text(text = "Save")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDialog = false }
                ) {
                    Text(text = "Cancel")
                }
            },
            text = {
                Column(modifier = Modifier.padding(horizontal = 10.dp)) {
                    Image(
                        painter = rememberAsyncImagePainter(model = item.imageBitmap),
                        contentDescription = "image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = postingName,
                        onValueChange = { postingName = it },
                        label = { Text("Enter Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = postingQuantity,  // This should be a mutable state of String
                        onValueChange = {
                            if (it.toFloatOrNull() != null || it.isEmpty()) {
                                postingQuantity = it
                            }
                        },
                        label = { Text("Enter quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = postingPrice,  // This should be a mutable state of String
                        onValueChange = {
                            if (it.toFloatOrNull() != null || it.isEmpty()) {
                                postingPrice = it
                            }
                        },
                        label = { Text("Enter price") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPostingsMarketScreen(navController: NavController) {
    Log.d("TAG,", "option 2_2");
    cur_context_2 = LocalContext.current
    val viewModel = viewModel<AddPostingViewModel>()
    val searchText by viewModel.searchText.collectAsState()
    val theFoodItems by viewModel.items.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row {
            TextField(
                value = searchText,
                onValueChange = viewModel::onSearchTextChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = "Search") },
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row {
            Button(
                onClick = { navController.navigate(Screens.Marketplace.name) },
                colors = ButtonDefaults.buttonColors(containerColor = darkGreen),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Text(text = "View Postings")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = { navController.navigate(Screens.EditMarketplace.name) },
                colors = ButtonDefaults.buttonColors(containerColor = darkGreen),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Text(text = "Edit Postings")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 128.dp)
        ) {
            items(theFoodItems.size) { item ->
                AddPostingsMarketItemCard(
                    viewModel = viewModel,
                    item = theFoodItems.get(item),
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(150.dp))
    }

}

@Composable
fun AddPostingsMarketplaceScreen() {
    val navController = rememberNavController()
    FarmConnectTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            AddPostingsMarketScreen(navController)
        }
    }
}