package com.example.doan.ui.dialog

import android.app.Activity
import android.app.AlertDialog
import com.example.doan.R

class LoadingDialog(private val activity: Activity) {

    private lateinit var dialog: AlertDialog

    fun show() {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity.layoutInflater
        builder.setView(inflater.inflate(R.layout.loading_layout, null))

        builder.setCancelable(false)
        dialog = builder.create()
        dialog.show()
    }

    fun close() {
        dialog.dismiss()
    }
}