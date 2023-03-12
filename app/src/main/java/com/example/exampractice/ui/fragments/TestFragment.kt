package com.example.exampractice.ui.fragments

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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.exampractice.adapters.TestAdapter
import com.example.exampractice.databinding.FragmentTestBinding
import com.example.exampractice.models.TestModel
import com.example.exampractice.ui.activites.HomeActivity
import com.example.exampractice.util.Resource
import com.example.exampractice.util.onItemClick
import com.example.exampractice.viewmodels.TestViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


private const val TAG = "TestFragment"

@AndroidEntryPoint
class TestFragment : Fragment() {

    private var _binding: FragmentTestBinding? = null
    private val binding get() = _binding!!

    private val args: TestFragmentArgs by navArgs()

    private lateinit var adapter: TestAdapter

    private val testViewModel by viewModels<TestViewModel>()

    //    companion object{
    private var testList = arrayListOf<TestModel>()

    private var testPosition: Int = 0

    private var topScore: Int = 0
//    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentTestBinding.inflate(inflater, container, false)

        (requireActivity() as HomeActivity).supportActionBar?.title = args.categoryName
        (requireActivity() as HomeActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = TestAdapter()

        setUpRecyclerView()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        testViewModel.loadTestData(args.categoryIndex)

        lifecycleScope.launchWhenStarted {
            testViewModel.testList.collect {
                when (it) {

                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }


                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        it.data?.let { testModelArrayList ->

                            testList.addAll(testModelArrayList)

                            adapter.asyncListDiffer.submitList(testModelArrayList)


                        }

                    }
                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Log.e(TAG, "onViewCreated: ${it.message.toString()}")
                    }
                    else -> {}
                }
            }
        }

        testViewModel.loadTopScore(testList)

        lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                testViewModel.topScore.collectLatest { result ->
                    when (result) {

                        is Resource.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }

                        is Resource.Success -> {
                            binding.progressBar.visibility = View.GONE
                            result.data?.let {
//                            testList[testPosition].topScore = it
                                topScore = it
                                Log.v(TAG, "top score is $it")
                            }
                        }
                        is Resource.Error -> {
                            binding.progressBar.visibility = View.GONE
                            Log.e(TAG, "top score error: ${result.message.toString()}")
                        }
                        else -> {}
                    }
                }
            }


        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        testList.clear()
    }

    private fun setUpRecyclerView() {
        binding.testRv.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = this@TestFragment.adapter

            onItemClick { _, position, _ ->

                testPosition = position

                findNavController().navigate(
                    TestFragmentDirections.actionTestFragmentToStartTestFragment(
                        testList = testList.toTypedArray(),
                        testPosition = position,
                        categoryIndex = args.categoryIndex,
                        categoryName = args.categoryName,
                        testTime = testList[position].time.toString()
                    )
                )

            }
        }
    }

}