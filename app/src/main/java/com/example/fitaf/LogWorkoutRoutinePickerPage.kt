package com.example.fitaf

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogWorkoutRoutinePickerPage(
    routines: List<Routine>?,
    onRoutineClick: (Routine) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Log workout") })
        }
    ) { contentPadding ->
        if (routines == null) {
            Text("Loading...")
            return@Scaffold
        }
        LazyColumn(modifier = Modifier.padding(contentPadding)) {
            items(routines) {
                Button(onClick = { onRoutineClick(it) }) {
                    Text(text = it.name)
                }
            }
        }
    }
}