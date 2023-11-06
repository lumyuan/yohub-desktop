package io.lumstudio.yohub.common.utils

import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object AesCbcPkcs5Padding {
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val AES = "AES"
    private const val CHARSET = "UTF-8"

    private val key by lazy {
        ""
    }

    private val ivKey by lazy {
        ""
    }

    @Throws(Exception::class)
    fun encrypt(data: String?, key: String, initVector: String): String {
        val iv = IvParameterSpec(initVector.toByteArray(charset(CHARSET)))
        val skeySpec = SecretKeySpec(key.toByteArray(charset(CHARSET)), AES)

        val cipher = Cipher.getInstance(ALGORITHM).apply {
            init(Cipher.ENCRYPT_MODE, skeySpec, iv)
        }

        val encrypted = cipher.doFinal(data?.toByteArray())
        return Base64.getEncoder().encodeToString(encrypted)
    }

    @Throws(Exception::class)
    fun decrypt(encryptedData: String?, key: String, initVector: String): String {
        val iv = IvParameterSpec(initVector.toByteArray(charset(CHARSET)))
        val skeySpec = SecretKeySpec(key.toByteArray(charset(CHARSET)), AES)

        val cipher = Cipher.getInstance(ALGORITHM).apply {
            init(Cipher.DECRYPT_MODE, skeySpec, iv)
        }

        val original = cipher.doFinal(Base64.getDecoder().decode(encryptedData))

        return String(original)
    }

    @Throws(Exception::class)
    fun encrypt(data: String?): String? =
        try {
            encrypt(data, key, ivKey)
        }catch (e: Exception) {
            e.printStackTrace()
            null
        }

    @Throws(Exception::class)
    fun decrypt(encryptedData: String?): String? =
        try {
            decrypt(encryptedData, key, ivKey)
        }catch (e: Exception){
            e.printStackTrace()
            null
        }
}