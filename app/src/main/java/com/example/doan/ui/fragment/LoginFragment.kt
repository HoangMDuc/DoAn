package com.example.doan.ui.fragment

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.doan.R
import com.example.doan.databinding.FragmentLoginBinding
import com.example.doan.viewmodel.UserViewModel
import com.google.android.material.textfield.TextInputLayout

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Login.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding


    private val viewModel: UserViewModel by activityViewModels{
        UserViewModel.UserViewModelFactory(activity?.application as Application)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        if(viewModel.isFirstTimeAccess()) {


            findNavController().navigate(R.id.register_fragment)
        }

    }
    private fun handleClick() {
        Log.d("LoginFragment", "handleClick: ${viewModel.password.value}")
        val password = binding.secondaryPwInput.text.toString()
        if(viewModel.validatePassword(password)) {
            viewModel.setPassword(password)

            val sharedPreferences = activity?.getSharedPreferences(UserViewModel.PREFERENCES_NAME, MODE_PRIVATE)
            val user_pw = sharedPreferences?.getString("user_password" , "")
            if(user_pw == password) {
                viewModel.setLogin(true)
            }else {
                showErrorMessage("Incorrect Password", binding.secondaryPwInputLayout)
            }

        }else {
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


}