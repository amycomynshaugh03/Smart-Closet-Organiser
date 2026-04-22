package ie.setu.project.ui.auth

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import ie.setu.project.R
import ie.setu.project.firebase.auth.Response
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onGoToRegister: () -> Unit,
    onSignedIn: () -> Unit,
    vm: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showResetDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }

    val state by vm.authResponse.collectAsState()
    val resetState by vm.resetResponse.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val webClientId = stringResource(R.string.default_web_client_id)

    LaunchedEffect(state) {
        if (state is Response.Success) {
            vm.clearAuthResponse()
            onSignedIn()
        }
    }

    LaunchedEffect(resetState) {
        if (resetState is Response.Success) {
            vm.clearResetResponse()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Smart Closet Organiser",
            fontSize = 32.sp,
            fontFamily = FontFamily.Cursive,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text("Sign in to your closet", fontSize = 14.sp, color = Color.Gray)

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(8.dp))

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            TextButton(onClick = { resetEmail = email; showResetDialog = true }) {
                Text("Forgot Password?", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { vm.signIn(email.trim(), password) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Sign In", fontSize = 16.sp)
        }

        Spacer(Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text("  or  ", color = Color.Gray, fontSize = 13.sp)
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                scope.launch {
                    try {
                        val credentialManager = CredentialManager.create(context)
                        val googleIdOption = GetGoogleIdOption.Builder()
                            .setFilterByAuthorizedAccounts(false)
                            .setServerClientId(webClientId)
                            .build()
                        val request = GetCredentialRequest.Builder()
                            .addCredentialOption(googleIdOption).build()
                        val result = credentialManager.getCredential(request = request, context = context)
                        val credential = result.credential
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        vm.signInWithGoogle(googleIdTokenCredential.idToken)
                    } catch (e: GetCredentialException) {
                    } catch (e: Exception) {
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_google_logo),
                contentDescription = "Google logo",
                modifier = Modifier.size(20.dp),
                tint = Color.Unspecified
            )
            Spacer(Modifier.width(8.dp))
            Text("Continue with Google", fontSize = 16.sp, color = Color(0xFF4285F4))
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = onGoToRegister) {
            Text("Don't have an account? Create one", color = MaterialTheme.colorScheme.primary)
        }

        Spacer(Modifier.height(12.dp))

        when (val s = state) {
            is Response.Loading -> CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            is Response.Failure -> Text(
                "Error: ${s.e.message ?: "Unknown error"}",
                color = Color.Red,
                fontSize = 13.sp
            )
            else -> {}
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false; vm.clearResetResponse() },
            title = { Text("Reset Password") },
            text = {
                Column {
                    Text("Enter your email address and we will send you a reset link.")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    when (resetState) {
                        is Response.Failure -> Text(
                            "Something went wrong. Please try again.",
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                        is Response.Success -> Text(
                            "Reset email sent! Check your inbox.",
                            color = Color(0xFF2E7D32),
                            fontSize = 12.sp
                        )
                        else -> {}
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { vm.resetPassword(resetEmail.trim()) }) {
                    if (resetState is Response.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text("Send Reset Email")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false; vm.clearResetResponse() }) {
                    Text("Cancel")
                }
            }
        )
    }
}