package com.example.fitaf

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutinePage(navController: NavHostController, routine: Routine?, onExerciseAdd: (Exercise) -> Unit) {
    var showAddExerciseDialog by remember { mutableStateOf(false) }

    if (showAddExerciseDialog) {
        AddExerciseDialog(
            onDismiss = { showAddExerciseDialog = false },
            onSave = onExerciseAdd
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(routine?.name ?: "Loading") })
        }
    ) { contentPadding ->
        Column {
            if (routine != null) {
                LazyColumn(modifier = Modifier
                    .padding(contentPadding)
                    .weight(1f)) {
                    items(routine.exercises) {
                        Text("${it.name} - ${it.sets}x${it.reps}x${it.weight}kg")
                    }
                }
            } else {
                CircularProgressIndicator()
            }
            Button(onClick = { showAddExerciseDialog = true }) {
                Text(text = "Add exercise")
            }
        }
    }
}
