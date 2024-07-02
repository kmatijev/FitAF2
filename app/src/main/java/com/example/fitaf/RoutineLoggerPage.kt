package com.example.fitaf

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineLoggerPage(
    routine: Routine?,
    onExerciseSave: (Exercise) -> Unit,
    onRoutineSave: () -> Unit
) {
    var exerciseToEdit by remember { mutableStateOf<Exercise?>(null) }

    if (exerciseToEdit != null) {
        AddExerciseDialog(
            onDismiss = { exerciseToEdit = null },
            onSave = onExerciseSave,
            initialData = exerciseToEdit,
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Log ${routine?.name}") })
        }
    ) { contentPadding ->
        if (routine == null) {
            Text(text = "Loading")
            return@Scaffold
        }
        Column(modifier = Modifier.padding(contentPadding)) {
            LazyColumn {
                items(routine.exercises) { exercise ->
                    Button(onClick = { exerciseToEdit = exercise }) {
                        Text(text = exercise.toString())
                    }
                }
            }
            Button(onClick = onRoutineSave) {
                Text(text = "Finish")
            }
        }
    }
}