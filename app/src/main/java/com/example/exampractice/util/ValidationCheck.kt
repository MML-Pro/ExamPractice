package com.example.exampractice.util

import android.util.Patterns

fun validateEmail(email: String): RegisterValidation {

    return if (email.isEmpty()) {
        RegisterValidation.Failed("Email cannot be empty")
    } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        RegisterValidation.Failed("Wrong email format")
    } else {
        RegisterValidation.Success
    }
}

fun validatePassword(password: String, confirmationPassword: String? = null): RegisterValidation {

    if (confirmationPassword == null) {
        return if (password.isEmpty()) {
            RegisterValidation.Failed("password cannot be empty")
        } else if (password.length < 6) {
            RegisterValidation.Failed("password cannot be less than 6 chars")
        } else RegisterValidation.Success
    } else {
        return if (password.isEmpty()) {
            RegisterValidation.Failed("password cannot be empty")
        } else if (password.length < 6) {
            RegisterValidation.Failed("password cannot be less than 6 chars")
        } else if (password != confirmationPassword) {
            RegisterValidation.Failed("password doesn't matches")
        } else RegisterValidation.Success

    }
}