package com.ktar.ui.screens.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ktar.ssh.SessionManager
import kotlinx.coroutines.launch

/**
 * Terminal screen for executing SSH commands.
 *
 * @param viewModel ViewModel instance created with factory
 * @param sessionId SSH session ID
 * @param onNavigateBack Callback when user navigates back
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(
    viewModel: TerminalViewModel,
    sessionId: String,
    onNavigateBack: () -> Unit,
    onNavigateToSFTP: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var showMenu by remember { mutableStateOf(false) }

    // Initialize session when screen is created
    LaunchedEffect(sessionId) {
        val session = SessionManager.getSession(sessionId)
        if (session != null) {
            viewModel.setSession(session)
        } else {
            viewModel.addErrorMessage("Sessão SSH não encontrada")
        }
    }

    // Auto-scroll to bottom when new lines are added
    LaunchedEffect(uiState.outputLines.size) {
        if (uiState.outputLines.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(uiState.outputLines.size - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("SSH Terminal")
                        if (uiState.hostName.isNotEmpty()) {
                            Text(
                                text = uiState.hostName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.disconnect()
                            onNavigateBack()
                        },
                        modifier = Modifier.semantics {
                            contentDescription = "Voltar e desconectar"
                        }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    // Connection status indicator
                    if (uiState.isConnected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Status: Conectado ao servidor ${uiState.hostName}",
                            tint = Color(0xFF4CAF50), // Green
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .semantics {
                                    contentDescription = "Conexão ativa com ${uiState.hostName}"
                                }
                        )
                    }
                    
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.semantics {
                            contentDescription = "Abrir menu de opções"
                        }
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        // Terminal mode info
                        DropdownMenuItem(
                            text = { 
                                Column {
                                    Text(
                                        text = if (uiState.shellMode) "✓ Shell Persistente" else "Modo Exec",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (uiState.shellMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (uiState.shellMode) "PTY sempre ativo" else "PTY: ${if (uiState.ptyEnabled) "Ativo" else "Inativo"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                if (!uiState.shellMode) {
                                    viewModel.togglePTYMode()
                                }
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (uiState.shellMode || uiState.ptyEnabled) Icons.Default.CheckCircle else Icons.Default.Terminal,
                                    contentDescription = null,
                                    tint = if (uiState.shellMode || uiState.ptyEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            enabled = !uiState.shellMode // Disable toggle in shell mode
                        )
                        
                        Divider()
                        
                        DropdownMenuItem(
                            text = { Text("SFTP Manager") },
                            onClick = {
                                onNavigateToSFTP(sessionId)
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Folder, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Limpar Terminal") },
                            onClick = {
                                viewModel.clearTerminal()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Clear, contentDescription = null)
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // Terminal Status Bar
            TerminalStatusBar(
                shellMode = uiState.shellMode,
                bufferUsage = uiState.bufferUsage,
                maxBuffer = 10000,
                pollInterval = uiState.currentPollInterval,
                connectionTime = uiState.connectionTime
            )
            
            Divider()
            
            // Terminal output
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(uiState.outputLines) { line ->
                    TerminalLineItem(line)
                }

                // Show cursor if executing
                if (uiState.isExecuting) {
                    item {
                        Text(
                            text = "▋",
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            Divider()

            // Command input with history navigation
            TerminalInput(
                command = uiState.currentCommand,
                onCommandChange = { viewModel.updateCommand(it) },
                onExecute = { viewModel.executeCommand() },
                onHistoryUp = { viewModel.navigateHistoryUp() },
                onHistoryDown = { viewModel.navigateHistoryDown() },
                enabled = uiState.isConnected && !uiState.isExecuting,
                prompt = uiState.prompt
            )
        }
    }
}

/**
 * Terminal line item component.
 */
@Composable
private fun TerminalLineItem(line: TerminalLine) {
    val color = when (line.type) {
        TerminalLineType.COMMAND -> MaterialTheme.colorScheme.primary
        TerminalLineType.OUTPUT -> MaterialTheme.colorScheme.onSurface
        TerminalLineType.ERROR -> MaterialTheme.colorScheme.error
        TerminalLineType.SYSTEM -> MaterialTheme.colorScheme.secondary
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = line.text,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 16.sp, // Increased from 14sp for better readability
                color = color
            ),
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    // Accessibility: Announce line type to screen readers
                    contentDescription = when (line.type) {
                        TerminalLineType.COMMAND -> "Command: ${line.text}"
                        TerminalLineType.OUTPUT -> "Output: ${line.text}"
                        TerminalLineType.ERROR -> "Error: ${line.text}"
                        TerminalLineType.SYSTEM -> "System: ${line.text}"
                    }
                }
        )
    }
}

/**
 * Terminal input component with command history navigation.
 */
@Composable
private fun TerminalInput(
    command: String,
    onCommandChange: (String) -> Unit,
    onExecute: () -> Unit,
    onHistoryUp: () -> Unit = {},
    onHistoryDown: () -> Unit = {},
    enabled: Boolean,
    prompt: String
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = prompt,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            BasicTextField(
                value = command,
                onValueChange = onCommandChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                enabled = enabled,
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = { onExecute() }
                ),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (command.isEmpty()) {
                            Text(
                                text = "Digite um comando...",
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )
            
            // Send button
            IconButton(
                onClick = onExecute,
                enabled = enabled && command.isNotEmpty()
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Enviar comando",
                    tint = if (enabled && command.isNotEmpty()) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
        }
        
        // History navigation buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Up arrow - Previous command
            OutlinedButton(
                onClick = onHistoryUp,
                enabled = enabled,
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Comando anterior",
                    modifier = Modifier
                        .size(16.dp)
                        .rotate(90f) // Rotate to point up
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Anterior",
                    style = MaterialTheme.typography.labelSmall
                )
            }
            
            // Down arrow - Next command
            OutlinedButton(
                onClick = onHistoryDown,
                enabled = enabled,
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Próximo comando",
                    modifier = Modifier
                        .size(16.dp)
                        .rotate(270f) // Rotate to point down
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Próximo",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

/**
 * Terminal status bar showing mode, buffer, and performance info.
 */
@Composable
private fun TerminalStatusBar(
    shellMode: Boolean,
    bufferUsage: Int,
    maxBuffer: Int,
    pollInterval: Long,
    connectionTime: Long
) {
    val connectionDuration = remember(connectionTime) {
        if (connectionTime > 0) {
            val durationMs = System.currentTimeMillis() - connectionTime
            val minutes = (durationMs / 1000 / 60).toInt()
            val seconds = ((durationMs / 1000) % 60).toInt()
            "${minutes}m ${seconds}s"
        } else {
            "0s"
        }
    }
    
    val bufferPercentage = if (maxBuffer > 0) (bufferUsage.toFloat() / maxBuffer * 100).toInt() else 0
    val bufferColor = when {
        bufferPercentage < 50 -> Color(0xFF4CAF50) // Green
        bufferPercentage < 80 -> Color(0xFFFFC107) // Amber
        else -> Color(0xFFF44336) // Red
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Mode and status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (shellMode) Icons.Default.CheckCircle else Icons.Default.Terminal,
                    contentDescription = null,
                    tint = if (shellMode) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (shellMode) "Shell Persistente" else "Modo Exec",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (shellMode) androidx.compose.ui.text.font.FontWeight.SemiBold else androidx.compose.ui.text.font.FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Right: Stats
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Buffer usage
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        tint = bufferColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "$bufferUsage/$maxBuffer",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Polling interval (only in shell mode)
                if (shellMode) {
                    Text(
                        text = "⚡${pollInterval}ms",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
