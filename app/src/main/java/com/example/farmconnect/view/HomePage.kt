package com.example.farmconnect.view

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CheckboxDefaults.colors
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.farmconnect.navigation.AppBar
import com.example.farmconnect.navigation.DrawerContent
import com.example.farmconnect.ui.theme.FarmConnectTheme
import androidx.navigation.compose.rememberNavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.example.farmconnect.R
import com.example.farmconnect.SpeechRecognizerContract
import com.example.farmconnect.ui.charity.CharityModeScreen
import com.example.farmconnect.ui.farmer.FarmModeScreen
import com.example.farmconnect.ui.farmer.FinanceStatsScreen
import com.example.farmconnect.ui.farmer.InventoryScreen
import com.example.farmconnect.ui.farmer.MarketplaceScreen
import com.example.farmconnect.ui.SettingsScreen
import com.example.farmconnect.ui.farmer.MainViewModel
import com.example.farmconnect.ui.shopping.CartScreen
import com.example.farmconnect.ui.shopping.CartViewModel
import com.example.farmconnect.ui.shopping.ShoppingCenterScreen
import com.example.farmconnect.ui.theme.lightGreen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import android.app.Activity
import com.example.farmconnect.ui.farmer.AddPostingsMarketScreen
import com.example.farmconnect.ui.farmer.EditMarketScreen
import com.example.farmconnect.ui.farmer.EditMarketplaceScreen
import com.example.farmconnect.ui.farmer.MarketScreen

class HomePage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FarmConnectTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    App()
                }
            }
        }
    }
}

enum class Screens(@StringRes val title: Int) {
    //    Farm mode screens:
    Farm(title = R.string.farm_mode),
    Inventory(title = R.string.inventory),
    Finance(title = R.string.finance),
    Marketplace(title = R.string.marketplace),
    EditMarketplace(title = R.string.edit_marketplace),
    AddPostingMarketplace(title=R.string.add_marketplace),
    Donate(title = R.string.donate),
    //    Charity mode screens:
    Charity(title = R.string.charity),
    //    Ecommerce center screens:
    Shopping(title= R.string.shopping),
    Cart(title = R.string.cart),
    //    Settings screen
    Settings(title= R.string.settings)
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
        if (result.resultCode == RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            showDialog.value = true
            capturedImage.value = imageBitmap
        }
    }
    val activity = LocalContext.current as Activity
    /*
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { result ->
        if (result) {
            // Picture was taken successfully
            // Do something with the picture, e.g., save it or display it
            // Get the captured image file from the result data
            val imageFile = result.data?.data ?: return@rememberLauncherForActivityResult
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            showDialog.value = true
            capturedImage.value = imageBitmap
        }
    }*/


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
        onClick = { takePicture(activity, context, cameraLauncher) },
        content = {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
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


@OptIn(ExperimentalPermissionsApi::class, ExperimentalGlideComposeApi::class)
private fun takePicture(activity: Activity, context: Context, cameraLauncher: ActivityResultLauncher<Intent>) {
    //cameraLauncher.launch(null)
    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    activity.startActivityForResult(cameraIntent, 1)
    /*
    if (cameraIntent.resolveActivity(context.packageManager) != null) {
        cameraLauncher.launch(cameraIntent)
    }*/
}



@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun App() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    // Get the name of the current screen
    val currentScreen = Screens.valueOf(
        backStackEntry?.destination?.route ?: Screens.Farm.name
    )
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val cartViewModel = viewModel<CartViewModel>();

    //ask the permission for camera app
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    SideEffect {
        cameraPermissionState.launchPermissionRequest()
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { result ->
        if (result) {
            Log.d("TAG,", "camera works ");
            // Picture was taken successfully
            // Do something with the picture
        } else {
            // Picture capture was canceled or failed
            // Handle the failure or cancellation
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(drawerState, navController)
        }
    ) {
        Scaffold(
            topBar = {
                val showShoppingCart = currentScreen == Screens.Shopping
                val showCloseIcon = currentScreen == Screens.Cart
                AppBar(onMenuClick = { scope.launch { drawerState.open() } }, showShoppingCart, showCloseIcon, navController)
            },
            floatingActionButton = {
                if (currentScreen == Screens.Farm) {  // Check the current screen
                    ExtendedFABComponent()
                }
            },
            floatingActionButtonPosition = FabPosition.End
        ) {
                paddingValues ->

            Surface(modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 5.dp, vertical = 5.dp)
            ){
                Column() {
                    NavHost(
                        navController = navController,
                        startDestination = Screens.Farm.name

                    ){
//                  Farm mode:
                        composable(route = Screens.Farm.name){
                            FarmModeScreen(navController)
                        }
                        composable(route = Screens.Inventory.name){
                            InventoryScreen()
                        }
                        composable(route = Screens.Finance.name){
                            FinanceStatsScreen()
                        }
                        composable(route = Screens.Marketplace.name){
                            MarketScreen(navController)
                        }
                        composable(route = Screens.EditMarketplace.name){
                            EditMarketScreen(navController)
                        }
                        composable(route = Screens.AddPostingMarketplace.name) {
                            AddPostingsMarketScreen(navController)
                        }
//                  Charity mode:
                        composable(route = Screens.Charity.name){
                            CharityModeScreen()
                        }
//                  E-commerce center:
                        composable(route = Screens.Shopping.name){
                            ShoppingCenterScreen(cartViewModel)
                        }
                        composable(route = Screens.Cart.name){
                            CartScreen(cartViewModel)
                        }
//                    Settings
                        composable(route = Screens.Settings.name){
                            SettingsScreen()
                        }
                    }
                }

            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    FarmConnectTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            App()
        }
    }
}