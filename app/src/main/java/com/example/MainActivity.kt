package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ReelsScreen
import com.example.ui.theme.MyApplicationTheme

sealed class AppScreen {
    object Home : AppScreen()
    data class Reels(val videoId: String? = null) : AppScreen()
    object Favorites : AppScreen()
    object Creator : AppScreen()
}

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        // Fully immersive fullscreen 9:16 for active Reels, hide navigation bar
                        if (currentScreen !is AppScreen.Reels) {
                            BottomNavBar(
                                currentScreen = currentScreen,
                                onNavigate = { currentScreen = it }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (val screen = currentScreen) {
                            is AppScreen.Home -> {
                                HomeScreen(
                                    viewModel = viewModel,
                                    onVideoSelected = { video ->
                                        currentScreen = AppScreen.Reels(video.id)
                                    }
                                )
                            }
                            is AppScreen.Reels -> {
                                ReelsScreen(
                                    viewModel = viewModel,
                                    initialVideoId = screen.videoId,
                                    onBack = {
                                        currentScreen = AppScreen.Home
                                    }
                                )
                            }
                            is AppScreen.Favorites -> {
                                FavoritesScreen(
                                    viewModel = viewModel,
                                    onVideoSelected = { video ->
                                        currentScreen = AppScreen.Reels(video.id)
                                    }
                                )
                            }
                            is AppScreen.Creator -> {
                                AdminScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp, // Flat clean editorial design
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .testTag("bottom_nav_bar")
    ) {
        // Home Navigation Item
        NavigationBarItem(
            selected = currentScreen is AppScreen.Home,
            onClick = { onNavigate(AppScreen.Home) },
            icon = {
                Icon(
                    imageVector = Icons.Default.GridView,
                    contentDescription = "Home",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = "Explore",
                    fontSize = 11.sp,
                    fontWeight = if (currentScreen is AppScreen.Home) FontWeight.Bold else FontWeight.Medium
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        )

        // Reels Navigation Item
        NavigationBarItem(
            selected = currentScreen is AppScreen.Reels,
            onClick = { onNavigate(AppScreen.Reels(null)) },
            icon = {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = "Reels",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "AI Reels",
                    fontSize = 11.sp,
                    fontWeight = if (currentScreen is AppScreen.Reels) FontWeight.Bold else FontWeight.Medium
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        )

        // Favorites Navigation Item
        NavigationBarItem(
            selected = currentScreen is AppScreen.Favorites,
            onClick = { onNavigate(AppScreen.Favorites) },
            icon = {
                Icon(
                    imageVector = Icons.Default.FolderSpecial,
                    contentDescription = "Vault",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = "My Vault",
                    fontSize = 11.sp,
                    fontWeight = if (currentScreen is AppScreen.Favorites) FontWeight.Bold else FontWeight.Medium
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        )

        // Creator Panel Navigation Item
        NavigationBarItem(
            selected = currentScreen is AppScreen.Creator,
            onClick = { onNavigate(AppScreen.Creator) },
            icon = {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = "Creator Studio",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = "Creator",
                    fontSize = 11.sp,
                    fontWeight = if (currentScreen is AppScreen.Creator) FontWeight.Bold else FontWeight.Medium
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        )
    }
}
