package com.example.medijourney.common.managers.bio_metric

import android.content.Context
import javax.crypto.Cipher

fun CryptographyManager(): CryptographyManager = CryptographyManagerImpl()
data class CiphertextWrapper(val ciphertext: ByteArray, val initializationVector: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CiphertextWrapper

        if (!ciphertext.contentEquals(other.ciphertext)) return false
        if (!initializationVector.contentEquals(other.initializationVector)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = ciphertext.contentHashCode()
        result = 31 * result + initializationVector.contentHashCode()
        return result
    }
}

interface CryptographyManager {
    fun getInitializedCipherForEncryption(keyName: String): Cipher
    fun getInitializedCipherForDecryption(keyName: String, initializationVector: ByteArray): Cipher
    fun encryptData(plaintext: String, cipher: Cipher): CiphertextWrapper
    fun decryptData(ciphertext: ByteArray, cipher: Cipher): String
    fun removeData(keyName: String)
    fun checkKey(keyName: String): Boolean
    fun persistCiphertextWrapperToSharedPrefs(
        ciphertextWrapper: CiphertextWrapper,
        context: Context,
        filename: String,
        mode: Int,
        prefKey: String
    )
    fun getCiphertextWrapperFromSharedPrefs(
        context: Context,
        filename: String,
        mode: Int,
        prefKey: String
    ): CiphertextWrapper?
}