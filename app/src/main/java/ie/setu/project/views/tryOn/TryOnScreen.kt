package ie.setu.project.views.tryOn

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import ie.setu.project.R
import ie.setu.project.models.clothing.ClosetOrganiserModel
import ie.setu.project.models.outfit.OutfitModel
import kotlinx.coroutines.launch


data class OutfitSlot(
    val label: String,
    val category: String,
    val iconRes: Int
)

val OUTFIT_SLOTS = listOf(
    OutfitSlot("Top",    "Tops",    R.drawable.ic_heart),
    OutfitSlot("Bottom", "Bottoms", R.drawable.ic_heart),
    OutfitSlot("Jacket", "Jackets", R.drawable.ic_heart),
    OutfitSlot("Shoes",  "Shoes",   R.drawable.ic_heart)
)


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TryOnScreen(
    clothingItems: List<ClosetOrganiserModel>,
    savedOutfits: List<OutfitModel>,
    onSaveOutfit: (String, List<ClosetOrganiserModel>) -> Unit,
    onBack: () -> Unit
) {
    val selectedItems   = remember { mutableStateMapOf<Int, ClosetOrganiserModel>() }
    var activeSlotIndex by remember { mutableIntStateOf(0) }
    var showSaveDialog  by remember { mutableStateOf(false) }
    var outfitName      by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope             = rememberCoroutineScope()
    var showSavedPanel  by remember { mutableStateOf(false) }

    val activeSlot    = OUTFIT_SLOTS[activeSlotIndex]
    val filteredItems = remember(clothingItems, activeSlotIndex) {
        clothingItems.filter { it.category.equals(activeSlot.category, ignoreCase = true) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                title = {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier              = Modifier.fillMaxWidth()
                    ) {
                        Icon(painter = painterResource(R.drawable.ic_heart), contentDescription = null,
                            tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Virtual Try-On", fontSize = 26.sp,
                            fontFamily = FontFamily.Cursive, color = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Icon(painter = painterResource(R.drawable.ic_heart), contentDescription = null,
                            tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                },
                actions = {
                    IconButton(onClick = { showSavedPanel = !showSavedPanel }) {
                        Icon(Icons.Default.Bookmarks, contentDescription = "Saved Looks", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { padding ->

        Column(modifier = Modifier.fillMaxSize().padding(padding)) {


            AnimatedVisibility(
                visible = showSavedPanel,
                enter   = slideInVertically(tween(300)) { -it } + fadeIn(),
                exit    = slideOutVertically(tween(300)) { -it } + fadeOut()
            ) {
                SavedLooksPanel(outfits = savedOutfits, onLoadOutfit = { outfit ->
                    selectedItems.clear()
                    outfit.clothingItems.forEach { item ->
                        val idx = OUTFIT_SLOTS.indexOfFirst { s ->
                            s.category.equals(item.category, ignoreCase = true)
                        }
                        if (idx >= 0) selectedItems[idx] = item
                    }
                    showSavedPanel = false
                    scope.launch { snackbarHostState.showSnackbar("Outfit loaded!") }
                })
            }

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {


                VirtualModelCanvas(
                    selectedItems   = selectedItems,
                    activeSlotIndex = activeSlotIndex,
                    onSlotClick     = { activeSlotIndex = it }
                )

                Spacer(Modifier.height(8.dp))


                SlotSelectorTabs(
                    slots           = OUTFIT_SLOTS,
                    activeSlotIndex = activeSlotIndex,
                    selectedItems   = selectedItems,
                    onSlotSelected  = { activeSlotIndex = it }
                )

                Spacer(Modifier.height(12.dp))


                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Choose ${activeSlot.label}",
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                    Text(text = "${filteredItems.size} items",
                        style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }

                Spacer(Modifier.height(8.dp))


                if (filteredItems.isEmpty()) {
                    EmptySlotMessage(categoryName = activeSlot.category)
                } else {
                    ClothingPictureSlider(
                        items          = filteredItems,
                        selectedItem   = selectedItems[activeSlotIndex],
                        onItemSelected = { item ->
                            if (selectedItems[activeSlotIndex]?.id == item.id)
                                selectedItems.remove(activeSlotIndex)
                            else
                                selectedItems[activeSlotIndex] = item
                        }
                    )
                }

                Spacer(Modifier.height(16.dp))


                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { selectedItems.clear() }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Clear All")
                    }
                    Button(
                        onClick = {
                            if (selectedItems.isEmpty())
                                scope.launch { snackbarHostState.showSnackbar("Select at least one item first!") }
                            else { outfitName = ""; showSaveDialog = true }
                        },
                        modifier = Modifier.weight(1f),
                        colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Save Look")
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }


        if (showSaveDialog) {
            AlertDialog(
                onDismissRequest = { showSaveDialog = false },
                title = { Text("Save This Look") },
                text = {
                    Column {
                        Text("Give your outfit a name:", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = outfitName, onValueChange = { outfitName = it },
                            placeholder = { Text("e.g. Summer Casual") }, singleLine = true,
                            modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val name = outfitName.trim().ifBlank { "My Look" }
                        onSaveOutfit(name, selectedItems.values.toList())
                        showSaveDialog = false
                        scope.launch { snackbarHostState.showSnackbar("Outfit \"$name\" saved!") }
                    }) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { showSaveDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}


@Composable
fun VirtualModelCanvas(
    selectedItems: Map<Int, ClosetOrganiserModel>,
    activeSlotIndex: Int,
    onSlotClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape  = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {

            Text("Your Look", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))

            if (selectedItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(12.dp))
                    .background(Brush.verticalGradient(listOf(
                        MaterialTheme.colorScheme.primary.copy(0.07f),
                        MaterialTheme.colorScheme.primary.copy(0.15f)))),
                    contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Person, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(0.4f),
                            modifier = Modifier.size(56.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Select clothing below\nto build your look",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary.copy(0.6f),
                            textAlign = TextAlign.Center)
                    }
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically) {
                    OUTFIT_SLOTS.forEachIndexed { idx, slot ->
                        OutfitSlotPreviewCard(
                            slot     = slot,
                            item     = selectedItems[idx],
                            isActive = idx == activeSlotIndex,
                            onClick  = { onSlotClick(idx) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            val count = selectedItems.size
            val total = OUTFIT_SLOTS.size
            LinearProgressIndicator(
                progress    = { count.toFloat() / total },
                modifier    = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                color       = MaterialTheme.colorScheme.primary,
                trackColor  = MaterialTheme.colorScheme.primary.copy(0.15f)
            )
            Spacer(Modifier.height(4.dp))
            Text("$count / $total slots filled",
                style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}


@Composable
fun OutfitSlotPreviewCard(
    slot: OutfitSlot, item: ClosetOrganiserModel?,
    isActive: Boolean, onClick: () -> Unit
) {
    val borderAlpha by animateFloatAsState(if (isActive) 1f else 0.3f, tween(200), label = "ba")
    val elevation   by animateDpAsState(if (isActive) 6.dp else 2.dp,  tween(200), label = "el")
    val hasItem     = item != null
    val imageModel: Any? = item?.imageUrl?.takeIf { it.isNotBlank() } ?: item?.image

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(72.dp)) {
        Box(modifier = Modifier.size(72.dp).shadow(elevation, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(if (hasItem) Color.White else MaterialTheme.colorScheme.primary.copy(0.08f))
            .border(if (isActive) 2.dp else 1.dp,
                MaterialTheme.colorScheme.primary.copy(borderAlpha), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
            contentAlignment = Alignment.Center) {

            if (hasItem && imageModel != null && imageModel.toString().isNotBlank() && imageModel != Uri.EMPTY) {
                AsyncImage(model = imageModel, contentDescription = item!!.title,
                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else if (hasItem) {
                Box(modifier = Modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.primary.copy(0.15f)),
                    contentAlignment = Alignment.Center) {
                    Text(item!!.title.take(2).uppercase(), fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary)
                }
            } else {
                Icon(Icons.Default.Add, contentDescription = "Add ${slot.label}",
                    tint = MaterialTheme.colorScheme.primary.copy(0.5f), modifier = Modifier.size(28.dp))
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(text = if (hasItem) item!!.title else slot.label,
            style = MaterialTheme.typography.labelSmall, maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color      = if (isActive) MaterialTheme.colorScheme.primary else Color.Gray,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal)
    }
}


@Composable
fun SlotSelectorTabs(
    slots: List<OutfitSlot>, activeSlotIndex: Int,
    selectedItems: Map<Int, ClosetOrganiserModel>, onSlotSelected: (Int) -> Unit
) {
    LazyRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)) {
        itemsIndexed(slots) { index, slot ->
            val isActive = index == activeSlotIndex
            val hasItem  = selectedItems.containsKey(index)
            FilterChip(
                selected = isActive,
                onClick  = { onSlotSelected(index) },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (hasItem) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape)
                                .background(if (isActive) Color.White else MaterialTheme.colorScheme.primary))
                            Spacer(Modifier.width(4.dp))
                        }
                        Text(slot.label)
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor     = Color.White)
            )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClothingPictureSlider(
    items: List<ClosetOrganiserModel>,
    selectedItem: ClosetOrganiserModel?,
    onItemSelected: (ClosetOrganiserModel) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { items.size })
    val scope      = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxWidth()) {

        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth()) { page ->
                ClothingSliderCard(
                    item       = items[page],
                    isSelected = selectedItem?.id == items[page].id,
                    onClick    = { onItemSelected(items[page]) }
                )
            }

            if (pagerState.currentPage > 0) {
                IconButton(
                    onClick  = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                    modifier = Modifier.align(Alignment.CenterStart)
                        .background(Color.Black.copy(0.4f), CircleShape).size(36.dp)
                ) { Icon(Icons.Default.ArrowBack, "Previous", tint = Color.White, modifier = Modifier.size(18.dp)) }
            }

            if (pagerState.currentPage < items.lastIndex) {
                IconButton(
                    onClick  = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                    modifier = Modifier.align(Alignment.CenterEnd)
                        .background(Color.Black.copy(0.4f), CircleShape).size(36.dp)
                ) { Icon(Icons.Default.ArrowForward, "Next", tint = Color.White, modifier = Modifier.size(18.dp)) }
            }
        }

        Spacer(Modifier.height(10.dp))


        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            repeat(items.size) { index ->
                Box(modifier = Modifier.padding(3.dp)
                    .size(if (index == pagerState.currentPage) 10.dp else 6.dp)
                    .clip(CircleShape)
                    .background(if (index == pagerState.currentPage)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.primary.copy(0.3f))
                    .clickable { scope.launch { pagerState.animateScrollToPage(index) } })
            }
        }

        Spacer(Modifier.height(10.dp))


        LazyRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)) {
            itemsIndexed(items) { index, item ->
                ThumbnailItem(
                    item          = item,
                    isCurrentPage = pagerState.currentPage == index,
                    isSelected    = selectedItem?.id == item.id,
                    onClick       = {
                        scope.launch { pagerState.animateScrollToPage(index) }
                        onItemSelected(item)
                    }
                )
            }
        }
    }
}


@Composable
fun ClothingSliderCard(item: ClosetOrganiserModel, isSelected: Boolean, onClick: () -> Unit) {
    val imageModel: Any? = item.imageUrl.takeIf { it.isNotBlank() } ?: item.image
    val validImage = imageModel != null && imageModel != Uri.EMPTY && imageModel.toString().isNotBlank()

    Card(modifier = Modifier.fillMaxWidth().height(220.dp)
        .border(if (isSelected) 3.dp else 0.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
            RoundedCornerShape(16.dp))
        .clickable(onClick = onClick),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(if (isSelected) 8.dp else 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (validImage) {
                AsyncImage(model = imageModel, contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(
                    MaterialTheme.colorScheme.primary.copy(0.15f),
                    MaterialTheme.colorScheme.primary.copy(0.25f)))),
                    contentAlignment = Alignment.Center) {
                    Text(item.title.take(2).uppercase(), fontSize = 40.sp,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary.copy(0.5f))
                }
            }


            Box(modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.65f))))
                .padding(12.dp)) {
                Column {
                    Text(item.title, color = Color.White, fontWeight = FontWeight.Bold,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (item.colourPattern.isNotBlank()) TagChip(item.colourPattern)
                        if (item.size.isNotBlank())          TagChip(item.size)
                    }
                }
            }


            if (isSelected) {
                Box(modifier = Modifier.align(Alignment.TopEnd).padding(10.dp).size(32.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Check, "Selected", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun TagChip(text: String) {
    Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(Color.White.copy(0.25f))
        .padding(horizontal = 6.dp, vertical = 2.dp)) {
        Text(text, color = Color.White, fontSize = 11.sp)
    }
}


@Composable
fun ThumbnailItem(
    item: ClosetOrganiserModel, isCurrentPage: Boolean,
    isSelected: Boolean, onClick: () -> Unit
) {
    val imageModel: Any? = item.imageUrl.takeIf { it.isNotBlank() } ?: item.image
    val validImage = imageModel != null && imageModel != Uri.EMPTY && imageModel.toString().isNotBlank()

    Box(modifier = Modifier.size(60.dp).shadow(if (isCurrentPage) 4.dp else 1.dp, RoundedCornerShape(10.dp))
        .clip(RoundedCornerShape(10.dp))
        .border(width = when { isSelected -> 2.5.dp; isCurrentPage -> 1.5.dp; else -> 0.dp },
            color = when { isSelected -> MaterialTheme.colorScheme.primary;
                isCurrentPage -> MaterialTheme.colorScheme.primary.copy(0.5f); else -> Color.Transparent },
            shape = RoundedCornerShape(10.dp))
        .background(Color.LightGray).clickable(onClick = onClick)
        .alpha(if (isCurrentPage) 1f else 0.65f)) {

        if (validImage) {
            AsyncImage(model = imageModel, contentDescription = item.title,
                modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary.copy(0.12f)),
                contentAlignment = Alignment.Center) {
                Text(item.title.take(1).uppercase(), fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary)
            }
        }

        if (isSelected) {
            Box(modifier = Modifier.align(Alignment.TopEnd).padding(3.dp).size(16.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(10.dp))
            }
        }
    }
}


@Composable
fun EmptySlotMessage(categoryName: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(160.dp)
        .clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.primary.copy(0.06f)),
        contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Checkroom, null,
                tint = MaterialTheme.colorScheme.primary.copy(0.35f), modifier = Modifier.size(44.dp))
            Spacer(Modifier.height(8.dp))
            Text("No $categoryName in your wardrobe yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary.copy(0.5f), textAlign = TextAlign.Center)
            Text("Add some from the Wardrobe tab",
                style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}


@Composable
fun SavedLooksPanel(outfits: List<OutfitModel>, onLoadOutfit: (OutfitModel) -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp, shadowElevation = 6.dp) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Saved Looks", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(10.dp))
            if (outfits.isEmpty()) {
                Text("No saved looks yet — build one below!",
                    style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    itemsIndexed(outfits) { _, outfit ->
                        SavedOutfitCard(outfit = outfit, onClick = { onLoadOutfit(outfit) })
                    }
                }
            }
        }
    }
}

@Composable
fun SavedOutfitCard(outfit: OutfitModel, onClick: () -> Unit) {
    Card(modifier = Modifier.width(140.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                outfit.clothingItems.take(2).forEach { item ->
                    val imageModel: Any? = item.imageUrl.takeIf { it.isNotBlank() } ?: item.image
                    val valid = imageModel != null && imageModel != Uri.EMPTY && imageModel.toString().isNotBlank()
                    Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray), contentAlignment = Alignment.Center) {
                        if (valid) AsyncImage(model = imageModel, contentDescription = item.title,
                            modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        else Text(item.title.take(1), fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(outfit.title, style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${outfit.clothingItems.size} items",
                style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}