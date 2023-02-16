package com.example.exampractice.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.exampractice.databinding.TestItemLayoutBinding
import com.example.exampractice.models.TestModel
import com.example.exampractice.util.TestUtil


class TestAdapter() : RecyclerView.Adapter<TestAdapter.TestViewHolder>() {

    private val diffCallback: DiffUtil.ItemCallback<TestModel> =
        object : DiffUtil.ItemCallback<TestModel>() {
            override fun areItemsTheSame(oldItem: TestModel, newItem: TestModel): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: TestModel, newItem: TestModel): Boolean {
                return oldItem == newItem
            }

        }
    val asyncListDiffer = AsyncListDiffer(this, diffCallback)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestViewHolder {
        return TestViewHolder(
            TestItemLayoutBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return asyncListDiffer.currentList.size
    }

    override fun onBindViewHolder(holder: TestViewHolder, position: Int) {
        holder.bind(asyncListDiffer.currentList[position])
    }

    inner class TestViewHolder(private val binding: TestItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(testModel: TestModel) {
            binding.apply {
                testNumber.text = "Test No : ${adapterPosition + 1}"
                scorePercentage.text = "${testModel.topScore}%"
                testProgressBar.progress = testModel.topScore

            }
        }

    }
}