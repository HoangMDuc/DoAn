package com.example.doan.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.doan.R
import com.example.doan.databinding.FragmentChangePasswordBinding
import com.example.doan.network.UserApiService
import com.example.doan.repository.KeysRepository
import com.example.doan.ui.dialog.LoadingDialog
import com.example.doan.utils.STATUS
import com.example.doan.viewmodel.UserViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout


class ChangePasswordFragment : Fragment() {

    private lateinit var binding: FragmentChangePasswordBinding
    private val loadingDialog: LoadingDialog by lazy {
        LoadingDialog(requireActivity())
    }

    private val userViewModel: UserViewModel by viewModels {
        UserViewModel.UserViewModelFactory(
            requireActivity().application,
            KeysRepository(requireActivity().application),
            UserApiService()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_change_password, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userViewModel.status.observe(viewLifecycleOwner) {
            if(it == STATUS.DOING) {
                loadingDialog.show()
            }else {
                if(it == STATUS.DONE) {
                    loadingDialog.close()
                    Snackbar.make(view, "Success", Snackbar.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }else {
                    Snackbar.make(view, "Failed", Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        binding.passwordEdt.doOnTextChanged {
            _, _, _, _ ->
            handleTextChanged(binding.passwordLayout)
        }
        binding.passwordConfirmationEdt.doOnTextChanged {
                _, _, _, _ ->
            handleTextChanged(binding.passwordConfirmationLayout)
        }

        binding.saveUpdate.setOnClickListener {
            handleChangePassword()
        }

    }
    private fun handleTextChanged(editText: TextInputLayout) {
        Log.d("TAG", "handleTextChanged: ${binding.passwordEdt.text.toString().isEmpty()} ${binding.passwordConfirmationEdt.text.toString().isEmpty()}")
        binding.saveUpdate.isEnabled = !(binding.passwordEdt.text.toString().isEmpty() || binding.passwordConfirmationEdt.text.toString().isEmpty())

        editText.isErrorEnabled = false
        editText.error = ""
    }

    private fun showError(editText: TextInputLayout, message: String) {
        editText.isErrorEnabled = true
        editText.error = message
    }
    private fun handleChangePassword() {
        val password = binding.passwordEdt.text.toString()
        val confirmPassword = binding.passwordConfirmationEdt.text.toString()
        val oldPassword = userViewModel.getCurrentPassword()
        if(password == oldPassword) {
            showError(binding.passwordLayout, "New password is same with old password")
            return
        }
        if(userViewModel.validatePassword(password)) {
            if(userViewModel.confirmPassword(password, confirmPassword)) {
                userViewModel.updatePassword(password)
            }else {
                showError(binding.passwordConfirmationLayout, "Password not match")
            }
        }else {
            showError(binding.passwordLayout, "Invalid password")
        }
    }

}