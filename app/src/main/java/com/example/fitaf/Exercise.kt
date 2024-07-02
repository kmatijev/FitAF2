package com.example.fitaf

import java.util.UUID

data class Exercise(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    var sets: Int = 0,
    var reps: Int = 0,
    var weight: Int = 0
) {
    override fun toString(): String {
        return "$name - $sets x $reps x $weight kg"
    }
}
