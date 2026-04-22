package ie.setu.project.views.main

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import ie.setu.project.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    title: String, onTitleChange: (String) -> Unit,
    description: String, onDescriptionChange: (String) -> Unit,
    colour: String, onColourChange: (String) -> Unit,
    size: String, onSizeChange: (String) -> Unit,
    season: String, onSeasonChange: (String) -> Unit,
    category: String, onCategoryChange: (String) -> Unit,
    lastWornText: String, onPickLastWorn: () -> Unit,
    imageUri: Uri?, onChooseImage: () -> Unit,
    subCategory: String, onSubCategoryChange: (String) -> Unit,
    isEdit: Boolean, onCancel: () -> Unit, onSave: () -> Unit,
    snackbarHostState: SnackbarHostState,
    removeBg: Boolean, onRemoveBgChange: (Boolean) -> Unit,
    isScanning: Boolean = false,
    onScanImage: () -> Unit = {}
) {
    val seasons = stringArrayResource(id = R.array.seasons_array).toList()
    var seasonExpanded by remember { mutableStateOf(false) }
    val categories = listOf("All", "Tops", "Bottoms", "Dresses", "Shoes", "Jackets")
    val topSubTypes = listOf("Hoodie", "Jumper", "Long Sleeve", "T-Shirt", "Blouse", "Tank Top")
    var subCategoryExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    val saveLabel = if (isEdit) stringResource(R.string.save_clothing_item) else stringResource(R.string.add_clothing)
    val imageButtonLabel = if (imageUri != null) stringResource(R.string.change_clothing_image) else stringResource(R.string.button_addImage)

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("AI Image Scanning") },
            text = { Text("Add an image of your clothing item first, then tap \"Scan Image with AI\" to automatically fill in the title, description, colour and category. You will still need to enter the size, season and last worn date yourself.") },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(painter = painterResource(R.drawable.ic_heart), contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(text = "Add Clothing", fontSize = 30.sp, fontFamily = FontFamily.Cursive)
                        Spacer(Modifier.width(8.dp))
                        Icon(painter = painterResource(R.drawable.ic_heart), contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = "Info", tint = Color.White)
                    }
                    IconButton(onClick = onCancel, modifier = Modifier.size(48.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text(stringResource(R.string.enter_clothing_title)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = !categoryExpanded }) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                    categories.forEach { c ->
                        DropdownMenuItem(
                            text = { Text(c) },
                            onClick = {
                                if (c != "All") onCategoryChange(c)
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            if (category == "Tops") {
                ExposedDropdownMenuBox(
                    expanded = subCategoryExpanded,
                    onExpandedChange = { subCategoryExpanded = !subCategoryExpanded }
                ) {
                    OutlinedTextField(
                        value = subCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Top Style") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subCategoryExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = subCategoryExpanded,
                        onDismissRequest = { subCategoryExpanded = false }
                    ) {
                        topSubTypes.forEach { sub ->
                            DropdownMenuItem(
                                text = { Text(sub) },
                                onClick = { onSubCategoryChange(sub); subCategoryExpanded = false }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text(stringResource(R.string.enter_description)) },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = colour,
                onValueChange = onColourChange,
                label = { Text(stringResource(R.string.enter_colour)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = size,
                onValueChange = onSizeChange,
                label = { Text(stringResource(R.string.enter_size)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(expanded = seasonExpanded, onExpandedChange = { seasonExpanded = !seasonExpanded }) {
                OutlinedTextField(
                    value = season,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.enter_season)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = seasonExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = seasonExpanded, onDismissRequest = { seasonExpanded = false }) {
                    seasons.forEach { s ->
                        DropdownMenuItem(text = { Text(s) }, onClick = { onSeasonChange(s); seasonExpanded = false })
                    }
                }
            }

            OutlinedTextField(
                value = lastWornText,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.enter_lastworn)) },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { TextButton(onClick = onPickLastWorn) { Text("Pick") } }
            )

            Button(onClick = onChooseImage, modifier = Modifier.fillMaxWidth()) { Text(imageButtonLabel) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Remove Background", style = MaterialTheme.typography.bodyLarge)
                    Text("Works best on plain backgrounds", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Switch(
                    checked = removeBg,
                    onCheckedChange = onRemoveBgChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = stringResource(R.string.enter_image),
                    modifier = Modifier.fillMaxWidth().height(240.dp),
                    contentScale = ContentScale.Crop
                )

                if (isScanning) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    "Scanning image…",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    "Detecting title, description & colour",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = onScanImage,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Scan Image with AI")
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No image selected")
                }
            }

            Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) { Text(saveLabel) }
        }
    }
}