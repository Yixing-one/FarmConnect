    //package com.example.farmconnect.data
    package com.example.farmconnect.ui.farmer


    import android.Manifest
    import android.content.ContentValues
    import android.content.ContentValues.TAG
    import android.graphics.Bitmap
    import android.graphics.BitmapFactory
    import android.graphics.Canvas
    import android.graphics.Picture
    import android.graphics.drawable.BitmapDrawable
    import android.os.Build
    import android.util.Log
    import android.widget.ImageView
    import androidx.activity.compose.rememberLauncherForActivityResult
    import androidx.compose.foundation.Image
    import androidx.compose.foundation.layout.*
    import androidx.compose.foundation.lazy.LazyColumn
    import androidx.compose.foundation.lazy.rememberLazyListState
    import androidx.compose.foundation.lazy.LazyColumn
    import androidx.compose.material3.MaterialTheme
    import androidx.compose.runtime.remember
    import androidx.lifecycle.viewmodel.compose.viewModel
    import androidx.compose.ui.text.input.KeyboardType
    import androidx.compose.runtime.mutableStateListOf
    import androidx.compose.runtime.mutableStateMapOf
    import androidx.compose.runtime.mutableStateOf
    import androidx.annotation.DrawableRes
    //import androidx.annotation.RequiresApi
    import androidx.compose.foundation.background
    import androidx.compose.foundation.clickable
    //import androidx.compose.foundation.gestures.ModifierLocalScrollableContainerProvider.value
    import androidx.compose.foundation.lazy.grid.GridCells
    import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
    import androidx.compose.foundation.lazy.items
    import androidx.compose.foundation.text.KeyboardOptions
    import androidx.compose.material3.Button
    import androidx.compose.material3.Card
    import androidx.compose.material3.ExperimentalMaterial3Api
    import androidx.compose.material3.Snackbar
    import androidx.compose.material3.Text
    import com.example.farmconnect.R
    import androidx.compose.material3.TextField
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.LaunchedEffect
    import androidx.compose.runtime.SideEffect
    import androidx.compose.runtime.collectAsState
    import androidx.compose.runtime.getValue
    import androidx.compose.runtime.mutableStateOf
    import androidx.compose.runtime.remember
    import androidx.compose.runtime.setValue
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.layout.ContentScale
    import androidx.compose.ui.res.painterResource
    import androidx.compose.ui.text.TextStyle
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.text.style.TextAlign
    import androidx.compose.ui.tooling.preview.Preview
    import androidx.compose.ui.unit.dp
    //import com.example.farmconnect.data.allPosts
    import com.example.farmconnect.ui.charity.Post
    import com.example.farmconnect.data.postedItems
    import androidx.compose.ui.unit.sp
    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.viewModelScope
    import androidx.lifecycle.viewmodel.compose.viewModel
    import coil.compose.rememberAsyncImagePainter
    import com.example.farmconnect.SpeechRecognizerContract
    import com.example.farmconnect.data.InventoryItemCharity
    import com.example.farmconnect.data.InventoryItemsCharity
    import com.example.farmconnect.data.Inventory_Item
    import com.example.farmconnect.data.Inventory_Items
    import com.example.farmconnect.data.Item
    import com.example.farmconnect.ui.charity.PostCard
    import com.example.farmconnect.ui.theme.darkGreen
    import com.google.accompanist.permissions.ExperimentalPermissionsApi
    import com.google.accompanist.permissions.isGranted
    import com.google.accompanist.permissions.rememberPermissionState
    import kotlinx.coroutines.delay
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.firestore.ktx.firestore
    import com.google.firebase.ktx.Firebase
    import com.google.firebase.storage.ktx.storage
    import kotlinx.coroutines.flow.MutableStateFlow
    import kotlinx.coroutines.flow.SharingStarted
    import kotlinx.coroutines.flow.StateFlow
    import kotlinx.coroutines.flow.asStateFlow
    import kotlinx.coroutines.flow.combine
    import kotlinx.coroutines.flow.count
    import kotlinx.coroutines.flow.stateIn
    import kotlinx.coroutines.launch
    import kotlinx.coroutines.tasks.await
    import java.io.ByteArrayOutputStream


    class PostViewModel : ViewModel() {
        private val db = Firebase.firestore
        private val storage = Firebase.storage
        private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()


        var _charityNameState = MutableStateFlow("")
        val charityNameState: StateFlow<String> = _charityNameState.asStateFlow()

        var _charityLocationState = MutableStateFlow("")
        val charityLocationState: StateFlow<String> = _charityLocationState.asStateFlow()

        suspend fun addPost(post: Post) {
            val storage = Firebase.storage
            val storageRef = storage.reference
            val imagesRef = storageRef.child("images/crops")
            val fileName = "image_${System.currentTimeMillis()}.png"
            val imageRef = imagesRef.child(fileName)
            val baos = ByteArrayOutputStream()
            post.imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
            val imageData = baos.toByteArray()

            val uploadTask = imageRef.putBytes(imageData)
            uploadTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Image uploaded successfully
                    // Create a new doc in the inventory collection
                    val db = Firebase.firestore
                    val inventoryColRef = db.collection("charityPosts");
                    Log.d(TAG, "post: " + post)

                    val data = hashMapOf(
                        "charity_distance" to post.charity_distance,
                        "charity_name" to post.charity_name,
                        "charity_location" to post.charity_location,
                        "item_amount" to post.item_amount,
                        "item_name" to post.item_name,
                        "imageId" to imageRef.path.toString()
//                        "isClaimed" to
//                    "userId" to FirebaseAuth.getInstance().currentUser?.uid.toString(),
                    )
                    Log.d(TAG, "daata: " + data)
                    inventoryColRef.add(data);
                }
            }
            try {

            } catch (e: Exception) {
                Log.d(TAG, e.message.toString())
            }
        }

        private var _searchText = MutableStateFlow("")
        val searchText: StateFlow<String> = _searchText.asStateFlow()

        private var _isSearching = MutableStateFlow(false)
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
            Log.d(TAG, "init farmer perspective")

            viewModelScope.launch {
                Log.d(TAG, "farmer perspective launch")

                loadItems()
            }
        }

        fun onCharityNameTextChange(text: String) {
            _charityNameState.value = text
        }

        fun onCharityLocationTextChange(text: String) {
            _charityLocationState.value = text
        }


        suspend fun loadItems() {
            isLoading.emit(true) // Start loading
            try {
                    //remove all the item in the local cache
    //                Inventory_Items.item_list = mutableListOf<Inventory_Item>()
                Log.d(TAG, "farmer perspective load items")

                Log.d(TAG, "farmer perspective userID: " + currentUserId)
                Log.d(TAG, "farmer persp documents: " + db.collection("inventory").path)
                val documents = db.collection("inventory")
                .whereEqualTo("userId", currentUserId)
                        .get()
                        .await()



                val inventoryItems = ArrayList<Inventory_Item>()

                Log.d(TAG, "farmer persp inventoryItems: " + documents.toString())


                //load all the items from database to local cache
                    for (document in documents) {
                        Log.d(TAG, "farmer persp entered loop document: ")

                        val docData = document.data

                        Log.d(TAG, "farmer persp docData: " + docData.toString())

    //                    val imageUrl = docData.getValue("imageUrl").toString()
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
                        inventoryItems.add(Inventory_Item(name, price, quantity, imageBitmap, docId))
//                        InventoryItemsCharity.addItem(docId, name, price, quantity, imageBitmap)
                    }
//                _items.value = inventoryItems
                _items.emit(inventoryItems.toList())

            } catch (exception: Exception) {
                isLoading.emit(false)
                Log.d(TAG, "error " + exception.message)
            } finally {
                isLoading.emit(false)
            }
        }

        suspend fun deductQuanity(documentID: String, deductedAmount: Int){
            val inventoryDocuments = db.collection("inventory")

            if(documentID != null) {
                val docData = inventoryDocuments.document(documentID).get().await().data;
                var quantity = docData?.getValue("quantity").toString().toInt()
                quantity -= deductedAmount
                inventoryDocuments.document(documentID).update("quantity", quantity).await();
            }
        }
    }

    //PostViewModel
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun PostScreen() {
        val postViewModel: PostViewModel = viewModel<PostViewModel>();
        val selectedItems = remember { mutableStateListOf<Pair<Inventory_Item, Int>>() }
        val posts = remember { mutableStateListOf<Post>() }
        var showError by remember { mutableStateOf(false) }


        val thePostItems by postViewModel.items.collectAsState()
        Log.d(TAG, "the post items: " + thePostItems.size)

        LaunchedEffect(showError) {
            if (showError) {
                delay(1000) // Delay for 1 second
                showError = false
            }
        }

//        val charityNameState = remember { mutableStateOf("") }
//        val charityLocationState = remember { mutableStateOf("") }


        val charityNameState by postViewModel.charityNameState.collectAsState()
        val charityLocationState by postViewModel.charityLocationState.collectAsState()

//        val charityLocationState = remember { mutableStateOf("") }

        Log.d(TAG, "entered PostScreen")

        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(darkGreen)
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Create donation post",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Select items:",
                style = MaterialTheme.typography.bodyLarge,
            )

            val lazyListState = rememberLazyListState()
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .height(210.dp),
                state = lazyListState
            ) {
                items(thePostItems.size) { item ->
                    val inventoryItem = thePostItems.get(item)
                    val selectedQuantity = remember { mutableStateOf("") }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Card(
                            onClick = {
                                val selectedItem = inventoryItem
                                if (selectedItems.any { it.first.documentId == selectedItem.documentId }) {
                                    selectedItems.removeAll { it.first.documentId == selectedItem.documentId }
                                } else {
                                    val selectedQuantity = selectedQuantity.value.toIntOrNull() ?: 0
                                    if (selectedQuantity > 0) {
                                        selectedItems.add(selectedItem to selectedQuantity)
                                    }
                                }
                            },
                            modifier = Modifier
                                .width(275.dp)
                                .height(70.dp)
                        ) {
                            Row(){
                                Image(
                                    painter = rememberAsyncImagePainter(model = inventoryItem.imageBitmap),
                                    contentDescription = "image",
                                    modifier = Modifier
//                                        .fillMaxWidth()
                                        .width(120.dp)
                                        .height(120.dp),
                                    contentScale = ContentScale.Crop
                                )
                                Column(){
                                    Text(
                                        text = "${inventoryItem.name}",
                                        modifier = Modifier.padding(start = 5.dp, end = 5.dp, top = 5.dp, bottom = 5.dp),
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                    Text(
                                        text = "${inventoryItem.quantity} lb",
                                        modifier = Modifier.padding(start = 5.dp, end = 5.dp, top = 5.dp, bottom = 5.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }

                            }
                        }

                        TextField(
                            value = selectedQuantity.value,
                            onValueChange = { selectedQuantity.value = it },
                            modifier = Modifier.width(100.dp),
                            label = { Text(
                                text = "Amount (lbs)",
                                style = MaterialTheme.typography.bodySmall)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
            }
                TextField(
                    value = charityNameState,
//                onValueChange = { charityNameState.value = it },
                    onValueChange = { postViewModel.onCharityNameTextChange(it)},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    label = { Text("Charity Name") }
                )


            TextField(
                value = charityLocationState,
                onValueChange = { postViewModel.onCharityLocationTextChange(it)},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                label = { Text("Charity Location") }
            )

            Spacer(modifier = Modifier.height(16.dp))



            Button(
                onClick = {
                    if (selectedItems.isNotEmpty() && charityNameState.isNotBlank() && charityLocationState.isNotBlank()) {
                        val newPosts = selectedItems.mapNotNull { (selectedItem, selectedQuantity) ->
                            val availableQuantity = thePostItems.find { it.documentId == selectedItem.documentId }?.quantity ?: 0

                            if (selectedQuantity <= availableQuantity) {
                                selectedItem to selectedQuantity
                            } else {
                                null
                            }
                        }
                        if (newPosts.isNotEmpty()) {
                            postViewModel.viewModelScope.launch {
                                newPosts.forEach { (item, quantity) ->
                                    item.documentId?.let { postViewModel.deductQuanity(it, quantity) } // Deduct quantity from the 'inventory' collection database
                                }
                                val post = Post(
                                    charityNameState,
                                    charityLocationState,
                                    287.4,
                                    newPosts[0].first.name,
                                    newPosts[0].second.toDouble(),
                                    newPosts[0].first.imageBitmap
                                )
                                Log.d(TAG, "in loop post" + post)
                                postViewModel.addPost(post)
                            }

                            selectedItems.clear()
//                            postViewModel.onCharityNameTextChange("");
//                            postViewModel.onCharityLocationTextChange("");
                        } else {
                            showError = true
                        }
                    }
                },
                enabled = selectedItems.isNotEmpty() && selectedItems.all { it.second > 0 }
            ) {
                Text(text = "Add to Post")
            }


            val postLazyListState = rememberLazyListState()

            LazyColumn(
                state = postLazyListState
            ) {
                items(posts) { post ->
                    Row {
                        Text(
                            text = "Post ID:",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "${post.item_name}: ${post.item_amount}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                    Text(
                        text = "Charity Name: ${post.charity_name}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Text(
                        text = "Charity Location: ${post.charity_location}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                if (posts.isEmpty()) {
                    item {
                        Text(
                            text = "",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            if (showError) {
                LaunchedEffect(Unit) {
                    delay(2000)
                    showError = false
                }
                Snackbar(
                    modifier = Modifier.padding(top = 16.dp),
                    content = { Text(text = "Quantity limit exceeded", style = MaterialTheme.typography.bodyMedium) },
                    containerColor = Color(250, 142, 142)
                )
            }
        }
    }


    //@RequiresApi(Build.VERSION_CODES.P)
    @Preview
    @Composable
    fun PreviewPostScreen() {
        PostScreen()
    }







    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun DonationItemCard(item: Item, modifier: Modifier = Modifier){
        Card(
            modifier = modifier
                .width(150.dp)
                .height(230.dp)
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
