package com.example.doan.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.example.doan.R

class AddNewDialogFragment(private val listener: AddNewDialogListener) : DialogFragment() {

    interface AddNewDialogListener {
        fun onDialogImageClick(dialogFragment: DialogFragment)
        fun onDialogVideoClick(dialogFragment: DialogFragment)
        fun onDialogAudioClick(dialogFragment: DialogFragment)
        fun onDialogDocumentClick(dialogFragment: DialogFragment)
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)


            val view = requireActivity().layoutInflater.inflate(R.layout.add_new, null)

            val imageButton: Button = view.findViewById(R.id.add_image)
            val videoButton: Button = view.findViewById(R.id.add_video)
            val audioButton: Button = view.findViewById(R.id.add_audio)
            val documentButton: Button = view.findViewById(R.id.add_document)

            imageButton.setOnClickListener {
                listener.onDialogImageClick(this)

            }
            videoButton.setOnClickListener {
                listener.onDialogVideoClick(this)
            }

            audioButton.setOnClickListener {
                listener.onDialogAudioClick(this)
            }
            documentButton.setOnClickListener {
                listener.onDialogDocumentClick(this)
            }

            documentButton.setOnClickListener {
                listener.onDialogDocumentClick(this)
            }

            builder.setView(view)

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }



}