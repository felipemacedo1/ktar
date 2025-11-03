package com.ktar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.ktar.data.preferences.UserPreferencesRepository
import com.ktar.ui.ViewModelFactory
import com.ktar.ui.screens.connection.ConnectionScreen
import com.ktar.ui.screens.connection.ConnectionViewModel
import com.ktar.ui.screens.hostlist.HostListScreen
import com.ktar.ui.screens.hostlist.HostListViewModel
import com.ktar.ui.screens.sftp.SFTPScreen
import com.ktar.ui.screens.sftp.SFTPViewModel
import com.ktar.ui.screens.terminal.TerminalScreen
import com.ktar.ui.screens.terminal.TerminalViewModel
import com.ktar.ui.theme.AndroidSSHTerminalTheme

/**
 * Main activity for the Android SSH Terminal app.
 */
class MainActivity : ComponentActivity() {
    
    private val viewModelFactory by lazy { ViewModelFactory(applicationContext) }
    private val preferencesRepository by lazy { UserPreferencesRepository.getInstance(applicationContext) }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            // Collect user preferences for theme
            val userPreferences by preferencesRepository.userPreferencesFlow.collectAsState(
                initial = com.ktar.data.preferences.UserPreferences()
            )
            
            AndroidSSHTerminalTheme(themeMode = userPreferences.themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "host_list"
                    ) {
                        // Host list screen
                        composable("host_list") {
                            val hostListViewModel: HostListViewModel = viewModel(factory = viewModelFactory)
                            HostListScreen(
                                viewModel = hostListViewModel,
                                onNavigateToConnection = { hostId ->
                                    if (hostId != null) {
                                        navController.navigate("connection/$hostId")
                                    } else {
                                        navController.navigate("connection")
                                    }
                                },
                                onNavigateToTerminal = { sessionId ->
                                    navController.navigate("terminal/$sessionId")
                                }
                            )
                        }

                        // Connection screen (new connection)
                        composable("connection") {
                            val connectionViewModel: ConnectionViewModel = viewModel(factory = viewModelFactory)
                            ConnectionScreen(
                                viewModel = connectionViewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onNavigateToTerminal = { sessionId ->
                                    navController.navigate("terminal/$sessionId") {
                                        // Pop connection screen from back stack
                                        popUpTo("host_list")
                                    }
                                }
                            )
                        }

                        // Connection screen (edit existing connection)
                        composable(
                            route = "connection/{hostId}",
                            arguments = listOf(navArgument("hostId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val hostId = backStackEntry.arguments?.getString("hostId")
                            val connectionViewModel: ConnectionViewModel = viewModel(factory = viewModelFactory)
                            ConnectionScreen(
                                viewModel = connectionViewModel,
                                hostId = hostId,
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onNavigateToTerminal = { sessionId ->
                                    navController.navigate("terminal/$sessionId") {
                                        // Pop connection screen from back stack
                                        popUpTo("host_list")
                                    }
                                }
                            )
                        }

                        // Terminal screen
                        composable(
                            route = "terminal/{sessionId}",
                            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
                            val terminalViewModel: TerminalViewModel = viewModel(factory = viewModelFactory)
                            TerminalScreen(
                                viewModel = terminalViewModel,
                                sessionId = sessionId,
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onNavigateToSFTP = { sftpSessionId ->
                                    navController.navigate("sftp/$sftpSessionId")
                                }
                            )
                        }

                        // SFTP screen
                        composable(
                            route = "sftp/{sessionId}",
                            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
                            val sftpViewModel: SFTPViewModel = viewModel(factory = viewModelFactory)
                            SFTPScreen(
                                viewModel = sftpViewModel,
                                sessionId = sessionId,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
