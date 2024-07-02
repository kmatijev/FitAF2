package com.example.fitaf

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun LogWorkoutRoutinePickerPage(
    routines: List<Routine>?,
    onRoutineClick: (Routine) -> Unit,
) {
    Scaffold { contentPadding ->
        Column {
            AppBar(title = "Pick routine")
            if (routines == null) {
                Text("Loading...")
                return@Scaffold
            }
            LazyColumn(modifier = Modifier
                .padding(contentPadding)
                .weight(1f)) {
                items(routines) {
                    RoutineItem(
                        routine = it,
                        onClick = { onRoutineClick(it) }
                    )
                }
            }
        }
    }
}