import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.navigation.findNavController
import com.example.exampractice.databinding.CategoryItemLayoutBinding
import com.example.exampractice.models.CategoryModel
import com.example.exampractice.ui.fragments.CategoryFragmentDirections
import com.example.exampractice.util.TestUtil

class CategoryAdapter() : BaseAdapter() {

    private var categoryList: List<CategoryModel> = arrayListOf()

    override fun getCount(): Int = categoryList.size

    override fun getItem(p0: Int): Any? = null

    override fun getItemId(p0: Int): Long = 0

    fun submitList(newList:ArrayList<CategoryModel>){
        categoryList = newList
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        if (convertView == null) {
            val itemBinding =
                CategoryItemLayoutBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            holder = ViewHolder(itemBinding)
            holder.binding.root.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }

        holder.binding.categoryName.text = categoryList[position].name
        holder.binding.numberOfTests.text = categoryList[position].numberOfTests.toString()

        holder.binding.root.setOnClickListener {
            it.findNavController().navigate(CategoryFragmentDirections.actionCategoryFragmentToTestFragment(
                categoryList[position].name,position
            ))
        }

        return holder.binding.root
    }

    inner class ViewHolder(val binding: CategoryItemLayoutBinding)
}





