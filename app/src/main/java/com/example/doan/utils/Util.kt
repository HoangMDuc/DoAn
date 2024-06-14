package com.example.doan.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.example.doan.database.FileProjections
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Locale

fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}
fun bitmapToString(bitmap: Bitmap): String {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    val byteArray = stream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}

fun stringToBitmap(encodedString: String): Bitmap {
    val decodedBytes = Base64.decode(encodedString, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
}


fun getRootLockedFile(context: Context, type: String?) : File {

    return context.getExternalFilesDirs(type)[0]
}

fun getLockedFileRootPath(context: Context, type: String?) : String {
    return context.getExternalFilesDirs(type)[0].absolutePath
}

fun getFileInfo(file: FileProjections) : String {
    var i = 0
    var size : Double = file.size.toDouble()
    while(size >= 1000) {
        size /= 1000
        i++
    }
    val sizeFormat = String.format(Locale.US,"%.2f", size)
    val info = "${file.date}, $sizeFormat ${storageUnits[i]}, ${file.type.uppercase(Locale.ENGLISH)}"
    return info
}

fun getFileInfo(file: File) : String {
    var i = 0
    var size : Double = file.length().toDouble()
    while(size >= 1000) {
        size /= 1000
        i++
    }
    val sizeFormat = String.format(Locale.US,"%.2f", size)
    val info = "${formatter.format(file.lastModified())}, $sizeFormat ${storageUnits[i]}, ${file.extension.uppercase(Locale.ENGLISH)}"
    return info
}