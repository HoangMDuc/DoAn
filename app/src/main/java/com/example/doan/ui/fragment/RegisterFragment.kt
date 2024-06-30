package com.example.doan.ui.fragment

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.doan.R
import com.example.doan.databinding.FragmentRegisterBinding
import com.example.doan.network.UserApiService
import com.example.doan.repository.KeysRepository
import com.example.doan.ui.dialog.LoadingDialog
import com.example.doan.utils.STATUS
import com.example.doan.viewmodel.UserViewModel
import com.google.android.material.textfield.TextInputLayout


class RegisterFragment : Fragment() {
    private lateinit var binding: FragmentRegisterBinding

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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_register, container, false)

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        initView()

    }

    private fun initView() {
        val loadingDialog = LoadingDialog(requireActivity())
        viewModel.isLogin.observe(viewLifecycleOwner) {
            if (it) {
                findNavController().navigate(R.id.home_fragment)
            }
        }
        viewModel.status.observe(viewLifecycleOwner) {
            if (it == STATUS.DOING) {
                loadingDialog.show()
            } else {
                loadingDialog.close()
            }
        }
        binding.apply {
            userViewModel = viewModel
            lifecycleOwner = viewLifecycleOwner
            masterPwInput.requestFocus()
            registerFragment = this@RegisterFragment
            buttonNext.setOnClickListener {
                val masterPassword = masterPwInput.text.toString()
                val confirmPassword = masterPwInput2.text.toString()
                val isValidPw = viewModel.validatePassword(masterPassword)
                if (!isValidPw) {
                    showErrorMessage("Invalid password", masterPwInputLayout)
                    masterPwInput.requestFocus()
                    return@setOnClickListener
                }
                val isCorrectConfirmPw = viewModel.confirmPassword(
                    masterPassword,
                    confirmPassword
                )
                if (!isCorrectConfirmPw) {
                    showErrorMessage(
                        "Confirm password must be same with master password",
                        masterPwInputLayout2
                    )
                    masterPwInput2.requestFocus()
                    return@setOnClickListener
                }
                viewModel.setMasterPassword(masterPassword)
                handleClickNextBtn()
            }
            buttonNext2.setOnClickListener {
                val email = emailInput.text.toString()
                if (viewModel.validateEmail(email)) {
                    viewModel.setEmail(email)
                    handleClickNextBtn()
                } else {
                    showErrorMessage("Invalid email", emailInputLayout)
                    emailInput.requestFocus()
                }

            }

            buttonNext3.setOnClickListener {
                val password = secondaryPwInput.text.toString()
                if (viewModel.validatePassword(password)) {
                    viewModel.setPassword(password)
                    viewModel.register()

                } else {
                    showErrorMessage("Invalid password", secondaryPwInputLayout)
                    secondaryPwInput.requestFocus()
                }
            }

            masterPwInput.doOnTextChanged { _, _, _, _ ->
                hideErrorMessage(masterPwInputLayout)
            }
            masterPwInput2.doOnTextChanged { _, _, _, _ ->
                hideErrorMessage(masterPwInputLayout2)
            }

            emailInput.doOnTextChanged { _, _, _, _ ->
                hideErrorMessage(emailInputLayout)
            }
            secondaryPwInput.doOnTextChanged { _, _, _, _ ->
                hideErrorMessage(secondaryPwInputLayout)
            }
        }
    }

    private fun handleClickNextBtn() {
        binding.viewFlipper.showNext()

    }

    private fun showErrorMessage(errorMessage: String, inputLayout: TextInputLayout) {
        inputLayout.error = errorMessage
        inputLayout.isErrorEnabled = true
    }

    private fun hideErrorMessage(inputLayout: TextInputLayout) {
        inputLayout.error = ""
        inputLayout.isErrorEnabled = false
    }

    fun handleCancelBtn() {
        binding.viewFlipper.showPrevious()
    }

}