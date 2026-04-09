package ie.setu.project.views.clothing

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import ie.setu.project.R
import ie.setu.project.models.clothing.ClosetOrganiserModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClothingScreen(
    items: List<ClosetOrganiserModel>,
    onAddClick: () -> Unit,
    onBackToHome: () -> Unit,
    onItemClick: (ClosetOrganiserModel) -> Unit,
    onDeleteClick: (ClosetOrganiserModel) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val categories = listOf("All", "Tops", "Bottoms", "Dress", "Shoes", "Jackets")
    var selectedCategory by rememberSaveable { mutableStateOf("All") }

    val filteredItems =
        if (selectedCategory == "All") items
        else items.filter { it.category.trim().equals(selectedCategory, ignoreCase = true) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            Icon(painter = painterResource(R.drawable.ic_heart), contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text(text = "Clothing", fontSize = 30.sp, fontFamily = FontFamily.Cursive, color = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Icon(painter = painterResource(R.drawable.ic_heart), contentDescription = null, tint = Color.White)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackToHome) {
                            Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = onAddClick, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Default.Add, "Add", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        categories.forEach { cat ->
                            FilterChip(selected = selectedCategory == cat, onClick = { selectedCategory = cat }, label = { Text(cat) })
                        }
                    }
                    Text(text = "Selected: $selectedCategory", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                }
            }
        }
    ) { padding ->
        if (filteredItems.isEmpty()) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(if (selectedCategory == "All") "No items." else "No items in $selectedCategory.")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredItems, key = { it.id }) { item ->
                    ClothingRow(item = item, onClick = { onItemClick(item) }, onDelete = { onDeleteClick(item) })
                }
            }
        }
    }
}

@Composable
private fun ClothingRow(item: ClosetOrganiserModel, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            val imageModel: Any? = item.imageUrl.takeIf { it.isNotBlank() } ?: item.image
            val ok = imageModel != null && imageModel != Uri.EMPTY && imageModel.toString().isNotBlank()
            if (ok) {
                AsyncImage(model = imageModel, contentDescription = "Clothing image", modifier = Modifier.size(64.dp))
            } else {
                Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.Center) {
                    Text("No image", color = Color.Gray)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title.ifBlank { "No title" }, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Text(item.description.ifBlank { "No description" }, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(4.dp))
                Text("Category: ${item.category.ifBlank { "None" }}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete", tint = Color.Red)
            }
        }
    }
}