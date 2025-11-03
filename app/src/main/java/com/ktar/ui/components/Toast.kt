package com.ktar.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Toast notification types with corresponding colors and icons.
 */
enum class ToastType(
    val icon: ImageVector,
    val backgroundColor: Color,
    val contentColor: Color
) {
    SUCCESS(
        icon = Icons.Default.CheckCircle,
        backgroundColor = Color(0xFF4CAF50),
        contentColor = Color.White
    ),
    ERROR(
        icon = Icons.Default.Error,
        backgroundColor = Color(0xFFF44336),
        contentColor = Color.White
    ),
    WARNING(
        icon = Icons.Default.Warning,
        backgroundColor = Color(0xFFFF9800),
        contentColor = Color.White
    ),
    INFO(
        icon = Icons.Default.Info,
        backgroundColor = Color(0xFF2196F3),
        contentColor = Color.White
    )
}

/**
 * Toast notification state holder.
 */
class ToastState {
    var message by mutableStateOf<String?>(null)
        private set
    var type by mutableStateOf(ToastType.INFO)
        private set
    var isVisible by mutableStateOf(false)
        private set

    fun show(message: String, type: ToastType = ToastType.INFO, duration: Long = 3000L) {
        this.message = message
        this.type = type
        this.isVisible = true

        // Auto-dismiss after duration
        kotlinx.coroutines.GlobalScope.launch {
            delay(duration)
            hide()
        }
    }

    fun hide() {
        isVisible = false
    }
}

/**
 * Toast notification composable.
 *
 * @param state Toast state holder
 * @param modifier Modifier for the toast container
 */
@Composable
fun Toast(
    state: ToastState,
    modifier: Modifier = Modifier
) {
    if (state.isVisible && state.message != null) {
        Snackbar(
            modifier = modifier.padding(16.dp),
            containerColor = state.type.backgroundColor,
            contentColor = state.type.contentColor,
            action = {
                TextButton(
                    onClick = { state.hide() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = state.type.contentColor
                    )
                ) {
                    Text("OK")
                }
            }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = state.type.icon,
                    contentDescription = when (state.type) {
                        ToastType.SUCCESS -> "Success"
                        ToastType.ERROR -> "Error"
                        ToastType.WARNING -> "Warning"
                        ToastType.INFO -> "Information"
                    },
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = state.message ?: "",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Remember toast state.
 */
@Composable
fun rememberToastState(): ToastState {
    return remember { ToastState() }
}
