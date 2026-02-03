package ie.setu.project.activities

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ie.setu.project.R
import ie.setu.project.models.clothing.ClosetOrganiserModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectClothingScreen(
    clothingItems: List<ClosetOrganiserModel>,
    selectedItems: List<ClosetOrganiserModel>,
    onToggle: (ClosetOrganiserModel, Boolean) -> Unit,
    onDelete: (ClosetOrganiserModel) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    val selectedCount = selectedItems.size

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_heart),
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Select Clothing",
                                fontSize = 28.sp,
                                fontFamily = FontFamily.Cursive,
                                color = Color.White
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                painter = painterResource(R.drawable.ic_heart),
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        Text(
                            text = "$selectedCount selected",
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6200EE),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Surface(tonalElevation = 6.dp) {
                Button(
                    onClick = onSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Save Selection")
                }
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(clothingItems, key = { it.id }) { item ->
                val isChecked = selectedItems.any { it.id == item.id }

                SelectClothingRow(
                    item = item,
                    checked = isChecked,
                    onCheckedChange = { checked -> onToggle(item, checked) },
                    onDelete = { onDelete(item) }
                )
            }
        }
    }
}

@Composable
private fun SelectClothingRow(
    item: ClosetOrganiserModel,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange
            )

            Spacer(Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.title.ifBlank { "No title" })
                Text(
                    text = item.description.ifBlank { "No description" },
                    style = MaterialTheme.typography.bodySmall
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.Red
                )
            }
        }
    }
}
