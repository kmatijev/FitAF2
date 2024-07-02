package com.example.fitaf

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID



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
    NavHost(navController = navController, startDestination = if (Firebase.auth.currentUser == null) "login" else "workout") {
        composable("login") { LoginPage(navController) }
        composable("register") { RegisterPage(navController) }
        composable("routines") { RoutinesPage(navController) }
        composable("workout") { WorkoutPage(navController) }
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

            RoutinePage(
                navController,
                routine,
                onExerciseAdd = { exercise ->
                    val currRoutine = routine ?: return@RoutinePage
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
        //composable("tracking/{routineId}") { backStackEntry ->
        //    val routineId = backStackEntry.arguments?.getString("routineId") ?: ""
        //    WorkoutTrackingPage(navController, routineId)
        //}
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

@Composable
fun RegisterPage(navController: NavHostController) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var registerError by remember { mutableStateOf(false) }
    val auth: FirebaseAuth = Firebase.auth

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Register",
            color = Color(0xFF3271A1),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineSmall,
            fontStyle = FontStyle.Italic
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = email,
            onValueChange = { email = it },
            label = { Text(text = "E-mail") },
            placeholder = { Text(text = "Type e-mail here") },
            shape = RoundedCornerShape(percent = 20),
            isError = !(email.isNotEmpty() && email.contains("@") && email.endsWith(".com"))
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = password,
            onValueChange = { password = it },
            label = { Text(text = "Password") },
            singleLine = true,
            placeholder = { Text(text = "Type password here") },
            shape = RoundedCornerShape(percent = 20),
            isError = password.length < 6,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible)
                    R.drawable.visibility
                else R.drawable.visibility_off

                // Localized description for accessibility services
                val description = if (passwordVisible) "Hide password" else "Show password"

                // Toggle button to hide or display password
                IconButton(onClick = {passwordVisible = !passwordVisible}){
                    Icon(imageVector  = ImageVector.vectorResource(id =image), description)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (registerError) {
            Text(
                text = "Registration failed. Please try again.",
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (email.isNotEmpty() && email.contains("@") && email.endsWith(".com") && password.length >= 6) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            registerError = !task.isSuccessful
                            if (task.isSuccessful) {
                                navController.navigate("workout")
                            }
                        }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3271A1))
        ) {
            Text(text = "Register", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate("login") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
        ) {
            Text(text = "Back to Login", color = Color.White)
        }
    }
}

@Composable
fun LoginPage(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf(false) }
    val auth: FirebaseAuth = Firebase.auth


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        //horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Let's get...",
            color = Color(0xFF3271A1),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineSmall,
            fontStyle = FontStyle.Italic
        )

        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .padding(8.dp)
                .clip(RoundedCornerShape(64.dp))
                .background(Color(0xFF9A7EBF))
                .padding(16.dp) // Dodano unutarnje padding
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        )
        {
            Text(
                text = "FitAF!",
                color = Color(0xFF1113A6),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.displayLarge
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth(),
            value = email,
            onValueChange = { newText ->
                email = newText
            },
            label = {
                Text(text = "E-mail")
            },
            placeholder = { Text(text = "Type e-mail here") },
            shape = RoundedCornerShape(percent = 20),
            isError = !(email.isNotEmpty() && email.contains("@") && email.endsWith(".com"))
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = password,
            onValueChange = { password = it },
            label = { Text(text = "Password") },
            singleLine = true,
            placeholder = { Text(text = "Type password here") },
            shape = RoundedCornerShape(percent = 20),
            isError = password.length < 6,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible)
                    R.drawable.visibility
                else R.drawable.visibility_off

                // Localized description for accessibility services
                val description = if (passwordVisible) "Hide password" else "Show password"

                // Toggle button to hide or display password
                IconButton(onClick = {passwordVisible = !passwordVisible}){
                    Icon(imageVector  = ImageVector.vectorResource(id =image), description)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (loginError) {
            Text(
                text = "Invalid email or password",
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            loginError = !task.isSuccessful
                            if (task.isSuccessful) {
                                navController.navigate("workout")
                            }
                    }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3271A1))
        ) {
            Text(text = "Login", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                navController.navigate("register")

            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
        ) {
            Text(text = "Register", color = Color.White)
        }
    }
}

@Composable
fun CreateRoutineDialog(
    onDismiss: () -> Unit,
    onCreateRoutine: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Create New Routine") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Routine Name") },
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(

                onClick = {
                    onCreateRoutine(name, description)
                    onDismiss()
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() }
            ) {
                Text("Cancel")
            }
        }
    )
}

private fun getAllRoutines(onSuccess: (List<Routine>) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("routines")
        .get()
        .addOnSuccessListener { result ->
            onSuccess(result.documents.map { it.toObject(Routine::class.java)!! })
        }
}

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
        Text(text = "Routines", style = MaterialTheme.typography.headlineLarge)

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

