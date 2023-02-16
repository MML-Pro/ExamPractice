package com.example.exampractice.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.exampractice.adapters.TestAdapter
import com.example.exampractice.databinding.FragmentTestBinding
import com.example.exampractice.models.TestModel
import com.example.exampractice.ui.activites.HomeActivity
import com.example.exampractice.util.Resource
import com.example.exampractice.util.TestUtil
import com.example.exampractice.util.onItemClick
import com.example.exampractice.viewmodels.DbQueryViewModel
import dagger.hilt.android.AndroidEntryPoint


private const val TAG = "TestFragment"

@AndroidEntryPoint
class TestFragment : Fragment() {

    private var _binding: FragmentTestBinding? = null
    private val binding get() = _binding!!

    private val args: TestFragmentArgs by navArgs()

    private lateinit var adapter: TestAdapter

    private val viewModel by viewModels<DbQueryViewModel>()

    private var position: Int = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentTestBinding.inflate(inflater, container, false)

        (requireActivity() as HomeActivity).supportActionBar?.title = args.categoryName
        (requireActivity() as HomeActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = TestAdapter()
        viewModel.loadTestData(args.categoryPosition)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        testList.add(TestModel("1",50,20))
//        testList.add(TestModel("2",80,20))
//        testList.add(TestModel("3",0,25))
//        testList.add(TestModel("4",10,40))


        lifecycleScope.launchWhenStarted {
            viewModel.testList.collect{
                when(it){
                    is Resource.Success ->{
                        adapter.asyncListDiffer.submitList(it.data)
                    }
                    is Resource.Error ->{
                        Log.e(TAG, "onViewCreated: ${it.message.toString()}" )
                    }
                    else -> {}
                }
            }
        }

        setUpRecyclerView()


    }

    private fun setUpRecyclerView() {
        binding.testRv.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = this@TestFragment.adapter

        }
    }

}