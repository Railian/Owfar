package com.owfar.android.extensions

fun String.orNullIfEmpty() = if (this.isEmpty()) null else this

fun String.orNullIfBlank() = if (this.isBlank()) null else this