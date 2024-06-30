package com.example.doan.utils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Base64
import android.view.View
import androidx.core.content.ContextCompat
import com.example.doan.R
import com.example.doan.database.FileProjections
import com.example.doan.database.entity.FileEntity
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Locale

fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}

fun encodeByteArray(b: ByteArray): String {
    return java.util.Base64.getEncoder().encodeToString(b)
}

fun decodeString(s: String): ByteArray {
    return java.util.Base64.getDecoder().decode(s)
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


fun getRootLockedFile(context: Context, type: String?): File {

    return context.getExternalFilesDirs(type)[0]
}

fun getLockedFileRootPath(context: Context, type: String?): String {
    return context.getExternalFilesDirs(type)[0].absolutePath
}

fun getFileInfo(file: FileProjections): String {
    var i = 0
    var size: Double = file.size.toDouble()
    while (size >= 1000) {
        size /= 1000
        i++
    }
    val sizeFormat = String.format(Locale.US, "%.2f", size)
    val info =
        "${file.date}, $sizeFormat ${storageUnits[i]}, ${file.type.uppercase(Locale.ENGLISH)}"
    return info
}

fun getFileInfo(file: File): String {
    var i = 0
    var size: Double = file.length().toDouble()
    while (size >= 1000) {
        size /= 1000
        i++
    }
    val sizeFormat = String.format(Locale.US, "%.2f", size)
    val info = "${formatter.format(file.lastModified())}, $sizeFormat ${storageUnits[i]}, ${
        file.extension.uppercase(Locale.ENGLISH)
    }"
    return info
}

fun getFileInfo(file: FileEntity): String {
    var i = 0
    var size: Double = file.size.toDouble()
    while (size >= 1000) {
        size /= 1000
        i++
    }
    val extension = file.name.substringAfterLast(".").uppercase(Locale.ENGLISH)
    val sizeFormat = String.format(Locale.US, "%.2f", size)
    val info = "${file.updateAt}, $sizeFormat ${storageUnits[i]}, $extension"
    return info
}


fun isNetworkAvailable(activity: Activity): Boolean {
    val connectivityManager =
        activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return when {
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
        else -> false
    }
}

fun showErrorSnackBar(view: View, context: Context, message: String) {
    val snackBar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
    snackBar.setBackgroundTint(ContextCompat.getColor(context, R.color.snackbar_fail_background))
    snackBar.show()

}

fun showSuccessSnackBar(view: View, context: Context, message: String) {
    val snackBar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
    snackBar.setBackgroundTint(ContextCompat.getColor(context, R.color.snackbar_success_background))
    snackBar.show()

}
