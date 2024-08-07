package com.example.doan.crypto

import android.content.Context
import android.util.Log
import com.example.doan.utils.getLockedFileRootPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID
import kotlin.random.Random

class Crypto(
    private val context: Context,
) {
//    suspend fun encryptFile2(
//        filePath: String,
//        fileName: String,
//        fileType: String,
//        folderName: String,
//        key: ByteArray,
//        iv: ByteArray
//    ) : File {
//        return withContext(Dispatchers.IO) {
//            val inputStream = FileInputStream(filePath)
//            val uuid = UUID.randomUUID().toString()
//            val destinationFolder = File(
//                getLockedFileRootPath(context, fileType), folderName
//            )
//            if (!destinationFolder.exists()) {
//                destinationFolder.mkdirs()
//            }
//            val destinationFile = File(
//                destinationFolder.absolutePath,
//                fileName.substringBeforeLast(".") + uuid + "." + fileName.substringAfterLast(".")
//            )
//            val outputStream = FileOutputStream(destinationFile.absolutePath)
//            var length: Int
//            val buffer = ByteArray(1024)
//
//            while (inputStream.read(buffer).also { length = it } > 0) {
//                val encryptedData = encryptGCM1(buffer, "add".toByteArray(), key, iv)
//                outputStream.write(buffer, 0, length)
//
//            }
//            inputStream.close()
//            outputStream.close()
//
//            destinationFile
//        }
//    }
    suspend fun encryptFile(
        filePath: String,
        fileName: String,
        fileType: String,
        folderName: String
    ): Pair<String, File> {
        return withContext(Dispatchers.IO) {
            var encryptedInfo = ""
            val random = Random.Default
            val indexOfLines = mutableListOf<Long>()
            val file = File(filePath)
            val numberOfBlock = file.length() / 1024 + 1
            repeat(1024) {
                indexOfLines.add(random.nextLong(0, numberOfBlock - 2))
            }
            indexOfLines.sort()
            val inputStream = FileInputStream(filePath)
            val uuid = UUID.randomUUID().toString()
            val destinationFolder = File(
                getLockedFileRootPath(context, fileType), folderName
            )
            if (!destinationFolder.exists()) {
                destinationFolder.mkdirs()
            }
            val destinationFile = File(
                destinationFolder.absolutePath,
                fileName.substringBeforeLast(".")+ "_" + uuid + "." + fileName.substringAfterLast(".")
            )
            val outputStream = FileOutputStream(destinationFile.absolutePath)

            val buffer = ByteArray(1024)
            var length: Int
            var addedByte: ByteArray
            var count = 0
            var index = 0
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
                if (index < indexOfLines.size && count.toLong() == indexOfLines[index]) {
                    while (index < indexOfLines.size && count.toLong() == indexOfLines[index]) {
                        addedByte = random.nextBytes(1)
                        outputStream.write(addedByte, 0, 1)
                        encryptedInfo += "$count:${addedByte[0]};"
                        index++
                    }
                }
                count++
            }
            inputStream.close()
            outputStream.close()
            Pair(encryptedInfo.substring(0, encryptedInfo.length - 1), destinationFile)
        }
    }

    suspend fun decryptFile(filePath: String, fileName: String, encryptionInfo: String): File {
        return withContext(Dispatchers.IO) {
            Log.d("encryptionInfo", encryptionInfo)
            val encryptionList = encryptionInfo.split(";")
            val encryptionMap = mutableMapOf<Long, Int>()
            encryptionList.forEach {
                val pair = it.split(":")
                if (encryptionMap.contains(pair[0].toLong())) {
                    encryptionMap[pair[0].toLong()] = encryptionMap[pair[0].toLong()]!! + 1
                } else {
                    encryptionMap[pair[0].toLong()] = 1
                }
            }
            Log.d("encryptionList", encryptionList.joinToString(","))
            val inputStream = FileInputStream(filePath)
            val cacheFile = File(context.externalCacheDir, fileName)
            val outputStream = FileOutputStream(cacheFile.absolutePath)
            val buffer = ByteArray(1024)
            var length: Int
            var count: Long = 0
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
                if (encryptionMap.contains(count)) {
                    inputStream.read(ByteArray(encryptionMap[count]!!))
                }
                count++
            }
            inputStream.close()
            outputStream.close()
            cacheFile
        }
    }

    suspend fun decryptFileToOriginPath(
        filePath: String,
        fileName: String,
        originPath: String,
        encryptionInfo: String
    ): File {
        return withContext(Dispatchers.IO) {
            Log.d("encryptionInfo", encryptionInfo)
            val encryptionList = encryptionInfo.split(";")
            val encryptionMap = mutableMapOf<Long, Int>()
            encryptionList.forEach {
                val pair = it.split(":")
                if (encryptionMap.contains(pair[0].toLong())) {
                    encryptionMap[pair[0].toLong()] = encryptionMap[pair[0].toLong()]!! + 1
                } else {
                    encryptionMap[pair[0].toLong()] = 1
                }
            }
            Log.d("encryptionList", encryptionList.joinToString(","))
            val inputStream = FileInputStream(filePath)
            val originFile = File(originPath)
            val outputStream = FileOutputStream(originFile.absolutePath)
            val buffer = ByteArray(1024)
            var length: Int
            var count: Long = 0
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
                if (encryptionMap.contains(count)) {
                    inputStream.read(ByteArray(encryptionMap[count]!!))
                }
                count++
            }
            inputStream.close()
            outputStream.close()
            originFile
        }
    }

    fun encrypt(input: ByteArray, add: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {

        val result = encryptGCM1(input, add, key, iv)
        return result
    }

    fun decrypt(ciphertext: ByteArray, add: ByteArray, key: ByteArray, iv: ByteArray): ByteArray? {
        return decryptGCM1(ciphertext, add, key, iv)
    }

    companion object {


        init {
            System.loadLibrary("doan")
        }
    }


    private external fun encryptGCM1(
        plaintext: ByteArray,
        aad: ByteArray,
        key: ByteArray,
        iv: ByteArray
    ): ByteArray

    private external fun decryptGCM1(
        ciphertext: ByteArray,
        aad: ByteArray,
        key: ByteArray,
        iv: ByteArray
    ): ByteArray?

}