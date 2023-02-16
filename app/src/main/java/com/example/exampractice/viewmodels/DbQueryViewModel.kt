package com.example.exampractice.viewmodels

import android.util.ArrayMap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exampractice.models.CategoryModel
import com.example.exampractice.models.TestModel
import com.example.exampractice.ui.fragments.CategoryFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.example.exampractice.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "DbQueryViewModel"

@HiltViewModel
class DbQueryViewModel
@Inject constructor(private val firestore: FirebaseFirestore) : ViewModel() {

    private var _categoriesList = MutableStateFlow<Resource<ArrayList<CategoryModel>>>(Resource.Ideal())
    val categoriesList : Flow<Resource<ArrayList<CategoryModel>>> get() = _categoriesList

    private var _testList = MutableStateFlow<Resource<ArrayList<TestModel>>>(Resource.Ideal())
    val testList : Flow<Resource<ArrayList<TestModel>>> get() = _testList

//    private var selectedCategoryIndex:Int=0



    fun loadCategories() {

        val categoriesList = arrayListOf<CategoryModel>()

        viewModelScope.launch {
            _categoriesList.value = Resource.Loading()

            firestore.collection("QUIZ")
                .get().addOnSuccessListener { querySnapshot ->

                    val docList = ArrayMap<String, QueryDocumentSnapshot>()

                    for (doc: QueryDocumentSnapshot in querySnapshot) {

                        docList[doc.id] = doc
                    }

                        val categoryListDoc = docList["CATEGORIES"]

                        categoryListDoc?.let {

                            val catCount: Long = categoryListDoc.getLong("COUNT") as Long

//                            Log.d(TAG, "loadCategories: ${categoryListDoc.getLong("COUNT")}")

                            for (i in 1 .. catCount) {

                                val catId = categoryListDoc.getString("CAT${i}_ID").toString()
                                val catDoc = docList[catId]

                                val catNumberOfTest: Int? = catDoc?.getLong("NUMBER_OF_TESTS")?.toInt()
                                val catName = catDoc?.getString("CATEGORY_NAME").toString()
                                val categoryModel = CategoryModel(catId, catName, catNumberOfTest!!)

                                categoriesList.add(categoryModel)
                            }
                            _categoriesList.value = Resource.Success(categoriesList)

                    }

                }.addOnFailureListener {
                    _categoriesList.value = Resource.Error(it.message.toString())
                }
        }

    }

    fun loadTestData(selectedCategoryIndex:Int){

        val testList = ArrayList<TestModel>()

        Log.d(TAG, "loadTestData: ${_categoriesList.value.data?.get(selectedCategoryIndex)?.id}")


            firestore.collection("QUIZ")
                .document(CategoryFragment.categoryList[selectedCategoryIndex].id)
                .collection("TEST_LIST")
                .document("TESTS_INFO")
                .get()
                .addOnSuccessListener {documentSnapshot->


                    val numOfTests = CategoryFragment.categoryList[selectedCategoryIndex].numberOfTests

                    Log.d(TAG, "loadTestData: num of tests ${CategoryFragment.categoryList[selectedCategoryIndex].numberOfTests}")

                    for(i in 1 .. numOfTests){

                        val testModel = TestModel(
                            documentSnapshot.getString("TEST${i}_ID").toString(),
                            0,
                            documentSnapshot.getLong("TEST${i}_TIME")!!.toInt())

                        Log.d(TAG, "loadTestData: test id TEST${i}_ID")

                        testList.add(testModel)

                    }
                    _testList.value = Resource.Success(testList)

                }
                .addOnFailureListener {
                    _testList.value = Resource.Error(it.message.toString())
                }
        }
    }