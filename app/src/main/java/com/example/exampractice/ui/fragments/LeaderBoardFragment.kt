package com.example.exampractice.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.exampractice.adapters.RankAdapter
import com.example.exampractice.databinding.FragmentLeaderBoardBinding
import com.example.exampractice.models.RankModel
import com.example.exampractice.util.Resource
import com.example.exampractice.viewmodels.CredentialsViewModel
import com.example.exampractice.viewmodels.LeaderboardViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

private const val TAG = "LeaderBoardFragment"

@AndroidEntryPoint
class LeaderBoardFragment : Fragment() {

    private var _binding: FragmentLeaderBoardBinding? = null
    private val binding get() = _binding!!
    private lateinit var rankAdapter: RankAdapter

    private val leaderboardViewModel by viewModels<LeaderboardViewModel>()
    private val credentialsViewModel by viewModels<CredentialsViewModel>()

    private var myPerformanceLocal = CredentialsViewModel.myPerformanceLocal

    private val localUsersList = arrayListOf<RankModel>()

    private var usersCount: Int = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentLeaderBoardBinding.inflate(inflater, container, false)

        rankAdapter = RankAdapter()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val linearLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        binding.usersRV.apply {
            layoutManager = linearLayoutManager
            adapter = rankAdapter
        }


        leaderboardViewModel.getUsersCount()

        lifecycleScope.launchWhenStarted {
            leaderboardViewModel.usersCount.collectLatest {

                when (it) {
                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        it.data?.let { usersCount ->
                            binding.totalUsersTv.text = "Total Users : $usersCount"
                            this@LeaderBoardFragment.usersCount = usersCount
                        }
                    }
                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Log.e(TAG, "getUsersCount: ${it.message.toString()}")
                    }
                    else -> {}
                }

            }
        }

        credentialsViewModel.getUserData()

        lifecycleScope.launchWhenStarted {
            credentialsViewModel.myPerformance.collect {

                when (it) {
                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }

                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        it.data?.let { rankModel ->
                            myPerformanceLocal = rankModel

                            Log.d(TAG, "myPerformance: name ${rankModel.name}")
                        }
                    }

                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Log.e(TAG, "getUsersCount: ${it.message.toString()}")
                    }
                    else -> {}
                }

            }
        }


        leaderboardViewModel.getTopUsers(myPerformanceLocal)


        lifecycleScope.launchWhenStarted {
            leaderboardViewModel.usersList.collect {
                when (it) {

                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }

                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE

                        it.data?.let { rankModelArray ->
                            rankAdapter.submitList(rankModelArray)
                            localUsersList.addAll(rankModelArray)

//                            Log.d(TAG, "usersList: ${rankModelArray.size}")
                        }
                    }

                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
//                        Log.e(TAG, "getUsersCount: ${it.message.toString()}")
                    }

                    else -> {}
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            leaderboardViewModel.isMeInTheTopList.collect {

                when (it) {

                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }

                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE

                        it.data?.let { isMeInTheTopList ->

                            if (myPerformanceLocal.score != 0) {

                                if (!isMeInTheTopList) {
                                    calculateRank()
                                }
                            }
                            binding.apply {
                                myScoreTv.text = "Score : ${myPerformanceLocal.score}"
                                myRankTv.text = "Rank : ${myPerformanceLocal.rank}"
                            }
                        }
                    }

                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Log.e(TAG, "getUsersCount: ${it.message.toString()}")
                    }

                    else -> {}
                }
            }
        }


    }

    private fun calculateRank() {
        val lowTopScore = localUsersList[localUsersList.size - 1].score
        val remainingUsers = usersCount - 30
        val mySlot = (myPerformanceLocal.score * remainingUsers) / lowTopScore

        val rank = if (lowTopScore != myPerformanceLocal.score) {
            usersCount - mySlot
        } else {
            31
        }
        myPerformanceLocal.rank = rank
    }

}