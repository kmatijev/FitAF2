package com.example.fitaf

import java.util.UUID

data class Exercise(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    var sets: Int = 0,
    var reps: Int = 0,
    var weight: Float = 0f
)
