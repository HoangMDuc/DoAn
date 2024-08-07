package com.example.doan.repository

import android.app.Application
import androidx.core.content.ContextCompat
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.security.KeyStore
import javax.crypto.spec.SecretKeySpec

class KeysRepository(val application: Application) {

    private var path: String = ContextCompat.getExternalFilesDirs(application.applicationContext, null)[0].absolutePath + "/keystore.jks"
    private var keyStore: KeyStore = KeyStore.getInstance(KeyStore.getDefaultType())


    init {

        val f = File(path)

        if (f.exists()) {

            keyStore.load(Files.newInputStream(Paths.get(path)), null)
        } else {

            keyStore.load(null, null)
        }
    }

    fun storeMKey(alias: String, key: String) {
        val masterKey = SecretKeySpec(key.toByteArray(), "AES")
        val masterSecretEntry = KeyStore.SecretKeyEntry(masterKey)
        keyStore.setEntry(alias, masterSecretEntry, null)
        keyStore.store(Files.newOutputStream(Paths.get(path)), null)

    }

    fun getKey(alias: String) : String {
        val key = keyStore.getKey(alias, null)
        return String(key.encoded, Charsets.UTF_8)
    }

    fun deleteKey(alias: String) {
        if(keyStore.containsAlias(alias)) {
            keyStore.deleteEntry(alias)
        }
    }

    fun hasKey(alias: String) : Boolean {
        return keyStore.containsAlias(alias)
    }


    fun genCryptoKey() : String{
        val binaryKey = genKey()
        val hexKey = convertBinaryStringToHexString(binaryKey)
        return hexKey
    }


    fun genCryptoIV() : String{
        return genIV()
    }

    fun genAddData() : String {
        return genAdditionalData()
    }


    companion object {
        init {
            System.loadLibrary("doan")
        }
    }

    private external fun genKey() : String
    private external fun genIV() : String

    private external fun genAdditionalData() : String

    public external fun test1(input : String)

    private fun convertBinaryStringToHexString(binaryString: String): String {
        val hexString = StringBuilder()
        var i = 0
        while (i < binaryString.length) {
            val byteString = binaryString.substring(i, i + 8)
            val charCode = byteString.toInt(2)
            hexString.append(String.format("%02x", charCode))
            i += 8
        }
        return hexString.toString()
    }







}