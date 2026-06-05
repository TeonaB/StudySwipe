package com.example.studyswipe.utils

import android.util.Patterns

fun String.isValidEmail(): Boolean {
    return isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.isValidPassword(): Boolean {
    return this.length >= 6
}
