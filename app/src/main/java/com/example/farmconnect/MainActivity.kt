package com.example.farmconnect

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmconnect.ui.theme.FarmConnectTheme
import com.google.firebase.auth.FirebaseAuth


class MainActivity : ComponentActivity() {

    private var mFirebaseAuth = FirebaseAuth.getInstance();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FarmConnectTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAct("Android")
                }
            }
        }
//
//        var isLoggedIn = mFirebaseAuth.currentUser
//        Log.d(TAG, mFirebaseAuth.currentUser?.displayName.toString())
//
//        if (isLoggedIn == null) {
//            // show sign in screen
//            Log.d(TAG, "Sign in screen")
//            startActivity(Intent(this, SignIn::class.java))
//            finish()

        //} else {
            // show homepage screen
            Log.d(TAG, "HomePage screen")
            startActivity(Intent(this, MainActivity2::class.java))
            finish()
        //}

    }


}

@Composable
fun MainAct(name: String, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text = name,
            modifier = Modifier.padding(start = 8.dp),
            fontSize = 20.sp,
            color = Color.Black
        )
    }

}

@Preview(showBackground = true)
@Composable
fun MainActPreview() {
    FarmConnectTheme {
        MainAct("Android")
    }
}

