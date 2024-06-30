package com.example.doan.ui.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.doan.R
import com.example.doan.databinding.FragmentChangeEmailBinding
import com.example.doan.network.UserApiService
import com.example.doan.repository.KeysRepository
import com.example.doan.ui.dialog.LoadingDialog
import com.example.doan.utils.STATUS
import com.example.doan.viewmodel.UserViewModel
import com.google.android.material.snackbar.Snackbar


class ChangeEmailFragment : Fragment() {

    private lateinit var binding: FragmentChangeEmailBinding
    private val loadingDialog : LoadingDialog by lazy {
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
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_change_email, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.emailInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.saveUpdate.isEnabled = s.toString().isNotEmpty()
                binding.emailInputLayout.isErrorEnabled = false
                binding.emailInputLayout.error = ""
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
        binding.saveUpdate.setOnClickListener {
            handleUpdateEmail()
        }

        userViewModel.status.observe(viewLifecycleOwner) {
            if(it == STATUS.DOING) {
                loadingDialog.show()
            }else {
                if(it == STATUS.DONE) {
                    loadingDialog.close()
                    Snackbar.make(binding.root, "Change email successfully", Snackbar.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }else {
                    Snackbar.make(binding.root, "Change email failed", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleUpdateEmail() {
        val email = binding.emailInput.text.toString()
        if (email.isNotEmpty()){
            if (userViewModel.validateEmail(email)) {
                userViewModel.updateEmail(email)
            }else {
                binding.emailInputLayout.isErrorEnabled = true
                binding.emailInputLayout.error = "Invalid email"
            }
        }
    }
}