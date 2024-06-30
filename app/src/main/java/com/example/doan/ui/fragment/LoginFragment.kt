package com.example.doan.ui.fragment

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.doan.R
import com.example.doan.databinding.FragmentLoginBinding
import com.example.doan.network.UserApiService
import com.example.doan.repository.KeysRepository
import com.example.doan.ui.dialog.AuthenticateDialog
import com.example.doan.ui.dialog.LoadingDialog
import com.example.doan.utils.STATUS
import com.example.doan.utils.isNetworkAvailable
import com.example.doan.utils.showErrorSnackBar
import com.example.doan.utils.showSuccessSnackBar
import com.example.doan.viewmodel.UserViewModel
import com.google.android.material.textfield.TextInputLayout


class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding

    private val authenticateDialog: AuthenticateDialog by lazy {
        AuthenticateDialog(getListener())
    }

    private val loadingDialog: LoadingDialog by lazy {
        LoadingDialog(requireActivity())
    }
    private val viewModel: UserViewModel by activityViewModels {
        UserViewModel.UserViewModelFactory(
            activity?.application as Application,
            KeysRepository(requireActivity().application),
            UserApiService()
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        binding.apply {
            userViewModel = viewModel
            lifecycleOwner = viewLifecycleOwner
            loginFragment = this@LoginFragment

            buttonNext.setOnClickListener {
                handleClick()
            }
        }
        viewModel.isLogin.observe(viewLifecycleOwner) {
            if (it) {
                findNavController().navigate(R.id.home_fragment)
            }
        }
        Log.d("LoginFragment", "onViewCreated: ${viewModel.isFirstTimeAccess()}")
        if (viewModel.isFirstTimeAccess()) {
            findNavController().navigate(R.id.register_fragment)
        }
        binding.forgotPassword.setOnClickListener {
            if(isNetworkAvailable(requireActivity())) {
                authenticateDialog.show(requireActivity().supportFragmentManager, "AuthenticateDialog")
            }else {
                showErrorSnackBar(requireView(), requireContext(), "No Internet Connection")
            }
        }
        viewModel.status.observe(viewLifecycleOwner) {
            if (it == STATUS.DOING) {
                loadingDialog.show()
            } else {
                loadingDialog.close()
            }
        }
        binding.secondaryPwInput.doOnTextChanged {
            _, _, _, _ ->
            hideErrorMessage(binding.secondaryPwInputLayout)
        }
        viewModel.isCorrectPassword.observe(viewLifecycleOwner) {
            if (it) {
                authenticateDialog.dismiss()
                showSuccessSnackBar(requireView(), requireContext(), "Reset password will be sent to your recovery email")
                handleForgotPassword()
            } else {
                authenticateDialog.setError("Incorrect Master Password")
            }
        }
        binding.appLogo.setOnClickListener {
            viewModel.test()
        }
    }

    private fun handleForgotPassword() {
        viewModel.forgotPassword()
    }

    private fun handleClick() {
        Log.d("LoginFragment", "handleClick: ${viewModel.password.value}")
        val password = binding.secondaryPwInput.text.toString()
        if (viewModel.validatePassword(password)) {
            viewModel.setPassword(password)
            viewModel.login(password)
            if (viewModel.isLogin.value == false) {
                showErrorMessage("Incorrect Password", binding.secondaryPwInputLayout)
            }
        } else {
            showErrorMessage("Invalid Password!", binding.secondaryPwInputLayout)
        }
    }

    private fun showErrorMessage(errorMessage: String, inputLayout: TextInputLayout) {
        inputLayout.error = errorMessage
        inputLayout.isErrorEnabled = true
    }

    private fun hideErrorMessage(inputLayout: TextInputLayout) {
        inputLayout.error = ""
        inputLayout.isErrorEnabled = false
    }

    private fun getListener(): AuthenticateDialog.AuthenticateDialogListener {
        return object : AuthenticateDialog.AuthenticateDialogListener {
            override fun onSubmit(password: String, input: EditText) {

                if (password.isNotEmpty()) {

//                    if(userViewModel.validateEmail(email)) {
//                        userViewModel.updateEmail(email)
//                    }else {
//                        input.error = "Invalid email"
//                    }
                    viewModel.isCorrectMasterPassword(password)
                }

            }

            override fun onCancel(dialog: AuthenticateDialog) {
                dialog.dismiss()
            }
        }
    }

}