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
import com.example.exampractice.databinding.FragmentProfileBinding
import com.example.exampractice.ui.activites.MainActivity
import com.example.exampractice.viewmodels.CredentialsViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val TAG = "ProfileFragment"

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    private lateinit var googleSignInClient: GoogleSignInClient

    private val viewModel by viewModels<CredentialsViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.logoutButton.setOnClickListener {


            val currentUser = firebaseAuth.currentUser ?: return@setOnClickListener

            for (i in 0 until currentUser.providerData.size) {
                when (currentUser.providerData[i].providerId) {
                    "google.com" -> {
                        //User signed in with a custom account
                        Log.d(
                            TAG,
                            "onViewCreated: provider is ${currentUser.providerData[i].providerId}"
                        )
                        googleSignInClient = GoogleSignIn.getClient(
                            requireActivity(),
                            viewModel.getGoogleSignInOptions()
                        )

                        firebaseAuth.signOut()

                        googleSignInClient.signOut().addOnCompleteListener {

                            if (it.isSuccessful) {
                                redirectToMainActivity()
                            }else {
                                return@addOnCompleteListener
                            }
                        }

                    }
                    "password" -> {
                        Log.d(TAG, "onViewCreated: ${currentUser.providerData[i].providerId}")
                        firebaseAuth.signOut()
                        redirectToMainActivity()
                    }
                    else -> {
                        firebaseAuth.signOut()
                        Log.d(TAG, "onViewCreated: ${currentUser.providerData[i].providerId}")
                        redirectToMainActivity()
                        return@setOnClickListener
                    }
                }
            }

        }
    }

    private fun redirectToMainActivity() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}