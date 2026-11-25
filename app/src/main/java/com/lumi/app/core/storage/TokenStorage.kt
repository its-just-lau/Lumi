package com.lumi.app.core.storage

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private val Context.sessionDataStore by preferencesDataStore(
    name = "lumi_session"
)

/**
 * Guarda el refresh token cifrado con una clave del Android Keystore (AES-GCM,
 * no exportable). El access token NO se guarda: vive solo en memoria.
 */
class TokenStorage(
    private val context: Context
) {

    private val crypto = TokenCrypto()

    private val refreshTokenKey =
        stringPreferencesKey("refresh_token_encrypted")

    suspend fun readRefreshToken(): String? {
        val stored = context.sessionDataStore.data.first()[refreshTokenKey]
            ?: return null

        val token = crypto.decrypt(stored)

        // Clave invalidada o datos corruptos: no hay sesión recuperable.
        if (token == null) clear()

        return token
    }

    suspend fun saveRefreshToken(token: String) {
        val encrypted = crypto.encrypt(token)

        context.sessionDataStore.edit { preferences ->
            preferences[refreshTokenKey] = encrypted
        }
    }

    suspend fun clear() {
        context.sessionDataStore.edit { preferences ->
            preferences.remove(refreshTokenKey)
        }
    }
}

internal class TokenCrypto {

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }

        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val generator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )

        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )

        return generator.generateKey()
    }

    fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())

        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        return Base64.encodeToString(cipher.iv, Base64.NO_WRAP) +
                SEPARATOR +
                Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }

    fun decrypt(stored: String): String? {
        return try {
            val parts = stored.split(SEPARATOR)
            if (parts.size != 2) return null

            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val encrypted = Base64.decode(parts[1], Base64.NO_WRAP)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                getOrCreateKey(),
                GCMParameterSpec(TAG_LENGTH_BITS, iv)
            )

            String(cipher.doFinal(encrypted), Charsets.UTF_8)
        } catch (exception: Exception) {
            null
        }
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "lumi_refresh_token_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val TAG_LENGTH_BITS = 128
        const val SEPARATOR = ":"
    }
}
