package com.example.exampractice.ui.fragments

import CategoryAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.exampractice.databinding.FragmentCategoryBinding
import com.example.exampractice.models.CategoryModel
import com.example.exampractice.util.Resource
import com.example.exampractice.viewmodels.CategoriesViewModel
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "CategoryFragment"

@AndroidEntryPoint
class CategoryFragment : Fragment() {

    private var _binding:FragmentCategoryBinding?=null
    private val binding get() = _binding!!

    companion object{
        var categoryList = arrayListOf<CategoryModel>()
    }
    private lateinit var adapter: CategoryAdapter

    private val viewModel by viewModels<CategoriesViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)


        adapter = CategoryAdapter()

//        viewModel.loadCategories()


        loadCategories()

//        Log.d(TAG, "onCreateView: cat list size is ${testList.size}")


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