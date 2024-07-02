package com.example.fitaf

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun RoutineAddExercisesPage(
    navController: NavHostController,
    routine: Routine?,
    onExerciseAdd: (Exercise) -> Unit,
) {
    var showAddExerciseDialog by remember { mutableStateOf(false) }

    if (showAddExerciseDialog) {
        AddExerciseDialog(
            onDismiss = { showAddExerciseDialog = false },
            onSave = onExerciseAdd
        )
    }

    Scaffold { contentPadding ->
        Column {
            AppBar(title = routine?.name ?: "Loading...")
            if (routine != null) {
                LazyColumn(
                    modifier = Modifier
                        .padding(contentPadding)
                        .weight(1f)
                ) {
                    items(routine.exercises) {
                        ExerciseItem(exercise = it)
                    }
                }
            } else {
                Text(text = "Loading...")
            }
            AppButton(
                label = "Add exercise",
                icon = Icons.Outlined.AddCircle,
                onClick = { showAddExerciseDialog = true },
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}