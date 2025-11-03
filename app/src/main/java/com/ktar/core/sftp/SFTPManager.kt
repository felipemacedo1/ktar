package com.ktar.core.sftp

import android.util.Log
import com.ktar.ssh.SSHSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.schmizz.sshj.sftp.FileAttributes
import net.schmizz.sshj.sftp.FileMode
import net.schmizz.sshj.sftp.RemoteResourceInfo
import net.schmizz.sshj.sftp.SFTPClient
import net.schmizz.sshj.xfer.FileSystemFile
import java.io.File
import java.io.IOException

/**
 * Manages SFTP operations using an existing SSH session.
 * Handles file listing, upload, and download operations with security validation.
 */
class SFTPManager(private val session: SSHSession) {

    private var sftpClient: SFTPClient? = null

    companion object {
        private const val TAG = "SFTP"
        private const val BUFFER_SIZE = 32768 // 32KB buffer for transfers
        
        // File upload security constraints
        private val ALLOWED_EXTENSIONS = setOf(
            "txt", "log", "conf", "config", "sh", "py", "kt", "java",
            "json", "xml", "yaml", "yml", "sql", "csv", "md", "rst",
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "zip", "tar", "gz", "rar", "7z", "sh", "bash"
        )
        private const val MAX_FILE_SIZE = 500 * 1024 * 1024 // 500MB
        
        // Patterns to detect directory traversal and invalid paths
        private val FORBIDDEN_PATTERNS = listOf(
            Regex("\\.\\.[\\\\/]"),  // Directory traversal ../ or ..\
            Regex("^[\\\\/]"),       // Absolute paths starting with / or \
            Regex("[<>:\"|?*\\x00]") // Invalid filename characters
        )
    }

    /**
     * Initializes the SFTP client using the existing SSH session.
     * Must be called before any SFTP operations.
     */
    suspend fun connect(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!session.isConnected()) {
                Log.e(TAG, "SSH session is not connected")
                return@withContext Result.failure(IOException("SSH session is not connected"))
            }

            // Access the internal SSHClient through reflection (safe approach)
            val clientField = session::class.java.getDeclaredField("client")
            clientField.isAccessible = true
            val sshClient = clientField.get(session) as net.schmizz.sshj.SSHClient

