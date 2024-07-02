package com.example.fitaf

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import java.util.UUID

@Composable
fun RoutinesPage(navController: NavHostController) {
    val routines = remember { mutableStateListOf<Routine>() }
    var showEditDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var routineToEdit by remember { mutableStateOf<Routine?>(null) }

    LaunchedEffect(Unit) {
        getAllRoutines { routines.addAll(it)  }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        AppBar(title = "Routines")
        LazyColumn {
            items(routines) { routine ->
                RoutineItem(
                    routine = routine,
                    onClick = { navController.navigate("routine/${routine.id}") },
                    onEdit = {
                        routineToEdit = it
                        showEditDialog = true
                    },
                    onDelete = {
                        deleteRoutine(it.id, {
                            routines.remove(it)
                        }, { e -> Log.e("RoutinesPage", "Error deleting routine", e) })
                    }
                )
            }
        }

        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(color = Color(0xFF85C3F2), shape = RoundedCornerShape(8.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF85C3F2))
        ) {
            Text(text = "Add Routine", color = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                Icons.Outlined.AddCircle,
                contentDescription = "Add"
            )
        }
    }

    if (showEditDialog && routineToEdit != null) {
        EditRoutineDialog(
            routine = routineToEdit!!,
            onDismiss = { showEditDialog = false },
            onSave = { updatedRoutine ->
                routineToEdit?.let { originalRoutine ->
                    val index = routines.indexOf(originalRoutine)
                    if (index >= 0) {
                        routines[index] = updatedRoutine
                        updateRoutine(updatedRoutine)
                    }
                }
                showEditDialog = false
            }
        )
    }

    if (showAddDialog) {
        AddRoutineDialog(
            onDismiss = { showAddDialog = false },
            onSave = { newRoutine ->
                addRoutine(newRoutine, {
                    routines.add(it)
                }, { e -> Log.e("RoutinesPage", "Error adding routine", e) })
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddRoutineDialog(
    onDismiss: () -> Unit,
    onSave: (Routine) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val exercises = remember { mutableStateListOf<Exercise>() }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Add Routine") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Routine Name") }
                )
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") }
                )
                exercises.forEachIndexed { index, exercise ->
                    TextField(
                        value = exercise.name,
                        onValueChange = { newName -> exercises[index] = exercises[index].copy(name = newName) },
                        label = { Text("Exercise Name") }
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val newRoutine = Routine(
                    userId = requireUserId(),
                    id = UUID.randomUUID().toString(),
                    name = name,
                    description = description,
                    exercises = exercises.toList()
                )
                onSave(newRoutine)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}