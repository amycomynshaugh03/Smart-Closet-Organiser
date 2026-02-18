package ie.setu.project.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ie.setu.project.firebase.auth.Response

@Composable
fun RegisterScreen(
    onGoToLogin: () -> Unit,
    onRegistered: () -> Unit,
    vm: AuthViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val state by vm.authResponse.collectAsState()

    LaunchedEffect(state) {
        if (state is Response.Success) {
            vm.clearAuthResponse()
            onRegistered()
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Register", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { vm.signUp(name.trim(), email.trim(), password) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Account")
        }

        Spacer(Modifier.height(12.dp))

        TextButton(onClick = onGoToLogin) {
            Text("Back to login")
        }

        Spacer(Modifier.height(12.dp))

        when (val s = state) {
            is Response.Loading -> CircularProgressIndicator()
            is Response.Failure -> Text("Error: ${s.e.message ?: "Unknown error"}")
            else -> {}
        }
    }
}
