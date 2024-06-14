package com.example.doan.crypto

import kotlin.random.Random

class Crypto {
    private fun insertByte(originalArray: ByteArray, byteToInsert: Byte, index: Int): ByteArray {
        val newArray = ByteArray(originalArray.size + 1)
        System.arraycopy(originalArray, 0, newArray, 0, index)
        newArray[index] = byteToInsert
        System.arraycopy(originalArray, index, newArray, index + 1, originalArray.size - index)
        return newArray
    }
     fun addRandomBytes(originalArray: ByteArray, n: Int): Pair<String, ByteArray> {
        val random = Random.Default
        var result = ""
        var newArray = ByteArray(originalArray.size)
        System.arraycopy(originalArray, 0, newArray, 0, originalArray.size)

        repeat(n) {
            val randomBytes = random.nextBytes(1)
            val randomPosition = random.nextInt(0, originalArray.size - 1)
            result += "${randomBytes[0]}:$randomPosition;"
            newArray = insertByte(newArray, randomBytes[0], randomPosition)
        }

        return  Pair(result.substring(0, result.length-1), newArray)
    }

    fun restoreOriginalArray(modifiedArray: ByteArray, encodedInfo: String): ByteArray {
        val insertionInfoList = encodedInfo.split(";")
        println(insertionInfoList)
        var restoredArray = ByteArray(modifiedArray.size)
        System.arraycopy(modifiedArray,0,restoredArray,0,modifiedArray.size)
        var newArray = ByteArray(modifiedArray.size)
        for (i in insertionInfoList.size -1  downTo 0) {
            val position = insertionInfoList[i].split(":")[1].toInt()

            newArray = ByteArray(restoredArray.size -1 )
            System.arraycopy(restoredArray,0, newArray, 0, position )
            if(position + 1 < restoredArray.size) {
                System.arraycopy(restoredArray, position+1, newArray, position, restoredArray.size - position - 1)
            }

            restoredArray = newArray
        }

        return restoredArray
    }

    fun encrypt(input: String, key: String, iv: String) : String {

        val result = encryptGCM(input, input.length, key, key.length, iv, iv.length)
        return result
    }

    fun decrypt(ciphertext: String, key: String, iv: String) : String {

        return decryptGCM(ciphertext, key, iv)
    }
    companion object {


        init {
            System.loadLibrary("doan")
        }
    }

    private external fun encryptGCM(plaintext: String, plaintextLength: Int, key: String , keySize: Int, iv: String, ivSize: Int) : String
    private external fun decryptGCM(ciphertext: String, key: String ,  iv: String) : String

    //private external fun loadEncrypt(s: ByteArray): ByteArray


}