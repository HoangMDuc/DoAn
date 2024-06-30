package com.example.doan.ui.fragment

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.doan.R
import com.example.doan.databinding.FragmentSettingsBinding
import com.example.doan.network.UserApiService
import com.example.doan.repository.KeysRepository
import com.example.doan.ui.dialog.AuthenticateDialog
import com.example.doan.ui.dialog.LoadingDialog
import com.example.doan.utils.STATUS
import com.example.doan.utils.isNetworkAvailable
import com.example.doan.utils.showErrorSnackBar
import com.example.doan.viewmodel.UserViewModel

enum class ACTION {
    CHANGE_EMAIL,
    CHANGE_PASSWORD,
    CHANGE_MASTER_PASSWORD
}

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private val authenticateDialog: AuthenticateDialog by lazy {
        AuthenticateDialog(getListener())
    }
    private var act = ACTION.CHANGE_PASSWORD
    private val loadingDialog: LoadingDialog by lazy {
        LoadingDialog(requireActivity())
    }
    private val userViewModel: UserViewModel by viewModels {
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.changeEmail.setOnClickListener {

            if(isNetworkAvailable(requireActivity())) {
                act = ACTION.CHANGE_EMAIL
                activity?.supportFragmentManager?.let {
                    authenticateDialog.show(it, "Change email")
                }
            }else {
                showErrorSnackBar(requireView(), requireContext(), "Please check your internet connection")
            }


        }
        binding.changePassword.setOnClickListener {
            if(isNetworkAvailable(requireActivity())) {
                act = ACTION.CHANGE_PASSWORD
                activity?.supportFragmentManager?.let {
                    authenticateDialog.show(it, "Change password")
                }
            }else {
                showErrorSnackBar(requireView(), requireContext(), "Please check your internet connection")
            }
        }
        binding.changeMasterPassword.setOnClickListener {
            if(isNetworkAvailable(requireActivity())) {
                act = ACTION.CHANGE_MASTER_PASSWORD
                activity?.supportFragmentManager?.let {
                    authenticateDialog.show(it, "Change master password")
                }
            }else {
                showErrorSnackBar(requireView(), requireContext(), "Please check your internet connection")
            }



        }
        userViewModel.status.observe(viewLifecycleOwner) {
            if (it == STATUS.DOING) {
                loadingDialog.show()
            } else {
                loadingDialog.close()
            }
        }
        userViewModel.isCorrectPassword.observe(viewLifecycleOwner) {
            if (it) {
                authenticateDialog.dismiss()
                userViewModel.setIsCorrectPassword(false)
                Log.d("Navigation", act.toString())
                when (act) {
                    ACTION.CHANGE_EMAIL -> {
                        val action =
                            SettingsFragmentDirections.actionSettingsFragmentToChangeEmailFragment()
                        findNavController().navigate(action)
                    }
                    ACTION.CHANGE_PASSWORD -> {
                        val action =
                            SettingsFragmentDirections.actionSettingsFragmentToChangePasswordFragment()
                        findNavController().navigate(action)
                    }

                    else -> {
                        val action = SettingsFragmentDirections.actionSettingsFragmentToChangeMasterPasswordFragment()
                        findNavController().navigate(action)
                    }
                }
            } else {
                authenticateDialog.setError("Wrong password")
            }
        }
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
                    userViewModel.isCorrectMasterPassword(password)
                }

            }

            override fun onCancel(dialog: AuthenticateDialog) {
                dialog.dismiss()
            }
        }
    }


}