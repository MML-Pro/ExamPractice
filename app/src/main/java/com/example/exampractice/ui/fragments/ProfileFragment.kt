package com.example.exampractice.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.exampractice.databinding.FragmentProfileBinding
import com.example.exampractice.models.RankModel
import com.example.exampractice.ui.activites.HomeActivity
import com.example.exampractice.ui.activites.MainActivity
import com.example.exampractice.util.Resource
import com.example.exampractice.viewmodels.CredentialsViewModel
import com.example.exampractice.viewmodels.LeaderboardViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ProfileFragment"

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    private lateinit var googleSignInClient: GoogleSignInClient

    private val credentialsViewModel by viewModels<CredentialsViewModel>()

    private val leaderboardViewModel by viewModels<LeaderboardViewModel>()

    private var userName: String? = null

    var myPerformanceLocal = RankModel(null, 0, -1)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(inflater, container, false)


        this@ProfileFragment.userName = (requireActivity() as HomeActivity).userName

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        credentialsViewModel.getUserData()

        lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                credentialsViewModel.myPerformance.collectLatest { result ->
                    when (result) {
                        is Resource.Loading -> {
                            binding.progressBar2.visibility = View.VISIBLE
                        }
                        is Resource.Success -> {

                            result.data?.let {
                                myPerformanceLocal.score = it.score


                                leaderboardViewModel.getTopUsers(myPerformanceLocal)


                            }

                            binding.apply {
                                progressBar2.visibility = View.GONE
                                myScoreTv.text = myPerformanceLocal.score.toString()
                                myRankTv.text = myPerformanceLocal.rank.toString()
                            }

                        }

                        is Resource.Error -> {
                            binding.progressBar2.visibility = View.GONE
                            Log.e(TAG, "onViewCreated: ${result.message.toString()}")
                        }
                        else -> {}
                    }
                }
            }


        }




        binding.profileTv.text = userName?.uppercase()?.substring(0, 1)
        binding.profileNameTv.text = userName
        binding.logoutLL.setOnClickListener {


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
                            credentialsViewModel.getGoogleSignInOptions()
                        )

                        firebaseAuth.signOut()

                        googleSignInClient.signOut().addOnCompleteListener {

                            if (it.isSuccessful) {
                                redirectToMainActivity()
                            } else {
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