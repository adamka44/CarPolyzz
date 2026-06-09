package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ui.game.*
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: CarPolyzzViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkBackground)
                ) { innerPadding ->
                    BoxWithScreenNavigation(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun BoxWithScreenNavigation(
    viewModel: CarPolyzzViewModel,
    modifier: Modifier = Modifier
) {
    // Elegant backpress mapping protecting matching racing loops
    BackHandler(enabled = viewModel.screenState != ScreenState.Login) {
        when (viewModel.screenState) {
            ScreenState.Dashboard -> {
                // Return to login screen
                viewModel.signOut()
            }
            ScreenState.Matchmaking -> {
                viewModel.cancelMatchmaking()
            }
            ScreenState.Racing -> {
                viewModel.finishRaceSummary()
            }
            ScreenState.PostRaceSummary -> {
                viewModel.finishRaceSummary()
            }
            else -> {}
        }
    }

    // Dynamic state-driven layout switching
    Box(modifier = modifier.fillMaxSize()) {
        when (val state = viewModel.screenState) {
            is ScreenState.Login -> {
                LoginScreen(
                    onSignIn = { email, name ->
                        viewModel.signInWithGoogle(email = email, displayName = name)
                    }
                )
            }
            is ScreenState.Dashboard -> {
                DashboardScreen(viewModel = viewModel)
            }
            is ScreenState.Matchmaking -> {
                MatchmakingScreen(viewModel = viewModel)
            }
            is ScreenState.Racing -> {
                RacingScreen(viewModel = viewModel)
            }
            is ScreenState.PostRaceSummary -> {
                PostRaceSummaryScreen(viewModel = viewModel)
            }
        }
    }
}
