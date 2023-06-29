package com.example.farmconnect.view

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmconnect.R
import com.example.farmconnect.ui.theme.FarmConnectTheme
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider.getCredential


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
                                    startActivity(Intent(this, HomePage::class.java))
                                    finish()
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
                .width(270.dp)
                .height(60.dp)
                .shadow(5.dp, RoundedCornerShape(4.dp)),
            shape = RoundedCornerShape(1.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
        ) {
            Row(modifier = Modifier.padding(8.dp)) {
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