            sftpClient = sshClient.newSFTPClient()
            Log.d(TAG, "SFTP client connected successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize SFTP client", e)
            Result.failure(e)
        }
    }

    /**
     * Validates a file path for security issues (directory traversal, etc).
     */
    private fun validateFilePath(path: String): Result<String> {
        // Check for directory traversal and invalid characters
        if (FORBIDDEN_PATTERNS.any { it.containsMatchIn(path) }) {
            return Result.failure(IllegalArgumentException("Invalid path: contains forbidden characters or traversal patterns"))
        }

        // Extract just the filename from the path
        val filename = path.substringAfterLast("/").substringAfterLast("\\")
        
        // Check for hidden files/configs (starts with dot)
        if (filename.startsWith(".")) {
            return Result.failure(IllegalArgumentException("Hidden files are not allowed"))
        }

        // Filename must not be empty
        if (filename.isEmpty()) {
            return Result.failure(IllegalArgumentException("Invalid filename"))
        }

        return Result.success(filename)
    }

    /**
     * Validates file before upload (size, extension, etc).
     */
    private fun validateFileUpload(filename: String, size: Long): Result<Unit> {
        // Check file size
        if (size > MAX_FILE_SIZE) {
            return Result.failure(
                IllegalArgumentException("File size ${size / (1024 * 1024)}MB exceeds limit of ${MAX_FILE_SIZE / (1024 * 1024)}MB")
            )
        }

        // Check file extension against whitelist
        val extension = filename.substringAfterLast(".", "").lowercase()
        if (extension.isEmpty() || extension !in ALLOWED_EXTENSIONS) {
            return Result.failure(
                IllegalArgumentException("File type .$extension is not allowed")
            )
        }

        return Result.success(Unit)
    }

    /**
     * Lists files and directories in the specified remote path.
     * @param remotePath Remote directory path to list
     * @return List of SFTPFile objects
     */
    suspend fun listFiles(remotePath: String): Result<List<SFTPFile>> = withContext(Dispatchers.IO) {
        try {
            val client = sftpClient ?: return@withContext Result.failure(
                IllegalStateException("SFTP client not connected. Call connect() first.")
            )

            Log.d("SFTP_LIST", "Listing files in: $remotePath")
            
            val files = client.ls(remotePath).map { resource ->
                mapToSFTPFile(resource)
            }.sortedWith(compareBy({ !it.isDirectory }, { it.name }))

            Log.d("SFTP_LIST", "Found ${files.size} items in $remotePath")
            Result.success(files)
        } catch (e: Exception) {
            Log.e("SFTP_LIST", "Failed to list files in $remotePath", e)
            Result.failure(e)
        }
    }

    /**
     * Uploads a local file to the remote server with security validation.
     * @param localPath Local file path
     * @param remotePath Remote destination path
     * @param progressListener Optional progress listener
     * @return SFTPResult indicating success or failure
     */
    suspend fun uploadFile(
        localPath: String,
        remotePath: String,
        progressListener: SFTPProgressListener? = null
    ): SFTPResult = withContext(Dispatchers.IO) {
        try {
            val client = sftpClient ?: return@withContext SFTPResult.Error(
                "SFTP client not connected. Call connect() first."
            )

            val localFile = File(localPath)
            if (!localFile.exists()) {
                Log.e("SFTP_UPLOAD", "Local file not found: $localPath")
                return@withContext SFTPResult.Error("Local file not found: $localPath")
            }

            if (!localFile.canRead()) {
                Log.e("SFTP_UPLOAD", "Cannot read local file: $localPath")
                return@withContext SFTPResult.Error("Cannot read local file: $localPath")
            }

            // Validate remote path for directory traversal and injection attacks
            val pathValidation = validateFilePath(remotePath)
            if (pathValidation.isFailure) {
                Log.e("SFTP_UPLOAD", "Invalid remote path: ${pathValidation.exceptionOrNull()?.message}")
                return@withContext SFTPResult.Error("Invalid remote path: ${pathValidation.exceptionOrNull()?.message}")
            }

            // Validate file size and extension
            val uploadValidation = validateFileUpload(localFile.name, localFile.length())
            if (uploadValidation.isFailure) {
                Log.e("SFTP_UPLOAD", "Upload validation failed: ${uploadValidation.exceptionOrNull()?.message}")
                return@withContext SFTPResult.Error("Upload validation failed: ${uploadValidation.exceptionOrNull()?.message}")
            }

            Log.d("SFTP_UPLOAD", "Uploading $localPath to $remotePath (${localFile.length()} bytes)")

            // Use SSHJ's put method
            client.put(FileSystemFile(localFile), remotePath)
            
            Log.d("SFTP_UPLOAD", "Upload completed: $remotePath")
            SFTPResult.Success("File uploaded successfully")
        } catch (e: Exception) {
            Log.e("SFTP_UPLOAD", "Upload failed: $localPath -> $remotePath", e)
            SFTPResult.Error(e.message ?: "Upload failed")
        }
    }

    /**
     * Downloads a remote file to the local device with restricted permissions.
     * @param remotePath Remote file path
     * @param localPath Local destination path
     * @param progressListener Optional progress listener
     * @return SFTPResult indicating success or failure
     */
    suspend fun downloadFile(
        remotePath: String,
        localPath: String,
        progressListener: SFTPProgressListener? = null
    ): SFTPResult = withContext(Dispatchers.IO) {
        try {
            val client = sftpClient ?: return@withContext SFTPResult.Error(
                "SFTP client not connected. Call connect() first."
            )

            // Check if remote file exists
            val attrs = try {
                client.stat(remotePath)
            } catch (e: Exception) {
                Log.e("SFTP_DOWNLOAD", "Remote file not found: $remotePath", e)
                return@withContext SFTPResult.Error("Remote file not found: $remotePath")
            }

            val localFile = File(localPath)
            val localDir = localFile.parentFile
            
            if (localDir != null && !localDir.exists()) {
                if (!localDir.mkdirs()) {
                    Log.e("SFTP_DOWNLOAD", "Failed to create local directory: ${localDir.path}")
                    return@withContext SFTPResult.Error("Failed to create local directory")
                }
            }

            Log.d("SFTP_DOWNLOAD", "Downloading $remotePath to $localPath (${attrs.size} bytes)")

            // Use SSHJ's get method
            client.get(remotePath, FileSystemFile(localFile))
            
            // Set restrictive permissions: owner read/write only (0600)
            // This prevents other apps from reading sensitive downloaded files
            val canSetReadable = localFile.setReadable(true, true)  // owner-only readable
            val canSetWritable = localFile.setWritable(true, true)  // owner-only writable
            
            if (!canSetReadable || !canSetWritable) {
                Log.w("SFTP_DOWNLOAD", "Could not set restrictive permissions on $localPath")
            }
            
            Log.d("SFTP_DOWNLOAD", "Download completed: $localPath")
            SFTPResult.Success("File downloaded successfully to ${localFile.name}")
        } catch (e: Exception) {
            Log.e("SFTP_DOWNLOAD", "Download failed: $remotePath -> $localPath", e)
            SFTPResult.Error(e.message ?: "Download failed")
        }
    }

    /**
     * Closes the SFTP connection.
     */
    suspend fun disconnect() = withContext(Dispatchers.IO) {
        try {
            sftpClient?.close()
            sftpClient = null
            Log.d(TAG, "SFTP client disconnected")
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting SFTP client", e)
        }
    }

    /**
     * Maps SSHJ RemoteResourceInfo to SFTPFile.
     */
    private fun mapToSFTPFile(resource: RemoteResourceInfo): SFTPFile {
        val attrs = resource.attributes
        return SFTPFile(
            name = resource.name,
            path = resource.path,
            size = attrs.size,
            isDirectory = attrs.type == FileMode.Type.DIRECTORY,
            lastModified = attrs.mtime * 1000L, // Convert to milliseconds
            permissions = formatPermissions(attrs)
        )
    }

    /**
     * Formats file permissions for display.
     */
    private fun formatPermissions(attrs: FileAttributes): String {
        return try {
            val permissions = attrs.permissions
            if (permissions != null) {
                permissions.toString()
            } else {
                "---------"
            }
        } catch (e: Exception) {
            "---------"
        }
    }
}
