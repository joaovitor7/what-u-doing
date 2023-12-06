package com.discordtime.whatudoing

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.discordtime.whatudoing.signin.GoogleAuthManager
import com.discordtime.whatudoing.signin.SingInViewModel
import com.discordtime.whatudoing.signin.ui.SignInScreen
import com.discordtime.whatudoing.ui.theme.WhatudoingTheme
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    private val googleAuthManager by lazy {
        GoogleAuthManager(applicationContext)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WhatudoingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF00D7AD)
                ) {
                    val navController =  rememberNavController()
                    NavHost(navController = navController, startDestination = "sign_in") {
                        composable("sign_in") {
                            val viewModel =  viewModel<SingInViewModel>()
                            val state by viewModel.state.collectAsStateWithLifecycle()

                            //check if the user has already connected or not
                            LaunchedEffect(key1 = Unit) {
                                googleAuthManager.getSignedUser()?.let {
                                    navController.navigate("main_screen")
                                }
                            }

                            LaunchedEffect(key1 = state.isSingInSuccessful) {
                                if(state.isSingInSuccessful) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Sign in successful",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    navController.navigate("main_screen")
                                    viewModel.resetState()
                                }
                            }

                            val launcher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.StartIntentSenderForResult(),
                                onResult = { result ->
                                    if (result.resultCode == RESULT_OK) {
                                        lifecycleScope.launch {
                                            val signInResult = googleAuthManager.signInIntentSender(
                                                intent = result.data ?: return@launch
                                            )
                                            viewModel.onSignInResult(signInResult)
                                        }
                                    }
                                }
                            )

                            SignInScreen(
                                state = state,
                                onSignInClick = {
                                    lifecycleScope.launch {
                                        val signInIntentSender = googleAuthManager.signInIntentSender()
                                        launcher.launch(
                                            IntentSenderRequest.Builder(
                                                signInIntentSender ?: return@launch
                                            ).build()
                                        )
                                    }
                                }
                            )
                        }
                        composable("main_screen") {
                            Column(
                                Modifier.padding(26.dp, 36.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Logo()
                                Button(onClick = {
                                        lifecycleScope.launch {
                                            googleAuthManager.signOut()
                                            navController.popBackStack()
                                        }
                                   }) {
                                    Text("Sing Out")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WhatudoingTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF00D7AD)
        ) {
            Column(
                Modifier.padding(26.dp, 36.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Logo()
            }
        }
    }
}

@Composable
fun Logo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.logo),
        contentDescription = "Logo",
        modifier = modifier
    )
}