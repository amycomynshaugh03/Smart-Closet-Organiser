package ie.setu.project.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    vm: ProfileViewModel = hiltViewModel()
) {
    val profile by vm.profile.collectAsState()

    LaunchedEffect(Unit) { vm.loadProfile() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("My Profile", fontSize = 26.sp, fontFamily = FontFamily.Cursive, color = Color.White)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                val photoModel: Any? = profile.photoUrl.takeIf { it.isNotBlank() }
                if (photoModel != null) {
                    AsyncImage(
                        model = photoModel,
                        contentDescription = "Profile photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }

            val displayName = profile.displayName.takeIf { it.isNotBlank() } ?: "User"
            Text(displayName, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))

            ProfileInfoCard(
                icon = { Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp)) },
                label = "Email",
                value = profile.email.ifBlank { "Not available" }
            )

            if (profile.bio.isNotBlank()) {
                ProfileInfoCard(
                    icon = { Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp)) },
                    label = "Bio",
                    value = profile.bio
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onEditProfile,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("Edit Profile", fontSize = 16.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun ProfileInfoCard(icon: @Composable () -> Unit, label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            icon()
            Spacer(Modifier.width(12.dp))
            Column {
                Text(label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}