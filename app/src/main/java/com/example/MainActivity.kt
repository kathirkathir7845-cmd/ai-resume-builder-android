package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screen.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ResumeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            var darkTheme by remember { mutableStateOf(UserThemeState.isDarkTheme) }

            MyApplicationTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val resumeViewModel: ResumeViewModel = viewModel()
                    val sessionChecked by resumeViewModel.sessionChecked.collectAsState()
                    val currentUser by resumeViewModel.currentUser.collectAsState()

                    if (!sessionChecked) {
                        SplashLoadingScreen()
                    } else {
                        val startDest = if (currentUser != null) "dashboard" else "login"
                        NavHost(
                            navController = navController,
                            startDestination = startDest
                        ) {
                            // --- 1. Authentication Login Screen ---
                            composable("login") {
                                LoginScreen(
                                    viewModel = resumeViewModel,
                                    onNavigateToDashboard = {
                                        navController.navigate("dashboard") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                )
                            }

                            // --- 2. Recruiter Manage Dashboards ---
                            composable("dashboard") {
                                DashboardScreen(
                                    viewModel = resumeViewModel,
                                    onNavigateToEditor = { id ->
                                        navController.navigate("editor/$id")
                                    },
                                    onNavigateToPreview = { id ->
                                        navController.navigate("preview/$id")
                                    },
                                    onNavigateToAiWorkbench = { id ->
                                        navController.navigate("ai_workbench/$id")
                                    },
                                    onLogout = {
                                        navController.navigate("login") {
                                            popUpTo("dashboard") { inclusive = true }
                                        }
                                    },
                                    isDarkTheme = darkTheme,
                                    onToggleTheme = {
                                        UserThemeState.isDarkTheme = !UserThemeState.isDarkTheme
                                        darkTheme = UserThemeState.isDarkTheme
                                    }
                                )
                            }

                        // --- 3. Dynamic Form Editor ---
                        composable(
                            route = "editor/{resumeId}",
                            arguments = listOf(navArgument("resumeId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val resumeId = backStackEntry.arguments?.getInt("resumeId") ?: 0
                            EditorScreen(
                                viewModel = resumeViewModel,
                                resumeId = resumeId,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // --- 4. Templates customizers & PDF Exports ---
                        composable(
                            route = "preview/{resumeId}",
                            arguments = listOf(navArgument("resumeId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val resumeId = backStackEntry.arguments?.getInt("resumeId") ?: 0
                            PreviewScreen(
                                viewModel = resumeViewModel,
                                resumeId = resumeId,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // --- 5. Gemini Copilot & ATS Score Workbench ---
                        composable(
                            route = "ai_workbench/{resumeId}",
                            arguments = listOf(navArgument("resumeId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val resumeId = backStackEntry.arguments?.getInt("resumeId") ?: 0
                            AiWorkbenchScreen(
                                viewModel = resumeViewModel,
                                resumeId = resumeId,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun SplashLoadingScreen() {
    val gradientBg = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F172A), // Dark slate
            Color(0xFF1E1E2F), // Midnight blue
            Color(0xFF000000)  // Deep black
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Resume Craft AI",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Loading workspace...",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            CircularProgressIndicator(
                color = Color(0xFF3B82F6),
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

