package com.ktar.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ktar.data.datastore.HostDataStore
import com.ktar.data.security.SecurityManager
import com.ktar.ssh.SSHManager
import com.ktar.ssh.SessionManager
import com.ktar.ui.screens.connection.ConnectionViewModel
import com.ktar.ui.screens.hostlist.HostListViewModel
import com.ktar.ui.screens.sftp.SFTPViewModel
import com.ktar.ui.screens.terminal.TerminalViewModel

/**
 * Factory for creating ViewModels with dependencies.
 */
class ViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    private val hostDataStore by lazy { HostDataStore(context) }
    private val securityManager by lazy { SecurityManager() }
    private val sshManager by lazy { SSHManager() }
    private val sessionManager = SessionManager

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ConnectionViewModel::class.java) -> {
                ConnectionViewModel(hostDataStore, securityManager, sshManager) as T
            }
            modelClass.isAssignableFrom(HostListViewModel::class.java) -> {
                HostListViewModel(hostDataStore) as T
            }
            modelClass.isAssignableFrom(TerminalViewModel::class.java) -> {
                // TerminalViewModel now extends AndroidViewModel
                TerminalViewModel(context.applicationContext as Application) as T
            }
            modelClass.isAssignableFrom(SFTPViewModel::class.java) -> {
                SFTPViewModel(sessionManager) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
