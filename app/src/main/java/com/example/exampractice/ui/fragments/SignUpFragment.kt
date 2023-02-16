package com.example.exampractice.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.exampractice.databinding.FragmentSignUpBinding
import com.example.exampractice.models.User
import com.example.exampractice.ui.activites.HomeActivity
import com.example.exampractice.util.RegisterValidation
import com.example.exampractice.util.Resource
import com.example.exampractice.viewmodels.CredentialsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext


private const val TAG = "SignUpFragment"


@AndroidEntryPoint
class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<CredentialsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.signUpButton.setOnClickListener {
            val user = User(
                binding.userNameEditText.text.toString().trim(),
                binding.emailEditText.text.toString().trim(),
            )

            val password = binding.passwordEditText.text.toString()

            viewModel.createAccountWithEmailAndPassword(
                user,
                password,
                binding.confirmPassword.text.toString()
            )


        }

        lifecycleScope.launchWhenStarted {
            viewModel.register.collect {
                when (it) {

                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        Log.d(TAG, "onViewCreated: ${it.message.toString()}")
                        val intent = Intent(requireContext(), HomeActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        requireActivity().finish()
                    }
                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Log.e(TAG, "onViewCreated: ${it.message.toString()}")
                        Toast.makeText(requireContext(), it.message.toString(), Toast.LENGTH_LONG).show()

                    }

                    else -> {}
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.validation.collect { validation ->
                if (validation.email is RegisterValidation.Failed) {
                    withContext(Dispatchers.Main) {
                        binding.emailEditText.apply {
                            requestFocus()
                            error = validation.email.message
                        }
                    }
                }

                if (validation.password is RegisterValidation.Failed) {
                    withContext(Dispatchers.Main) {
                        binding.passwordEditText.apply {
                            requestFocus()
                            error = validation.password.message
                        }
                        binding.confirmPassword.apply {
                            requestFocus()
                            error = validation.password.message
                        }
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.updateUserInfo.collect{
                when(it){

                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                    }

                    is Resource.Error -> {
                        Log.e(TAG, "onViewCreated: ${it.message.toString()}" )
                        Toast.makeText(requireContext(), it.message.toString(), Toast.LENGTH_LONG).show()
                    }

                    else -> {}
                }
            }
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}