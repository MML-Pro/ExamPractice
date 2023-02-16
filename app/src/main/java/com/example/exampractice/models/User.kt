package com.example.exampractice.models

data class User(
    val userName: String,
    val email: String,
    var imagePath: String = "") {
    constructor() : this("", "", "")
}
