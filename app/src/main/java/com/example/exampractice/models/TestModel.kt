package com.example.exampractice.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TestModel(
    val testId: String, var topScore: Int, val time: Int
) : Parcelable