package com.example.farmconnect.ui.charity

import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.farmconnect.R
import com.example.farmconnect.ui.theme.FarmConnectTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.farmconnect.data.Inventory_Item
import com.example.farmconnect.data.Inventory_Items
import com.example.farmconnect.ui.farmer.MarketPlaceItem
//import com.example.farmconnect.data.allPosts
import com.example.farmconnect.ui.theme.darkGreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Post(
    val charity_name: String,
    val charity_location: String,
    val charity_distance: Double,
    val item_name: String,
    val item_amount: Double,
    val imageBitmap: Bitmap,
    var isClaimed: Boolean = false
){
    fun doesMatchSearchQuery(query: String): Boolean {
        val matchingCombinations = listOf(
            "$charity_name", "$charity_location", "$item_name",
        )
        return matchingCombinations.any {
            it.contains(query, ignoreCase = true)
        }
    }
}


class FarmViewModel: ViewModel() {
    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

//    private val _posts = MutableStateFlow(allPosts)
    val _posts = MutableStateFlow<List<Post>>(listOf())
    val posts: StateFlow<List<Post>> = searchText
        .combine(_posts) { text, items ->
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
            initialValue = _posts.value
        )
    val isLoading = MutableStateFlow(true)
    init {
        Log.d(TAG, "init")
        viewModelScope.launch {
            Log.d(TAG, "launch")
            loadItems()
        }
    }

    suspend fun loadItems() {
        isLoading.emit(true) // Start loading
        try {
            //remove all the item in the local cache
//            Post.item_list = mutableListOf<Inventory_Item>()
            Log.d(TAG, "load items func")

            val documents = db.collection("charityPosts")
//                .whereEqualTo("userId", currentUserId)
                .get()
                .await()

            Log.d(TAG, "documents: " + documents.toString())


            val charityItems = ArrayList<Post>()
            Log.d(TAG, "charityItems: " + charityItems)


            //load all the items from database to local cache
            for (document in documents) {
                Log.d(TAG, "entered loop document: ")

                val docData = document.data
                Log.d(TAG, "docData: " + docData.toString())

                val storageRef = storage.reference
                val imageRef = storageRef.child(docData.getValue("imageId").toString())

                val TEN_MEGABYTE:Long = 1024 * 1024 * 10
                val bytes = imageRef.getBytes(TEN_MEGABYTE).await()
                val imageBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                charityItems.add(
                    Post(
//                        documentId = document.id,
                        charity_name = docData.getValue("charity_name").toString(),
                        charity_location = docData.getValue("charity_location").toString(),
                        charity_distance = docData.getValue("charity_distance").toString().toDouble(),
                        item_name = docData.getValue("item_name").toString(),
                        item_amount = docData.getValue("item_amount").toString().toDouble(),
                        imageBitmap = imageBitmap
                    )
                )
            }
            Log.d(TAG, "exit loop")

            _posts.emit(charityItems.toList())
//            _posts.emit(Inventory_Items.item_list.toList())

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
@Composable
//reference from code: https://github.com/Spikeysanju/Wiggles/blob/main/app/src/main/java/dev/spikeysanju/wiggles/component/ItemDogCard.kt
fun PostCard(post: Post, modifier: Modifier = Modifier){
    Card(
        modifier = Modifier
            .width(410.dp)
            .height(150.dp)
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = {})
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = post.imageBitmap),
                contentDescription = "image",
                modifier = Modifier
//                    .fillMaxWidth()
                    .width(120.dp)
                    .height(120.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                Text(
                    text = "${post.item_name}  ${post.item_amount} kg",
                    modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 0.dp),
                    style = TextStyle(
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${post.charity_name}",
                    modifier = Modifier.padding(0.dp, 0.dp, 12.dp, 0.dp),
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${post.charity_location}",
                    modifier = Modifier.padding(0.dp, 0.dp, 19.dp, 0.dp),
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Row(verticalAlignment = Alignment.Bottom) {

                    val location: Painter = painterResource(id = R.drawable.ic_location)

                    Icon(
                        painter = location,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp, 16.dp),
                        tint = Color.Red
                    )

                    Text(
                        text = "${post.charity_distance} km",
                        modifier = Modifier.padding(8.dp, 3.dp, 5.dp, 0.dp),
                        style = TextStyle(
                            fontSize = 15.sp,
//                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Button(
                        onClick = {
                            post.isClaimed = true
                        },
                        enabled = !post.isClaimed
                    ) {
                        Text(text = if (post.isClaimed) "Claimed" else "Claim")
                    }
                }

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharityModeScreen(){
    val viewModel = viewModel<FarmViewModel>()
    val charityPosts by viewModel.posts.collectAsState()
    val searchText by viewModel.searchText.collectAsState()
    Log.d(TAG, "charityMODeScreen")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(darkGreen)
                .height(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Charity Mode",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Log.d(TAG, "charity mode label")

        }

        Spacer(modifier = Modifier.height(10.dp))

        Row{
            TextField(
                value = searchText,
                onValueChange = viewModel::onSearchTextChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {Text(text = "Search")},
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 300.dp)){
            items(charityPosts.size){item ->
                PostCard(
                    post = charityPosts.get(item),
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Image(
            painter = painterResource(id = R.drawable.plus_sign),
            contentDescription = "image",
            modifier = Modifier
                .padding(350.dp, 20.dp)
                .height(80.dp)
                .width(85.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CharityModeScreenPreview() {
    FarmConnectTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            CharityModeScreen()
        }
    }
}