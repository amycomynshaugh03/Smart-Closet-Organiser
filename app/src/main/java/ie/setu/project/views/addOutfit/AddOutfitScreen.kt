package ie.setu.project.views.addOutfit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ie.setu.project.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOutfitScreen(
    initialTitle: String, initialDescription: String, initialSeason: String?,
    lastWornText: String, selectedClothingCount: Int, snackbarHostState: SnackbarHostState,
    onPickLastWorn: () -> Unit, onChooseClothing: () -> Unit,
    onSave: (title: String, description: String, season: String) -> Unit,
    showError: (String) -> Unit
) {
    val seasons = stringArrayResource(id = R.array.seasons_array).toList()
    var title by rememberSaveable { mutableStateOf(initialTitle) }
    var description by rememberSaveable { mutableStateOf(initialDescription) }
    var selectedSeason by rememberSaveable { mutableStateOf(initialSeason?.takeIf { it.isNotBlank() } ?: seasons.firstOrNull().orEmpty()) }
    var seasonExpanded by remember { mutableStateOf(false) }

    val clothingButtonText = if (selectedClothingCount > 0)
        "Edit Selection ($selectedClothingCount item${if (selectedClothingCount > 1) "s" else ""} selected)"
    else
        stringResource(R.string.button_addClothing)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Icon(painter = androidx.compose.ui.res.painterResource(R.drawable.ic_heart), contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text(text = "Outfits", fontSize = 30.sp, fontFamily = FontFamily.Cursive, color = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Icon(painter = androidx.compose.ui.res.painterResource(R.drawable.ic_heart), contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(value = title, onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.enter_clothing_title)) }, singleLine = true)
            OutlinedTextField(value = description, onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.enter_description)) }, minLines = 3)

            ExposedDropdownMenuBox(expanded = seasonExpanded, onExpandedChange = { seasonExpanded = !seasonExpanded }) {
                OutlinedTextField(value = selectedSeason, onValueChange = {}, readOnly = true, label = { Text("Season") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = seasonExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth())
                ExposedDropdownMenu(expanded = seasonExpanded, onDismissRequest = { seasonExpanded = false }) {
                    seasons.forEach { season ->
                        DropdownMenuItem(text = { Text(season) }, onClick = { selectedSeason = season; seasonExpanded = false })
                    }
                }
            }

            OutlinedTextField(value = lastWornText, onValueChange = {},
                readOnly = true, label = { Text(stringResource(R.string.enter_lastworn)) },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { TextButton(onClick = onPickLastWorn) { Text("Pick") } })

            Button(
                onClick = onChooseClothing,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(clothingButtonText)
            }

            Button(
                onClick = {
                    if (title.isBlank()) showError("Please enter a title")
                    else onSave(title, description, selectedSeason)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.save_outfit)) }
        }
    }
}