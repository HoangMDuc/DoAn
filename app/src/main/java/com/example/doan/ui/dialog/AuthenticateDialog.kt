package com.example.doan.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.doan.R

class AuthenticateDialog(private val authenticateDialogListener: AuthenticateDialogListener) : DialogFragment() {
    private lateinit var inputText : EditText
    interface AuthenticateDialogListener {
        fun onSubmit(password: String, input: EditText)
        fun onCancel(dialog: AuthenticateDialog)
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            val view = requireActivity().layoutInflater.inflate(R.layout.authenticate_dialog, null)

            inputText = view.findViewById(R.id.master_password)
            val submitBtn : Button = view.findViewById(R.id.btn_ok)
            val cancelBtn : Button = view.findViewById(R.id.btn_cancel)
            inputText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    submitBtn.isEnabled = s?.length!! > 0
                    inputText.error = null
                }

                override fun afterTextChanged(s: Editable?) {

                }

            })
            builder.setView(view)
            submitBtn.setOnClickListener {
                authenticateDialogListener.onSubmit(inputText.text.toString(), inputText)
            }
            cancelBtn.setOnClickListener {
                authenticateDialogListener.onCancel(this)
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    fun setError(message: String) {
        inputText.error = message
    }
}