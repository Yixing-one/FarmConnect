//package com.example.farmconnect.ui.farmer
//
//import android.Manifest
//import android.util.Log
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.rememberLazyListState
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.runtime.remember
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.runtime.mutableStateListOf
//import androidx.compose.runtime.mutableStateMapOf
//import androidx.compose.runtime.mutableStateOf
//import androidx.annotation.DrawableRes
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.lazy.grid.GridCells
//import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.material3.Button
//import androidx.compose.material3.Card
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Snackbar
//import androidx.compose.material3.Text
//import com.example.farmconnect.R
//import androidx.compose.material3.TextField
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.SideEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import com.example.farmconnect.data.allPosts
//import com.example.farmconnect.ui.charity.Post
//
//import com.example.farmconnect.data.allItems
//import androidx.compose.ui.unit.sp
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.example.farmconnect.SpeechRecognizerContract
//import com.example.farmconnect.data.Item
//import com.example.farmconnect.ui.charity.PostCard
//import com.example.farmconnect.ui.theme.darkGreen
//import com.google.accompanist.permissions.ExperimentalPermissionsApi
//import com.google.accompanist.permissions.isGranted
//import com.google.accompanist.permissions.rememberPermissionState
//import kotlinx.coroutines.delay
//
//
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.ktx.firestore
//import com.google.firebase.ktx.Firebase
//import com.google.firebase.storage.ktx.storage
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.SharingStarted
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.combine
//import kotlinx.coroutines.flow.stateIn
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.tasks.await
//
//
//
////class FarmerPerspectiveDonation : ViewModel(){
////    private val db = Firebase.firestore
////    private val storage = Firebase.storage
////    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()
////
////
////    private val _searchText = MutableStateFlow("")
////    val searchText: StateFlow<String> = _searchText.asStateFlow()
////
////    private val _isSearching = MutableStateFlow(false)
////    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()
////
////    private val _items = MutableStateFlow<List<Post>>(listOf()) //MAYBE
////    val items: StateFlow<List<Post>> = searchText
////        .combine(_items) { text, items ->
////            if (text.isBlank()) {
////                items
////            } else {
////                items.filter {
////                    it.doesMatchSearchQuery(text)
////                }
////            }
////        }
////        .stateIn(
////            scope = viewModelScope,
////            started = SharingStarted.WhileSubscribed(),
////            initialValue = _items.value
////        )
////
////
////    val isLoading = MutableStateFlow(true)
////
////    init {
////        viewModelScope.launch {
////            loadItems()
////        }
////    }
////
////    private suspend fun loadItems() {
////        isLoading.emit(true) // Start loading
////        try {
////            val documents = db.collection("marketplace")
////                .whereEqualTo("userId", currentUserId)
////                .get()
////                .await()
////
////            val marketItems = ArrayList<Post>() //MAYBE
////
////            for (document in documents) {
////                val docData = document.data
////                val storageRef = storage.reference
//////                val imageRef = storageRef.child(docData.getValue("imageUrl").toString())
////
//////                val TEN_MEGABYTE:Long = 1024 * 1024 * 10
//////                val bytes = imageRef.getBytes(TEN_MEGABYTE).await()
//////                val imageBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
////
////                marketItems.add(
////                    Post( //MAYBE
////                        documentId = document.id,
////                        name = docData.getValue("name").toString(),
////                        price = docData.getValue("price").toString().toDouble(),
////                        quantityRemaining = docData.getValue("quantityRemaining").toString().toInt(),
////                        quantitySold = docData.getValue("quantitySold").toString().toInt(),
////                        imageBitmap = imageBitmap
////                    )
////                )
////            }
////
////            _items.emit(marketItems.toList())
////
////        } catch (exception: Exception) {
////            isLoading.emit(false)
////        } finally {
////            isLoading.emit(false)
////        }
////    }
////
////    fun onSearchTextChange(text: String){
////        _searchText.value = text
////    }
////
////
////
////}
//
//
//class PostViewModel : ViewModel() {
//    private val db = Firebase.firestore
//
//    suspend fun addPost(post: Post) {
//        try {
//            db.collection("charityPosts").add(post).await()
//        } catch (e: Exception) {
//        }
//    }
//}
//
//
//val inventoryDatabase = mutableStateMapOf<Int, Int>().apply {
//    allItems.forEachIndexed { index, item ->
//        this[index + 1] = item.quantity
//    }
//}
//
//@OptIn(ExperimentalPermissionsApi::class)
//@Composable
//fun DonationItemCard(item: Item, modifier: Modifier = Modifier){
//    Card(
//        modifier = modifier
//            .width(150.dp)
//            .height(230.dp)
//    ) {
//        Column{
//            Image(
//                painter = painterResource(id = item.imageId),
//                contentDescription = "image",
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(120.dp),
//                contentScale = ContentScale.Crop
//            )
//            Text(
//                text = "$${item.price}",
//                modifier = Modifier.padding(start = 13.dp, end = 10.dp, top = 10.dp, bottom = 7.dp),
//                style = MaterialTheme.typography.titleMedium,
//            )
//            Text(
//                text = "${item.name}",
//                modifier = Modifier.padding(start = 13.dp, end = 10.dp, top = 3.dp, bottom = 3.dp),
//                style = MaterialTheme.typography.titleSmall,
//            )
//            Text(
//                text = "${item.quantity} lb",
//                modifier = Modifier.padding(start = 13.dp, end = 10.dp, top = 0.dp, bottom = 10.dp),
//                style = MaterialTheme.typography.bodySmall,
//            )
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun PostScreen() {
//    val postViewModel: PostViewModel = viewModel()
//    val selectedItems = remember { mutableStateListOf<Pair<Item, Int>>() }
//    val posts = remember { mutableStateListOf<Post>() }
//    var showError by remember { mutableStateOf(false) }
//    var postIdCounter by remember { mutableStateOf(1) }
//
////    val postViewModel: PostViewModel = viewModel()
//
//    LaunchedEffect(showError) {
//        if (showError) {
//            delay(1000) // Delay for 1 second
//            showError = false
//        }
//    }
//
//    val charityNameState = remember { mutableStateOf("") }
//    val charityLocationState = remember { mutableStateOf("") }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize(),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .background(darkGreen)
//                .height(100.dp),
//            contentAlignment = Alignment.Center
//        ) {
//            Text(
//                text = "Create donation post",
//                color = Color.White,
//                fontSize = 24.sp,
//                fontWeight = FontWeight.Bold,
//                textAlign = TextAlign.Center,
//            )
//        }
//
//        Spacer(modifier = Modifier.height(10.dp))
//
//        Text(
//            text = "Select items:",
//            style = MaterialTheme.typography.bodyLarge,
//        )
//
//        val lazyListState = rememberLazyListState()
//        LazyColumn(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(10.dp)
//                .height(210.dp),
//            state = lazyListState
//        ) {
////            items(allItems.size) { index ->
////                val item = allItems[index]
////                val selectedQuantity = remember { mutableStateOf("") }
////
////                Row(
////                    verticalAlignment = Alignment.CenterVertically,
////                    modifier = Modifier.fillMaxWidth()
////                ) {
////                    Card(
////                        onClick = {
////                            val selectedItem = item
////                            if (selectedItems.any { it.first == selectedItem }) {
////                                selectedItems.removeAll { it.first == selectedItem }
////                            } else {
////                                val selectedQuantity = selectedQuantity.value.toIntOrNull() ?: 0
////                                if (selectedQuantity > 0) {
////                                    selectedItems.add(selectedItem to selectedQuantity)
////                                }
////                            }
////                        },
////                        modifier = Modifier
////                            .width(275.dp)
////                            .height(70.dp)
////                    ) {
////                        Row(){
////                            Image(
////                                painter = painterResource(id = item.imageId),
////                                contentDescription = "image",
////                                modifier = Modifier
////                                    .width(100.dp)
////                                    .height(100.dp),
////                                contentScale = ContentScale.Crop
////                            )
////                            Column(){
////                                Text(
////                                    text = "${item.name}",
////                                    modifier = Modifier.padding(start = 5.dp, end = 5.dp, top = 5.dp, bottom = 5.dp),
////                                    style = MaterialTheme.typography.titleMedium,
////                                )
////                                Text(
////                                    text = "${item.quantity} lb",
////                                    modifier = Modifier.padding(start = 5.dp, end = 5.dp, top = 5.dp, bottom = 5.dp),
////                                    style = MaterialTheme.typography.bodyMedium,
////                                )
////                            }
////
////                        }
////                    }
//
//                    TextField(
//                        value = selectedQuantity.value,
//                        onValueChange = { selectedQuantity.value = it },
//                        modifier = Modifier.width(100.dp),
//                        label = { Text(
//                            text = "Amount (lbs)",
//                            style = MaterialTheme.typography.bodySmall)
//                                },
//                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
//                    )
//                }
//            }
//        }
//
//        TextField(
//            value = charityNameState.value,
//            onValueChange = { charityNameState.value = it },
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(8.dp),
//            label = { Text("Charity Name") }
//        )
//
//        TextField(
//            value = charityLocationState.value,
//            onValueChange = { charityLocationState.value = it },
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(8.dp),
//            label = { Text("Charity Location") }
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//
//
////        Button(
////            onClick = {
////                if (selectedItems.isNotEmpty() && charityNameState.value.isNotBlank() && charityLocationState.value.isNotBlank()) {
////                    val newPosts = selectedItems.mapNotNull { (selectedItem, selectedQuantity) ->
////                        val availableQuantity = inventoryDatabase[selectedItem.id] ?: 0
////                        if (selectedQuantity <= availableQuantity) {
////                            selectedItem to selectedQuantity
////                        } else {
////                            null
////                        }
////                    }
////                    if (newPosts.isNotEmpty()) {
////                        newPosts.forEach { (item, quantity) ->
////                            inventoryDatabase[item.id] = inventoryDatabase[item.id]?.minus(quantity) ?: 0
////                        }
////
////                        val post = Post(
////                            postIdCounter++,
////                            charityNameState.value,
////                            charityLocationState.value,
////                            287.4,
////                            newPosts[0].first.name,
////                            newPosts[0].second.toDouble(),
////                            newPosts[0].first.imageId
////                        )
////
////                        allPosts.add(post)
////
////                        selectedItems.clear()
////                        charityNameState.value = ""
////                        charityLocationState.value = ""
////                    } else {
////                        showError = true
////                    }
////                }
////            },
////            enabled = selectedItems.isNotEmpty() && selectedItems.all { it.second > 0 }
////        ) {
////            Text(text = "Add to Post")
////        }
//
//        Button(
//            onClick = {
//                if (selectedItems.isNotEmpty() && charityNameState.value.isNotBlank() && charityLocationState.value.isNotBlank()) {
//                    val newPosts = selectedItems.mapNotNull { (selectedItem, selectedQuantity) ->
//                        val availableQuantity = inventoryDatabase[selectedItem.id] ?: 0
//                        if (selectedQuantity <= availableQuantity) {
//                            selectedItem to selectedQuantity
//                        } else {
//                            null
//                        }
//                    }
//                    if (newPosts.isNotEmpty()) {
//                        newPosts.forEach { (item, quantity) ->
//                            inventoryDatabase[item.id] = inventoryDatabase[item.id]?.minus(quantity) ?: 0
//                        }
//
//                        val post = Post(
//                            postIdCounter++,
//                            charityNameState.value,
//                            charityLocationState.value,
//                            287.4,
//                            newPosts[0].first.name,
//                            newPosts[0].second.toDouble(),
//                            newPosts[0].first.imageId
//                        )
//
//                        postViewModel.viewModelScope.launch {
//                            postViewModel.addPost(post)
//                        }
//
//                        selectedItems.clear()
//                        charityNameState.value = ""
//                        charityLocationState.value = ""
//                    } else {
//                        showError = true
//                    }
//                }
//            },
//            enabled = selectedItems.isNotEmpty() && selectedItems.all { it.second > 0 }
//        ) {
//            Text(text = "Add to Post")
//        }
//
//
//        val postLazyListState = rememberLazyListState()
//
//        LazyColumn(
//            state = postLazyListState
//        ) {
//            items(posts) { post ->
//                Row {
//                    Text(
//                        text = "Post ID: ${post.postId}",
//                        style = MaterialTheme.typography.bodyMedium,
//                        modifier = Modifier.padding(vertical = 4.dp)
//                    )
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Column {
//                            Text(
//                                text = "${post.item_name}: ${post.item_amount}",
//                                style = MaterialTheme.typography.bodyMedium,
//                                modifier = Modifier.padding(vertical = 4.dp)
//                            )
//                    }
//                }
//                Text(
//                    text = "Charity Name: ${post.charity_name}",
//                    style = MaterialTheme.typography.bodyMedium,
//                    modifier = Modifier.padding(vertical = 4.dp)
//                )
//                Text(
//                    text = "Charity Location: ${post.charity_location}",
//                    style = MaterialTheme.typography.bodyMedium,
//                    modifier = Modifier.padding(vertical = 4.dp)
//                )
//            }
//            if (posts.isEmpty()) {
//                item {
//                    Text(
//                        text = "",
//                        style = MaterialTheme.typography.bodyMedium
//                    )
//                }
//            }
//        }
//
//        if (showError) {
//            LaunchedEffect(Unit) {
//                delay(2000)
//                showError = false
//            }
//            Snackbar(
//                modifier = Modifier.padding(top = 16.dp),
//                content = { Text(text = "Quantity limit exceeded", style = MaterialTheme.typography.bodyMedium) },
//                containerColor = Color(250, 142, 142)
//            )
//        }
//        }
//    }
//
//
//@Preview
//@Composable
//fun PreviewPostScreen() {
//    PostScreen()
//}
//
//
////adding to post
////        Button(
////            onClick = {
////                if (selectedItems.isNotEmpty() && charityNameState.value.isNotBlank() && charityLocationState.value.isNotBlank()) {
////                    val newPosts = selectedItems.mapNotNull { (selectedItem, selectedQuantity) ->
////                        selectedItem to selectedQuantity
////                    }
////                    if (newPosts.isNotEmpty()) {
////                        // Deduct quantities from inventoryDatabase
////                        newPosts.forEach { (item, quantity) ->
////                            inventoryDatabase[item.id] = inventoryDatabase[item.id]?.minus(quantity) ?: 0
////                        }
////
////                        val post = Post(
////                            postIdCounter++,
////                            charityNameState.value,
////                            charityLocationState.value,
////                            287.4,
////                            newPosts[0].first.name,
////                            newPosts[0].second.toDouble(),
////                            newPosts[0].first.imageId
////                        )
////
////                        allPosts.add(post)
////
////                        selectedItems.clear()
////                        charityNameState.value = ""
////                        charityLocationState.value = ""
////                    } else {
////                        showError = true
////                    }
////                }
////            },
////            enabled = selectedItems.isNotEmpty() && selectedItems.all { it.second > 0 }
////        ) {
////            Text(text = "Add to Post")
////        }
//
//
////            if (showError) {
////                Snackbar(
////                    modifier = Modifier.padding(top = 16.dp),
////                    content = { Text(text = "Quantity limit exceeded", style = MaterialTheme.typography.bodyMedium) },
////                    containerColor = Color(250, 142, 142)
////                )
////            }