package com.example.doan.repository

import android.app.Application
import android.util.Log
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
        val aliases = keyStore.aliases()
        while (aliases.hasMoreElements()) {
            val ali = aliases.nextElement()
            Log.d("A", "Alias: $ali")
        }
        return String(key.encoded, Charsets.UTF_8)
    }

    fun getAllKeys() {
        val keys = keyStore.aliases()
        Log.d("keys: ", "Show")
        while(keys.hasMoreElements()) {
            val key = keys.nextElement()
            Log.d("keys: ",key)
        }
    }

    fun genCryptoKey() : String{
        val binaryKey = genKey()
        val hexKey = convertBinaryStringToHexString(binaryKey)

        return hexKey
    }


    fun genCryptoIV() : String{
        return "2390172847198247190"
    }


    companion object {
        init {
            System.loadLibrary("doan")
        }
    }

    private external fun genKey() : String
    private external fun genIV() : String

    public external fun test1(input : String)

    fun convertBinaryStringToHexString(binaryString: String): String {
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