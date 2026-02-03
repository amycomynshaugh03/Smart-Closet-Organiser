package ie.setu.project.views.outfit

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import ie.setu.project.R
import ie.setu.project.models.outfit.OutfitModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutfitScreen(
    outfitsProvider: () -> List<OutfitModel>,
    onBack: () -> Unit,
    onAddOutfit: () -> Unit,
    onOutfitClick: (OutfitModel) -> Unit,
    onDeleteOutfit: (OutfitModel) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var outfits by remember { mutableStateOf(outfitsProvider()) }

    fun reload() {
        outfits = outfitsProvider()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_heart),
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Outfits",
                            fontSize = 30.sp,
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
                },


                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },

                actions = {
                    IconButton(
                        onClick = onAddOutfit,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add outfit",
                            tint = Color.White
                        )
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6200EE),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->

        if (outfits.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No outfits yet.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(outfits, key = { it.id }) { outfit ->
                    OutfitRow(
                        outfit = outfit,
                        onClick = { onOutfitClick(outfit) },
                        onDelete = {
                            onDeleteOutfit(outfit)
                            reload()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun OutfitRow(
    outfit: OutfitModel,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = outfit.title.ifBlank { "No title" },
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete outfit",
                        tint = Color.Red
                    )
                }
            }

            // Horizontal image strip
            val scroll = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .horizontalScroll(scroll),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                outfit.clothingItems.forEach { clothing ->
                    val uri = clothing.image
                    if (uri != null && uri.toString().isNotBlank()) {
                        AsyncImage(
                            model = uri,
                            contentDescription = "Clothing image",
                            modifier = Modifier
                                .height(90.dp)
                                .width(90.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}
