package com.example.exampractice.ui.fragments

import CategoryAdapter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.exampractice.R
import com.example.exampractice.databinding.FragmentCategoryBinding
import com.example.exampractice.models.CategoryModel
import com.example.exampractice.util.Resource
import com.example.exampractice.util.TestUtil
import com.example.exampractice.viewmodels.DbQueryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

private const val TAG = "CategoryFragment"

@AndroidEntryPoint
class CategoryFragment : Fragment() {

    private var _binding:FragmentCategoryBinding?=null
    private val binding get() = _binding!!

    companion object{
        var categoryList = arrayListOf<CategoryModel>()
    }
    private lateinit var adapter: CategoryAdapter

    private val viewModel by viewModels<DbQueryViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)


        adapter = CategoryAdapter()

//        viewModel.loadCategories()


        loadCategories()

//        Log.d(TAG, "onCreateView: cat list size is ${categoryList.size}")


        binding.categoryGridView.adapter = adapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launchWhenStarted {
            viewModel.categoriesList.collect{ result->
                when(result){
                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        result.data?.let {
                            categoryList = it
                            adapter.submitList(categoryList)
                            adapter.notifyDataSetChanged()
                        }
                    }
                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Log.e(TAG, "onCreateView: ${result.message.toString()}" )
                    }
                    else -> {}
                }
            }
        }
    }


    private fun loadCategories() {
        categoryList.clear()
        viewModel.loadCategories()
    }

}