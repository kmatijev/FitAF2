package com.example.fitaf

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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

    Scaffold { contentPadding ->
        Column(modifier = Modifier.padding(contentPadding)) {
            AppBar(title = "Finish a workout")
            if (routine == null) {
                Text(text = "Loading")
                return@Scaffold
            }
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(routine.exercises) { exercise ->
                    ExerciseItem(
                        exercise = exercise,
                        onClick = { exerciseToEdit = exercise }
                    )
                }
            }
            AppButton(
                label = "Finish",
                icon = Icons.Outlined.Check,
                onClick = onRoutineSave,
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}