fun addRoutine(routine: Routine, onSuccess: (Routine) -> Unit, onFailure: (Exception) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("routines")
        .document(routine.id)
        .set(routine)
        .addOnSuccessListener { onSuccess(routine) }
        .addOnFailureListener { e -> onFailure(e) }
}

@Composable
fun RoutineItem(
    routine: Routine,
    onClick: () -> Unit,
    onEdit: (Routine) -> Unit,
    onDelete: (Routine) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.LightGray, RoundedCornerShape(8.dp))
            .padding(16.dp)
            .clickable { onClick() }
    ) {
        Text(text = routine.name, style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = routine.description, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))

        routine.exercises.forEach { exercise ->
            Text(
                text = "Exercise: ${exercise.name} ${exercise.sets}x${exercise.reps}x${exercise.weight}kg",
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = { onEdit(routine) }) {
                Icon(ImageVector.vectorResource(id = R.drawable.edit), contentDescription = "Edit Routine")
            }
            IconButton(onClick = { onDelete(routine) }) {
                Icon(ImageVector.vectorResource(id = R.drawable.delete), contentDescription = "Delete Routine")
            }
        }
    }
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

@Composable
fun EditRoutineDialog(
    routine: Routine,
    onDismiss: () -> Unit,
    onSave: (Routine) -> Unit
) {
    var name by remember { mutableStateOf(routine.name) }
    var description by remember { mutableStateOf(routine.description) }
    val exercises = remember { mutableStateListOf(*routine.exercises.toTypedArray()) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Edit Routine") },
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
                val updatedRoutine = Routine(routine.id, name, description, exercises.toList())
                onSave(updatedRoutine)
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


fun deleteAssociatedExercises(userId: String, routineId: String) {
    val firestore = FirebaseFirestore.getInstance()
    val exercisesRef = firestore.collection("users").document(userId)
        .collection("routines").document(routineId).collection("exercises")

    // Delete all exercises in the subcollection
    exercisesRef.get()
        .addOnSuccessListener { querySnapshot ->
            val batch = firestore.batch()
            querySnapshot.documents.forEach { document ->
                batch.delete(document.reference)
            }
            batch.commit()
                .addOnSuccessListener {
                    // Exercises deleted successfully
                }
                .addOnFailureListener { exception ->
                    // Handle failure
                    //Log.e(TAG, "Error deleting exercises", exception)
                }
        }
        .addOnFailureListener { exception ->
            // Handle failure
            //Log.e(TAG, "Error retrieving exercises", exception)
        }
}

fun addExercise(userId: String, routineId: String, exercise: Exercise) {
    val firestore = FirebaseFirestore.getInstance()
    firestore.collection("users").document(userId).collection("routines")
        .document(routineId).collection("exercises")
        .add(exercise)
        .addOnSuccessListener { documentReference ->
            // Exercise added successfully
        }
        .addOnFailureListener { exception ->
            // Handle failure
        }
}

fun deleteExercise(userId: String, routineId: String, exerciseId: String) {
    val firestore = FirebaseFirestore.getInstance()
    firestore.collection("users").document(userId).collection("routines")
        .document(routineId).collection("exercises")
        .document(exerciseId)
        .delete()
        .addOnSuccessListener {
            // Exercise deleted successfully
        }
        .addOnFailureListener { exception ->
            // Handle failure
        }
}


@Composable
fun ExerciseItem(exercise: Exercise, index: Int, onDelete: () -> Unit, onEdit: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = "${index + 1}. ${exercise.name}")
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Button(
                onClick = onDelete,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text("Delete")
            }
            Button(
                onClick = onEdit
            ) {
                Text("Edit")
            }
        }
    }
}

@Composable
fun ExerciseManagementPage(
    routine: Routine,
    onAddExercise: () -> Unit,
    onEditExercise: (Exercise) -> Unit,
    onDeleteExercise: (Exercise) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Routine: ${routine.name}",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Button to add new exercise
        Button(
            onClick = onAddExercise,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text("Add Exercise")
        }
    }
}

@Composable
fun WorkoutPage(navController: NavHostController) {
    // Hardcoded list of routines for demonstration
    val routines = listOf("Routine 1", "Routine 2", "Routine 3", "Routine 4")
    var selectedRoutine by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color.Black, thickness = 1.dp)
        Text(
            text = "Workout",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(16.dp)
        )

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            thickness = 1.dp,
            color = Color.Black
        )

        Text(
            text = "Quick start:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Button(
            onClick = { navController.navigate("routine_picker") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(color = Color(0xFF85C3F2), shape = RoundedCornerShape(8.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF85C3F2))
        ) {
            Text(text = "Log a workout", color = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                Icons.Outlined.AddCircle,
                contentDescription = "Add"
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Routines:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Button(
            onClick = { navController.navigate("routines") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(color = Color(0xFF85C3F2), shape = RoundedCornerShape(8.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF85C3F2))
        ) {
            Text(text = "Create routine", color = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                Icons.Outlined.AddCircle,
                contentDescription = "Add"
            )
        }
    }
    }
