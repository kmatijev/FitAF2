package com.example.fitaf

import java.util.UUID

data class Routine(
    val userId: String = "",
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val description: String = "",
    val exercises: List<Exercise> = emptyList()
)
