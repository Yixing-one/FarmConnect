package com.example.farmconnect.view

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.widget.RadioGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import com.example.farmconnect.R
import com.example.farmconnect.data.user_role
import com.example.farmconnect.ui.theme.FarmConnectTheme
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider.getCredential
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

var load_home = false
@OptIn(ExperimentalMaterial3Api::class)
@Composable
//add a pop-up dialog getting the user select which mode they want to be in
fun get_user_mode(context: Context, homepage: Class<HomePage>){
    var showDialog = remember { mutableStateOf(true) }
    var role = ""
    var db = Firebase.firestore
    var currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()
    val inventoryColRef = db.collection("userRole");
    var documents: QuerySnapshot
    runBlocking {
        documents = db.collection("userRole")
            .whereEqualTo("userId", currentUserId)
            .get()
            .await()
    }
    if (documents.size() != 0) {
        user_role.value = documents.documents[0].data?.getValue("role").toString()
        context.startActivity(Intent(context, homepage))
        return
    }

    if(showDialog.value) {
        val radioOptions = listOf("Manage our farm production", "Buy organic and affordable farm products", "We are charity looking for food donation")
        val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[0]) }
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text("What you are looking to achieve with FarmConnect?",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray)},
            text = {
                Column(Modifier.selectableGroup()) {
                    radioOptions.forEach { text ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .selectable(
                                    selected = (text == selectedOption),
                                    onClick = { onOptionSelected(text) },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (text == selectedOption),
                                onClick = null // null recommended for accessibility with screenreaders
                            )
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }

            },

            confirmButton = {
                Button(onClick = {
                    if(selectedOption == "Manage our farm production") {
                        role = "FARMER"
                    } else if (selectedOption == "Buy organic and affordable farm products") {
                        role = "BUYER"
                    } else {
                        role = "CHARITY"
                    }
                    user_role.value = role

                    val data = hashMapOf(
                        "role" to role,
                        "userId" to currentUserId
                    )
                    inventoryColRef.add(data)
                    context.startActivity(Intent(context, homepage))
                    showDialog.value = false
                }){Text("OK")}
            }
        )
    }
}

class SignIn : ComponentActivity() {

    private lateinit var oneTapClient: SignInClient
    private lateinit var signUpRequest: BeginSignInRequest

    private val REQ_ONE_TAP = 2  // Can be any integer unique to the Activity
    private var showOneTapUI = true
    private var mFirebaseAuth = FirebaseAuth.getInstance()

    private fun signIn() {
        oneTapClient.beginSignIn(signUpRequest)
            .addOnSuccessListener(this) { result ->
                try {

                    startIntentSenderForResult(
                        result.pendingIntent.intentSender, REQ_ONE_TAP,
                        null, 0, 0, 0)
                } catch (e: IntentSender.SendIntentException) {

                    Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                }
            }
            .addOnFailureListener(this) { e ->
                // No Google Accounts found. Just continue presenting the signed-out UI.
                Log.d(TAG, e.localizedMessage)
            }
    }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        Log.d(TAG, "SignIn activity!")

        setContent {
            FarmConnectTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    SignInScreen("Android", signIn = { signIn() })
                }
            }
        }

        Log.d(TAG, mFirebaseAuth.currentUser?.displayName.toString())

        oneTapClient = Identity.getSignInClient(this)
        Log.d(TAG, "1=========")
        signUpRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(getString(R.string.web_client_id))
                    // Show all accounts on the device.
                    .setFilterByAuthorizedAccounts(false)
                    .build())
            .build()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, requestCode.toString())
        when (requestCode) {

            REQ_ONE_TAP -> {

                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(data)
                    val idToken = credential.googleIdToken
                    when {
                        idToken != null -> {
                            // Got an ID token from Google. Use it to authenticate
                            // with your backend.
                            Log.d(TAG, "Got ID token.")
                            val googleCredentials = getCredential(idToken, null)
                            mFirebaseAuth.signInWithCredential(googleCredentials).addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInWithCredential:success")
                                    setContent {
                                        get_user_mode(this, HomePage::class.java)
                                    }
                                    if(load_home) {
                                        Log.d("TAGsign", "starthome");
                                        startActivity(Intent(this, HomePage::class.java))
                                        finish()
                                    }
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                                }
                            }
                        }

                        else -> {
                            // Shouldn't happen.
                            Log.d(TAG, "No ID token!")
                        }
                    }
                } catch (e: ApiException) {
                    when (e.statusCode) {
                        CommonStatusCodes.CANCELED -> {
                            Log.d(TAG, "One-tap dialog was closed.")
                            // Don't re-prompt the user.
                            showOneTapUI = false
                        }
                        CommonStatusCodes.NETWORK_ERROR -> {
                            Log.d(TAG, "One-tap encountered a network error.")
                            // Try again or just ignore.
                        }
                        else -> {
                            Log.d(TAG, "Couldn't get credential from result." +
                                    " (${e.localizedMessage})")
                        }
                    }

                }
            }
        }
    }

}

@Composable
fun SignInScreen(name: String, modifier: Modifier = Modifier, signIn: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.farmconnect_logo),
            contentDescription = "FarmConnect Logo",
            modifier = Modifier.size(220.dp)
        )
        Button(
            onClick = signIn,
            Modifier
                .width(280.dp)
                .height(60.dp)
                .shadow(5.dp, RoundedCornerShape(4.dp)),
            shape = RoundedCornerShape(1.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
        ) {
            Row(modifier = Modifier.padding(8.dp).width(280.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.google_logo),
                    contentDescription = "Google Logo",
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Sign in with Google",
                    modifier = Modifier.padding(start = 8.dp),
                    fontSize = 20.sp,
                    color = Color.Black
                )
            }

        }
    }
}


@Preview(showBackground = true)
@Composable
fun SignInScreenPreview() {
    FarmConnectTheme {
        SignInScreen("Android", signIn = {})
    }
}

