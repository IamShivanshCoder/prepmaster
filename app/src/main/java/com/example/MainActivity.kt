package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.*
import androidx.activity.compose.BackHandler
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryAccentAmber
import com.example.ui.theme.TextMuted
import androidx.compose.ui.graphics.Color
import com.example.ui.viewmodel.PrepViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: PrepViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppContainer(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(viewModel: PrepViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    var showExitBottomSheet by remember { mutableStateOf(false) }

    // Intercept back actions for in-app stack, show bottom sheet when at root dashboard
    BackHandler(enabled = currentScreen != "login") {
        if (currentScreen == "dashboard") {
            showExitBottomSheet = true
        } else {
            val handled = viewModel.navigateBack()
            if (!handled) {
                viewModel.navigateTo("dashboard")
            }
        }
    }

    // Determine if bottom navigation menu bar should be visible
    // Hide bottom bar on login and distraction-free exam attempt screen, and pdf viewer
    val showBottomBar = currentScreen != "login" && currentScreen != "attempt" && currentScreen != "pdf_viewer"

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color(0xFF0D0D1F),
                    contentColor = PrimaryAccentAmber,
                    tonalElevation = 0.dp,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    NavigationBarItem(
                        selected = currentScreen == "dashboard",
                        onClick = { viewModel.navigateTo("dashboard") },
                        label = { Text("Home", fontSize = 11.sp) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == "dashboard") Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = "Dashboard Screen Button",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryAccentAmber,
                            selectedTextColor = PrimaryAccentAmber,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = Color(0xFF1D1B36)
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == "library",
                        onClick = { viewModel.navigateTo("library") },
                        label = { Text("Papers", fontSize = 11.sp) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == "library") Icons.Filled.MenuBook else Icons.Outlined.MenuBook,
                                contentDescription = "Syllabus Papers library screen button",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryAccentAmber,
                            selectedTextColor = PrimaryAccentAmber,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = Color(0xFF1D1B36)
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == "challenge",
                        onClick = { viewModel.navigateTo("challenge") },
                        label = { Text("Daily", fontSize = 11.sp) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == "challenge") Icons.Filled.Bolt else Icons.Outlined.Bolt,
                                contentDescription = "Daily challenge question",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryAccentAmber,
                            selectedTextColor = PrimaryAccentAmber,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = Color(0xFF1D1B36)
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == "analytics",
                        onClick = { viewModel.navigateTo("analytics") },
                        label = { Text("Analytics", fontSize = 11.sp) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == "analytics") Icons.Filled.BarChart else Icons.Outlined.BarChart,
                                contentDescription = "Prep master stats analytics graphs",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryAccentAmber,
                            selectedTextColor = PrimaryAccentAmber,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = Color(0xFF1D1B36)
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == "settings",
                        onClick = { viewModel.navigateTo("settings") },
                        label = { Text("Profile", fontSize = 11.sp) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == "settings") Icons.Filled.Person else Icons.Outlined.Person,
                                contentDescription = "Settings and student profile",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryAccentAmber,
                            selectedTextColor = PrimaryAccentAmber,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = Color(0xFF1D1B36)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen transition animations
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "NavigationTransition"
            ) { targetScreen ->
                when (targetScreen) {
                    "login" -> LoginScreen(viewModel = viewModel)
                    "dashboard" -> DashboardScreen(viewModel = viewModel)
                    "library" -> PapersLibraryScreen(viewModel = viewModel)
                    "attempt" -> ExamAttemptScreen(viewModel = viewModel)
                    "pdf_viewer" -> PdfViewScreen(viewModel = viewModel)
                    "analytics" -> AnalyticsScreen(viewModel = viewModel)
                    "challenge" -> DailyChallengeScreen(viewModel = viewModel)
                    "settings" -> SettingsScreen(viewModel = viewModel)
                    else -> DashboardScreen(viewModel = viewModel)
                }
            }
        }
    }

    if (showExitBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showExitBottomSheet = false },
            containerColor = Color(0xFF111128), // SurfaceNavy from Color.kt
            contentColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Exit PrepPapers?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Are you sure you want to close the app?",
                    fontSize = 14.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { showExitBottomSheet = false },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = PrimaryAccentAmber
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryAccentAmber),
                        modifier = Modifier.weight(1f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            showExitBottomSheet = false
                            (context as? android.app.Activity)?.finish()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryAccentAmber,
                            contentColor = Color(0xFF0A0A1A)
                        ),
                        modifier = Modifier.weight(1f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    ) {
                        Text("Exit")
                    }
                }
            }
        }
    }
}
