package com.example.exampractice.ui.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.exampractice.databinding.FragmentLoginBinding
import com.example.exampractice.ui.activites.HomeActivity
import com.example.exampractice.util.RegisterValidation
import com.example.exampractice.util.Resource
import com.example.exampractice.viewmodels.CredentialsViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.Task
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "LoginFragment"


@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel by viewModels<CredentialsViewModel>()

    private lateinit var googleSignInClient: GoogleSignInClient


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentLoginBinding.inflate(inflater, container, false)

//        val textView: TextView = binding.textHome
//        homeViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            loginButton.setOnClickListener {

                val email = editTextTextEmailAddress.text.toString().trim()
                val password = editTextTextPassword.text.toString()

                viewModel.login(email, password)


            }

            signUpButton.setOnClickListener {
                findNavController().navigate(LoginFragmentDirections.actionNavLoginToSignUpFragment())
            }

            googleSignInButton.setOnClickListener {
                googleSignInClient = GoogleSignIn.getClient(requireActivity(), viewModel.getGoogleSignInOptions())
                signInGoogle()
            }
        }


        lifecycleScope.launchWhenStarted {

            viewModel.login.collect {
                when (it) {
                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE

                        val intent = Intent(requireContext(), HomeActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Log.e(TAG, "onViewCreated: ${it.message.toString()}")
                        Toast.makeText(requireContext(), it.message.toString(), Toast.LENGTH_LONG)
                            .show()
                    }

                    else -> {}
                }
            }

        }

        lifecycleScope.launchWhenStarted {
            viewModel.validation.collect { validation ->

                if (validation.email is RegisterValidation.Failed) {
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                        binding.editTextTextEmailAddress.apply {
                            requestFocus()
                            error = validation.email.message
                        }
                    }
                }

                if (validation.password is RegisterValidation.Failed) {
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                        binding.editTextTextPassword.apply {
                            requestFocus()
                            error = validation.password.message
                        }
                    }
                }

            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.signInWithGoogle.collect {
                when (it) {
                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {


                        binding.progressBar.visibility = View.GONE

                        val intent = Intent(requireContext(), HomeActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)

                        it.data?.let { account ->
                            intent.putExtra("email", account.email)
                            intent.putExtra("name", account.displayName)
                        }

                        startActivity(intent)
                    }
                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()

                    }
                    else -> {}
                }
            }
        }

    }

    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleResults(task)
            }
        }

    private fun handleResults(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful) {
            val account: GoogleSignInAccount? = task.result
            if (account != null) {
                viewModel.updateUI(account)
            }
        } else {
            Toast.makeText(requireContext(), task.exception.toString(), Toast.LENGTH_SHORT).show()
        }
    }

//    @Deprecated("Deprecated in Java")
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        when (requestCode) {
//            REQ_ONE_TAP -> {
//                try {
//                    val credential = oneTapClient.getSignInCredentialFromIntent(data)
//                    val idToken = credential.googleIdToken
//                    when {
//                        idToken != null -> {
//                            // Got an ID token from Google. Use it to authenticate
//                            // with Firebase.
//                            Log.d(TAG, "Got ID token.")
//                        }
//                        else -> {
//                            // Shouldn't happen.
//                            Log.d(TAG, "No ID token!")
//                        }
//                    }
//                } catch (e: ApiException) {
//                    // ...
//                }
//            }
//        }
    // ...

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

//    private fun validateData(): Boolean {
//
//        if(binding.editTextTextEmailAddress.text.toString().isEmpty()){
//            binding.editTextTextEmailAddress.error = "Email cannot be empty"
//            return false
//        }
//        if(binding.editTextTextPassword.text.toString().isEmpty()){
//            binding.editTextTextPassword.error = "Password cannot be empty"
//            return false
//        }
//
//        return false
//    }

