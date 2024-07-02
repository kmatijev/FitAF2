package com.example.fitaf

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
        }
    }
}

@Composable
fun MyApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, route = "app", startDestination = if (Firebase.auth.currentUser == null) "login" else "workout") {
        composable("login") {
            LoginPage(navController)
        }
        composable("register") {
            RegisterPage(navController)
        }
        composable("routines") {
            RoutinesPage(navController)
        }
        composable("workout") {
            WorkoutPage(navController)
        }
        composable(
            "routine/{routineId}",
            arguments = listOf(navArgument("routineId") { type = NavType.StringType })
        ) {
            var routine by remember { mutableStateOf<Routine?>(null) }
            LaunchedEffect(Unit) {
                getRoutine(it.arguments!!.getString("routineId")!!) {
                    routine = it
                }
            }

            RoutineAddExercisesPage(
                navController,
                routine,
                onExerciseAdd = { exercise ->
                    val currRoutine = routine ?: return@RoutineAddExercisesPage
                    val newRoutine = currRoutine.copy(exercises = currRoutine.exercises.toMutableList().apply { add(exercise) }.toList())
                    editRoutine(newRoutine) { routine = newRoutine }
                }
            )
        }
        composable("routine_picker") {
            var routines by remember { mutableStateOf<List<Routine>?>(null) }
            LaunchedEffect(Unit) {
                getAllRoutines { routines = it }
            }
            LogWorkoutRoutinePickerPage(
                routines = routines,
                onRoutineClick = {
                    navController.navigate("routine_logger/${it.id}")
                }
            )
        }
        composable(
            "routine_logger/{routineId}",
            arguments = listOf(navArgument("routineId") { type = NavType.StringType })
        ) {
            var startingRoutine by remember { mutableStateOf<Routine?>(null) }
            var routine by remember { mutableStateOf<Routine?>(null) }
            LaunchedEffect(Unit) {
                getRoutine(it.arguments!!.getString("routineId")!!) {
                    startingRoutine = it
                    routine = it
                }
            }
            RoutineLoggerPage(
                routine = routine,
                onExerciseSave = { newExercise ->
                    val localRoutine = routine;
                    if (localRoutine != null) {
                        routine = localRoutine.copy(
                            exercises = localRoutine.exercises.toMutableList().apply {
                                replaceAll {
                                    if (it.id == newExercise.id) newExercise
                                    else it
                                }
                            }
                        )
                    }
                },
                onRoutineSave = {
                    val localEditedRoutine = routine
                    val localStartingRoutine = startingRoutine

                    if (localEditedRoutine != null && localStartingRoutine != null ) {
                        val editedExercises = localEditedRoutine.exercises
                        val adjustedRoutine = localStartingRoutine.copy(
                            exercises = localStartingRoutine.exercises.mapIndexed { idx, startingExercise ->
                                val startingToEditedExerciseData = mapOf(
                                    startingExercise.sets to editedExercises[idx].sets,
                                    startingExercise.reps to editedExercises[idx].reps,
                                    startingExercise.weight to editedExercises[idx].weight
                                )
                                val bumpFactor = if (startingToEditedExerciseData.all { it.key == it.value }) {
                                    1.1
                                } else if (startingToEditedExerciseData.any { it.key < it.value }) {
                                    1.2
                                } else {
                                    0.9
                                }
                                startingExercise.copy(weight = (startingExercise.weight * bumpFactor).toInt())
                            }
                        )
                        editRoutine(adjustedRoutine) {
                            navController.popBackStack(route = "workout", inclusive = false)
                        }
                    }
                }
            )
        }
    }
}

private fun getRoutine(id: String, onSuccess: (Routine) -> Unit) {
    FirebaseFirestore
        .getInstance()
        .collection("routines")
        .document(id)
        .get()
        .addOnSuccessListener { onSuccess(it.toObject(Routine::class.java)!!); }
}

private fun editRoutine(routine: Routine, onSuccess: () -> Unit) {
    FirebaseFirestore
        .getInstance()
        .collection("routines")
        .document(routine.id)
        .set(routine)
        .addOnSuccessListener { onSuccess() }
}

fun requireUserId() = Firebase.auth.currentUser!!.uid

fun getAllRoutines(onSuccess: (List<Routine>) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("routines")
        .whereEqualTo("userId", requireUserId())
        .get()
        .addOnSuccessListener { result ->
            onSuccess(result.documents.map { it.toObject(Routine::class.java)!! })
        }
}

fun addRoutine(routine: Routine, onSuccess: (Routine) -> Unit, onFailure: (Exception) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("routines")
        .document(routine.id)
        .set(routine)
        .addOnSuccessListener { onSuccess(routine) }
        .addOnFailureListener { e -> onFailure(e) }
}

fun deleteRoutine(routineId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("routines")
        .document(routineId)
        .delete()
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { e -> onFailure(e) }
}

fun updateRoutine(routine: Routine) {
    val db = FirebaseFirestore.getInstance()
    db.collection("routines").document(routine.id)
        .set(routine)
        .addOnSuccessListener {
            Log.d("RoutinesPage", "Routine updated successfully")
        }
        .addOnFailureListener { e ->
            Log.e("RoutinesPage", "Error updating routine", e)
        }
}