package com.example.farmconnect.ui.farmer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class EditMarketPlaceViewModel: ViewModel() {
    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _deleteItemConfirmation = MutableStateFlow(null)
    val deleteItemConfirmation: StateFlow<Pair<String, Boolean>?> = _deleteItemConfirmation.asStateFlow()

    // Add a new state flow to track the item to be deleted and its confirmation status
    private val _deleteConfirmation = MutableStateFlow<Pair<String, Boolean>?>(null)
    val deleteConfirmation: StateFlow<Pair<String, Boolean>?> = _deleteConfirmation.asStateFlow()

    // Function to set the delete confirmation state
    fun setDeleteConfirmation(documentId: String, confirmed: Boolean) {
        _deleteConfirmation.value = Pair(documentId, confirmed)
    }

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

            // Launch concurrent coroutines for image downloading
            val jobs = documents.documents.map { document ->
                GlobalScope.launch {
                    val docData = document.data
                    val storageRef = storage.reference
                    val imageRef = storageRef.child(docData?.getValue("imageUrl").toString())
                    val TEN_MEGABYTE: Long = 1024 * 1024 * 10
                    val bytes = imageRef.getBytes(TEN_MEGABYTE).await()
                    val imageBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                    withContext(Dispatchers.Main) {
                        marketItems.add(
                            MarketPlaceItem(
                                documentId = document.id,
                                name = docData?.getValue("name").toString(),
                                price = docData?.getValue("price").toString().toDouble(),
                                quantityRemaining = docData?.getValue("quantityRemaining").toString().toInt(),
                                quantitySold = docData?.getValue("quantitySold").toString().toInt(),
                                imageBitmap = imageBitmap
                            )
                        )
                    }
                }
            }

            // Await all download jobs to finish
            jobs.joinAll()

            _items.emit(marketItems.toList())

        } catch (exception: Exception) {
            isLoading.emit(false)
        } finally {
            isLoading.emit(false)
        }
    }

    fun updateItemPrice(documentId: String, newPrice: Double) {
        viewModelScope.launch {
            isLoading.emit(true) // Start loading
            try {
                db.collection("marketplace")
                    .document(documentId)
                    .update("price", newPrice)
                    .addOnSuccessListener {
                        // Update the item locally in the _items StateFlow
                        _items.value = _items.value.map { if (it.documentId == documentId) it.copy(price = newPrice) else it }
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


    // Function to update the name of a specific item in Firestore
    fun updateItemName(documentId: String, newName: String) {
        viewModelScope.launch {
            isLoading.emit(true) // Start loading
            try {
                db.collection("marketplace")
                    .document(documentId)
                    .update("name", newName)
                    .addOnSuccessListener {
                        // Update the item locally in the _items StateFlow
                        _items.value = _items.value.map { if (it.documentId == documentId) it.copy(name = newName) else it }
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


    // Function to update the quantityRemaining of a specific item in Firestore
    fun updateItemQuantityRemaining(documentId: String, newQuantityRemaining: Int) {
        viewModelScope.launch {
            isLoading.emit(true) // Start loading
            try {
                db.collection("marketplace")
                    .document(documentId)
                    .update("quantityRemaining", newQuantityRemaining)
                    .addOnSuccessListener {
                        // Update the item locally in the _items StateFlow
                        _items.value = _items.value.map { if (it.documentId == documentId) it.copy(quantityRemaining = newQuantityRemaining) else it }
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

    fun deleteItem(documentId: String) {
        val confirmation = deleteConfirmation.value
        if (confirmation?.first == documentId && confirmation.second) {
            viewModelScope.launch {
                isLoading.emit(true) // Start loading
                try {
                    db.collection("marketplace")
                        .document(documentId)
                        .delete()
                        .addOnSuccessListener {
                            // Remove the item locally from the _items StateFlow
                            _items.value = _items.value.filter { it.documentId != documentId }
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
    }

    fun onSearchTextChange(text: String){
        _searchText.value = text
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMarketItemCard(
    viewModel: EditMarketPlaceViewModel,
    item: MarketPlaceItem,
    modifier: Modifier = Modifier
) : Boolean {

    var isSaveButtonClicked by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Card(
            modifier = modifier
                .fillMaxSize()
                .height(390.dp)
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
                var price by remember { mutableStateOf("${item.price}") }
                OutlinedTextField(
                    value = price,
                    onValueChange = { newValue ->
                        price = newValue
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .height(72.dp)
                        .padding(8.dp),
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    label = { Text("Price") },
                )

                // Similar updates for the name and quantityRemaining fields
                var name by remember { mutableStateOf(item.name) }
                OutlinedTextField(
                    value = name,
                    onValueChange = { newValue ->
                        name = newValue
                    },
                    modifier = Modifier
                        .height(72.dp)
                        .padding(8.dp),
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    label = { Text("Name") },
                )

                var quantityRemaining by remember { mutableStateOf(item.quantityRemaining.toString()) }
                OutlinedTextField(
                    value = quantityRemaining,
                    onValueChange = { newValue ->
                        quantityRemaining = newValue
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .height(72.dp)
                        .padding(8.dp),
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    label = { Text("Quantity (lb)") },
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp, start = 8.dp, end = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = {
                            viewModel.updateItemPrice(item.documentId, price.toDouble())
                            viewModel.updateItemName(item.documentId, name)
                            viewModel.updateItemQuantityRemaining(
                                item.documentId,
                                quantityRemaining.toInt()
                            )
                            isSaveButtonClicked = true
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.save),
                            contentDescription = "Save",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            viewModel.setDeleteConfirmation(item.documentId, true)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.delete),
                            contentDescription = "Delete",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

            }
        }
    }

    val deleteConfirmation = viewModel.deleteConfirmation.collectAsState()
    if (deleteConfirmation.value?.first == item.documentId && deleteConfirmation.value?.second == true) {
        AlertDialog(
            onDismissRequest = {
                // Reset the delete confirmation state when the dialog is dismissed
                viewModel.setDeleteConfirmation(item.documentId, false)
            },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete this item?") },
            confirmButton = {
                Button(
                    onClick = {
                        // Confirm item deletion
                        viewModel.deleteItem(item.documentId)
                        // Close the confirmation dialog
                        viewModel.setDeleteConfirmation(item.documentId, false)
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        viewModel.setDeleteConfirmation(item.documentId, false)
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    return isSaveButtonClicked
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMarketScreen(navController: NavController) {
    val viewModel = viewModel<EditMarketPlaceViewModel>()
    val searchText by viewModel.searchText.collectAsState()
    val theFoodItems by viewModel.items.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // State for the Snackbar message
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

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
                    // Capture the value returned by EditMarketItemCard
                    val isSaveButtonClicked = EditMarketItemCard(
                        viewModel = viewModel,
                        item = theFoodItems.get(item),
                        modifier = Modifier.padding(8.dp)
                    )

                    // Show Snackbar if the save button was clicked
                    if (isSaveButtonClicked) {
                        snackbarMessage = "${theFoodItems.get(item).name} was successfully saved!"
                        // Show the Snackbar using SnackbarHostState
                        LaunchedEffect(snackbarMessage) {
                            snackbarHostState.showSnackbar(
                                message = snackbarMessage!!,
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(150.dp))


            // Show the Snackbar using SnackbarHostState
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            ) {

                val messageState = rememberUpdatedState(snackbarMessage)

                Snackbar(
                    action = {
                        TextButton(onClick = { snackbarHostState.currentSnackbarData?.dismiss() }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(messageState.value!!)
                }
            }
        }
    }

}



@Preview
@Composable
fun EditMarketplaceScreen(){
    val navController = rememberNavController()
    FarmConnectTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            EditMarketScreen(navController)
        }
    }
}