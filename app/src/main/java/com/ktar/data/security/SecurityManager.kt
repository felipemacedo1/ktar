package com.ktar.data.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Manages encryption and decryption of sensitive data using Android Keystore.
 * Uses AES-GCM encryption for secure storage of passwords and private keys.
 */
class SecurityManager {
    
    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "ssh_terminal_master_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
        private const val SEPARATOR = "]|["
    }

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }

    init {
        // Create master key if it doesn't exist
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            createMasterKey()
        }
    }

    /**
     * Creates the master encryption key in Android Keystore.
     */
    private fun createMasterKey() {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )

        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(false)
            .build()

        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }

    /**
     * Gets the master key from Keystore.
     */
    private fun getMasterKey(): SecretKey {
        return keyStore.getKey(KEY_ALIAS, null) as SecretKey
    }

    /**
     * Generates a cryptographically secure IV for AES-GCM.
     * Uses 12 bytes (96 bits) as per NIST SP 800-38D specifications.
     */
    private fun generateSecureIV(): ByteArray {
        val iv = ByteArray(12) // GCM standard IV size: 96 bits
        SecureRandom().nextBytes(iv)
        return iv
    }

    /**
     * Encrypts the given data.
     *
     * @param data Plain text data to encrypt
     * @return Base64 encoded encrypted data with IV
     */
    fun encrypt(data: String): String {
        if (data.isEmpty()) return ""

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val iv = generateSecureIV()
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, getMasterKey(), spec)

        val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))

        // Combine IV and encrypted data
        val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
        val encryptedBase64 = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)

        return "$ivBase64$SEPARATOR$encryptedBase64"
    }

    /**
     * Decrypts the given encrypted data.
     *
     * @param encryptedData Base64 encoded encrypted data with IV
     * @return Decrypted plain text
     */
    fun decrypt(encryptedData: String): String {
        if (encryptedData.isEmpty()) return ""

        try {
            val parts = encryptedData.split(SEPARATOR)
            if (parts.size != 2) {
                throw IllegalArgumentException("Invalid encrypted data format")
            }

            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val encryptedBytes = Base64.decode(parts[1], Base64.NO_WRAP)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, getMasterKey(), spec)

            val decryptedBytes = cipher.doFinal(encryptedBytes)
            return String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            throw SecurityException("Failed to decrypt data", e)
        }
    }

    /**
     * Clears all keys from the Keystore (use with caution).
     */
    fun clearKeys() {
        if (keyStore.containsAlias(KEY_ALIAS)) {
            keyStore.deleteEntry(KEY_ALIAS)
        }
    }
}
