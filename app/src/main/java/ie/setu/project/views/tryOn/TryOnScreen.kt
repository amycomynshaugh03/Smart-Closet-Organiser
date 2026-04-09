package ie.setu.project.views.tryOn

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import ie.setu.project.R
import ie.setu.project.models.clothing.ClosetOrganiserModel
import ie.setu.project.models.outfit.OutfitModel
import kotlinx.coroutines.launch

data class OutfitSlot(val label: String, val category: String, val iconRes: Int)

val OUTFIT_SLOTS = listOf(
    OutfitSlot("Top",    "Tops",    R.drawable.ic_heart),
    OutfitSlot("Bottom", "Bottoms", R.drawable.ic_heart),
    OutfitSlot("Jacket", "Jackets", R.drawable.ic_heart),
    OutfitSlot("Shoes",  "Shoes",   R.drawable.ic_heart)
)

private const val IDX_TOP    = 0
private const val IDX_BOTTOM = 1
private const val IDX_JACKET = 2
private const val IDX_SHOES  = 3


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TryOnScreen(
    clothingItems: List<ClosetOrganiserModel>,
    savedOutfits: List<OutfitModel>,
    onSaveOutfit: (String, List<ClosetOrganiserModel>) -> Unit,
    onBack: () -> Unit
) {
    val selectedItems     = remember { mutableStateMapOf<Int, ClosetOrganiserModel>() }
    var activeSlotIndex   by remember { mutableIntStateOf(0) }
    var showPreview       by remember { mutableStateOf(false) }
    var showSaveDialog    by remember { mutableStateOf(false) }
    var outfitName        by remember { mutableStateOf("") }
    var showSavedPanel    by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope             = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        if (showPreview) showPreview = false else onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                title = {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier              = Modifier.fillMaxWidth()
                    ) {
                        Icon(painterResource(R.drawable.ic_heart), null,
                            tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (showPreview) "Your Look" else "Virtual Try-On",
                            fontSize = 26.sp, fontFamily = FontFamily.Cursive, color = Color.White
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(painterResource(R.drawable.ic_heart), null,
                            tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                },
                actions = {
                    IconButton(onClick = { showSavedPanel = !showSavedPanel }) {
                        Icon(Icons.Default.Bookmarks, "Saved Looks", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->

        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            AnimatedVisibility(
                visible = showSavedPanel,
                enter   = slideInVertically(tween(280)) { -it } + fadeIn(),
                exit    = slideOutVertically(tween(280)) { -it } + fadeOut()
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
                    showPreview    = true
                    scope.launch { snackbarHostState.showSnackbar("Outfit loaded!") }
                })
            }

            AnimatedContent(
                targetState = showPreview,
                transitionSpec = {
                    if (targetState) {
                        slideInHorizontally(tween(320)) { it } + fadeIn() togetherWith
                                slideOutHorizontally(tween(320)) { -it } + fadeOut()
                    } else {
                        slideInHorizontally(tween(320)) { -it } + fadeIn() togetherWith
                                slideOutHorizontally(tween(320)) { it } + fadeOut()
                    }
                },
                label = "phaseSwitch"
            ) { inPreview ->
                if (inPreview) {
                    OutfitPreviewPhase(
                        selectedItems     = selectedItems,
                        clothingItems     = clothingItems,
                        onSaveClick       = { outfitName = ""; showSaveDialog = true },
                        onBackToSelection = { showPreview = false }
                    )
                } else {
                    SelectionPhase(
                        clothingItems   = clothingItems,
                        selectedItems   = selectedItems,
                        activeSlotIndex = activeSlotIndex,
                        onSlotSelected  = { activeSlotIndex = it },
                        onTryOnClick    = {
                            if (selectedItems.isEmpty()) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Pick at least one item first!")
                                }
                            } else {
                                showPreview = true
                            }
                        }
                    )
                }
            }
        }

        if (showSaveDialog) {
            AlertDialog(
                onDismissRequest = { showSaveDialog = false },
                title = { Text("Save This Look") },
                text = {
                    Column {
                        Text("Give your outfit a name:",
                            style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value         = outfitName,
                            onValueChange = { outfitName = it },
                            placeholder   = { Text("e.g. Summer Casual") },
                            singleLine    = true,
                            modifier      = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val name = outfitName.trim().ifBlank { "My Look" }
                        onSaveOutfit(name, selectedItems.values.toList())
                        showSaveDialog = false
                        scope.launch { snackbarHostState.showSnackbar("\"$name\" saved!") }
                    }) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { showSaveDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SelectionPhase(
    clothingItems: List<ClosetOrganiserModel>,
    selectedItems: MutableMap<Int, ClosetOrganiserModel>,
    activeSlotIndex: Int,
    onSlotSelected: (Int) -> Unit,
    onTryOnClick: () -> Unit
) {
    val activeSlot    = OUTFIT_SLOTS[activeSlotIndex]
    val filteredItems = remember(clothingItems, activeSlotIndex) {
        clothingItems.filter { it.category.equals(activeSlot.category, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        MiniOutfitCanvas(
            selectedItems   = selectedItems,
            activeSlotIndex = activeSlotIndex,
            onSlotClick     = onSlotSelected
        )

        Spacer(Modifier.height(8.dp))

        SlotSelectorTabs(
            slots           = OUTFIT_SLOTS,
            activeSlotIndex = activeSlotIndex,
            selectedItems   = selectedItems,
            onSlotSelected  = onSlotSelected
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Choose ${activeSlot.label}",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary,
                modifier   = Modifier.weight(1f)
            )
            Text("${filteredItems.size} items",
                style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }

        Spacer(Modifier.height(8.dp))

        Box(modifier = Modifier.weight(1f)) {
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
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick  = { selectedItems.clear() },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Clear")
            }

            Button(
                onClick  = onTryOnClick,
                modifier = Modifier.weight(2f),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                enabled  = selectedItems.isNotEmpty()
            ) {
                Icon(Icons.Default.Visibility, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                val count = selectedItems.size
                Text(
                    if (count == 0) "Try On" else "Try On ($count selected)",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OutfitPreviewPhase(
    selectedItems: MutableMap<Int, ClosetOrganiserModel>,
    clothingItems: List<ClosetOrganiserModel>,
    onSaveClick: () -> Unit,
    onBackToSelection: () -> Unit
) {
    val itemsPerSlot = remember(clothingItems) {
        OUTFIT_SLOTS.map { slot ->
            clothingItems.filter { it.category.equals(slot.category, ignoreCase = true) }
        }
    }


    val pagerStates = OUTFIT_SLOTS.mapIndexed { slotIdx, _ ->
        val startPage = remember(itemsPerSlot[slotIdx], selectedItems[slotIdx]) {
            val cur = selectedItems[slotIdx]
            if (cur != null)
                itemsPerSlot[slotIdx].indexOfFirst { it.id == cur.id }.coerceAtLeast(0)
            else 0
        }
        rememberPagerState(initialPage = startPage, pageCount = { itemsPerSlot[slotIdx].size })
    }


    OUTFIT_SLOTS.forEachIndexed { slotIdx, _ ->
        val items = itemsPerSlot[slotIdx]
        val state = pagerStates[slotIdx]
        LaunchedEffect(state.currentPage) {
            if (items.isNotEmpty()) selectedItems[slotIdx] = items[state.currentPage]
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        Spacer(Modifier.height(6.dp))

        Text(
            "Swipe any row to try a different item",
            style     = MaterialTheme.typography.bodySmall,
            color     = Color.Gray,
            modifier  = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(14.dp))

        PreviewSlotRow(
            label      = "Top",
            slotIndex  = IDX_TOP,
            items      = itemsPerSlot[IDX_TOP],
            pagerState = pagerStates[IDX_TOP],
            cardHeight = 280.dp,
            isOptional = false
        )

        PreviewSlotRow(
            label      = "Bottom",
            slotIndex  = IDX_BOTTOM,
            items      = itemsPerSlot[IDX_BOTTOM],
            pagerState = pagerStates[IDX_BOTTOM],
            cardHeight = 300.dp,
            isOptional = false
        )


        PreviewSlotRow(
            label      = "Jacket",
            slotIndex  = IDX_JACKET,
            items      = itemsPerSlot[IDX_JACKET],
            pagerState = pagerStates[IDX_JACKET],
            cardHeight = 250.dp,
            isOptional = true
        )

        PreviewSlotRow(
            label      = "Shoes",
            slotIndex  = IDX_SHOES,
            items      = itemsPerSlot[IDX_SHOES],
            pagerState = pagerStates[IDX_SHOES],
            cardHeight = 210.dp,
            isOptional = false
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick  = onBackToSelection,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Edit")
            }
            Button(
                onClick  = onSaveClick,
                modifier = Modifier.weight(2f),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Save This Look", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(28.dp))
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PreviewSlotRow(
    label: String,
    slotIndex: Int,
    items: List<ClosetOrganiserModel>,
    pagerState: PagerState,
    cardHeight: Dp,
    isOptional: Boolean
) {
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {


        Row(
            modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary
            )
            if (isOptional) {
                Spacer(Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(0.12f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("optional", fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium)
                }
            }
            Spacer(Modifier.weight(1f))
            if (items.isNotEmpty()) {
                Text(
                    "${pagerState.currentPage + 1} / ${items.size}",
                    style = MaterialTheme.typography.labelSmall, color = Color.Gray
                )
            }
        }

        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(if (isOptional) 72.dp else 110.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Checkroom, null,
                        tint     = MaterialTheme.colorScheme.primary.copy(0.3f),
                        modifier = Modifier.size(26.dp))
                    Spacer(Modifier.height(4.dp))
                    Text("No $label items in wardrobe",
                        style = MaterialTheme.typography.bodySmall, color = Color.Gray,
                        textAlign = TextAlign.Center)
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                HorizontalPager(
                    state    = pagerState,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    PreviewItemCard(item = items[page], height = cardHeight)
                }

                if (pagerState.currentPage > 0) {
                    IconButton(
                        onClick  = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .background(Color.Black.copy(0.42f), CircleShape)
                            .size(38.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, "Prev", tint = Color.White,
                            modifier = Modifier.size(18.dp))
                    }
                }

                if (pagerState.currentPage < items.lastIndex) {
                    IconButton(
                        onClick  = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .background(Color.Black.copy(0.42f), CircleShape)
                            .size(38.dp)
                    ) {
                        Icon(Icons.Default.ArrowForward, "Next", tint = Color.White,
                            modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            if (items.size > 1) {
                Text(
                    "${pagerState.currentPage + 1} / ${items.size}",
                    style     = MaterialTheme.typography.labelSmall,
                    color     = Color.Gray,
                    modifier  = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }

        HorizontalDivider(
            modifier  = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            thickness = 0.5.dp,
            color     = MaterialTheme.colorScheme.outline.copy(0.25f)
        )
    }
}


@Composable
fun PreviewItemCard(item: ClosetOrganiserModel, height: Dp) {
    val imageModel: Any? = item.imageUrl.takeIf { it.isNotBlank() } ?: item.image
    val valid = imageModel != null && imageModel != Uri.EMPTY && imageModel.toString().isNotBlank()

    Card(
        modifier  = Modifier.fillMaxWidth().height(height),
        shape     = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (valid) {
                AsyncImage(model = imageModel, contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(
                        MaterialTheme.colorScheme.primary.copy(0.12f),
                        MaterialTheme.colorScheme.primary.copy(0.22f)))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(item.title.take(2).uppercase(), fontSize = 52.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary.copy(0.38f))
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.62f))))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column {
                    Text(item.title, color = Color.White, fontWeight = FontWeight.Bold,
                        fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (item.colourPattern.isNotBlank()) TagChip(item.colourPattern)
                        if (item.size.isNotBlank())          TagChip(item.size)
                        if (item.season.isNotBlank())        TagChip(item.season)
                    }
                }
            }
        }
    }
}

@Composable
fun MiniOutfitCanvas(
    selectedItems: Map<Int, ClosetOrganiserModel>,
    activeSlotIndex: Int,
    onSlotClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(3.dp, RoundedCornerShape(18.dp)),
        shape  = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.4f)
        )
    ) {
        Column(
            modifier            = Modifier.fillMaxWidth().padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Your Look", style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(10.dp))

            if (selectedItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(80.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Brush.verticalGradient(listOf(
                            MaterialTheme.colorScheme.primary.copy(0.06f),
                            MaterialTheme.colorScheme.primary.copy(0.13f)))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Select items below to build your look",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary.copy(0.5f),
                        textAlign = TextAlign.Center)
                }
            } else {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    OUTFIT_SLOTS.forEachIndexed { idx, slot ->
                        MiniSlotCard(
                            slot     = slot,
                            item     = selectedItems[idx],
                            isActive = idx == activeSlotIndex,
                            onClick  = { onSlotClick(idx) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            val count = selectedItems.size
            LinearProgressIndicator(
                progress   = { count.toFloat() / OUTFIT_SLOTS.size },
                modifier   = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
                color      = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(0.15f)
            )
            Spacer(Modifier.height(3.dp))
            Text("$count / ${OUTFIT_SLOTS.size} slots filled",
                style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

@Composable
fun MiniSlotCard(
    slot: OutfitSlot, item: ClosetOrganiserModel?,
    isActive: Boolean, onClick: () -> Unit
) {
    val borderAlpha by animateFloatAsState(if (isActive) 1f else 0.3f, tween(200), label = "ba")
    val elevation   by animateDpAsState(if (isActive) 5.dp else 1.dp, tween(200), label = "el")
    val hasItem     = item != null
    val imageModel: Any? = item?.imageUrl?.takeIf { it.isNotBlank() } ?: item?.image

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(66.dp)) {
        Box(
            modifier = Modifier.size(66.dp).shadow(elevation, RoundedCornerShape(11.dp))
                .clip(RoundedCornerShape(11.dp))
                .background(
                    if (hasItem) Color.White
                    else MaterialTheme.colorScheme.primary.copy(0.08f)
                )
                .border(if (isActive) 2.dp else 1.dp,
                    MaterialTheme.colorScheme.primary.copy(borderAlpha),
                    RoundedCornerShape(11.dp))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (hasItem && imageModel != null &&
                imageModel.toString().isNotBlank() && imageModel != Uri.EMPTY) {
                AsyncImage(model = imageModel, contentDescription = item!!.title,
                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
            } else if (hasItem) {
                Box(modifier = Modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.primary.copy(0.15f)),
                    contentAlignment = Alignment.Center) {
                    Text(item!!.title.take(2).uppercase(), fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary)
                }
            } else {
                Icon(Icons.Default.Add, "Add ${slot.label}",
                    tint = MaterialTheme.colorScheme.primary.copy(0.45f),
                    modifier = Modifier.size(26.dp))
            }
        }
        Spacer(Modifier.height(3.dp))
        Text(
            if (hasItem) item!!.title else slot.label,
            style      = MaterialTheme.typography.labelSmall,
            maxLines   = 1, overflow = TextOverflow.Ellipsis,
            color      = if (isActive) MaterialTheme.colorScheme.primary else Color.Gray,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun SlotSelectorTabs(
    slots: List<OutfitSlot>, activeSlotIndex: Int,
    selectedItems: Map<Int, ClosetOrganiserModel>, onSlotSelected: (Int) -> Unit
) {
    LazyRow(
        modifier              = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding        = PaddingValues(horizontal = 4.dp)
    ) {
        itemsIndexed(slots) { index, slot ->
            val isActive = index == activeSlotIndex
            val hasItem  = selectedItems.containsKey(index)
            FilterChip(
                selected = isActive,
                onClick  = { onSlotSelected(index) },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (hasItem) {
                            Box(modifier = Modifier.size(7.dp).clip(CircleShape)
                                .background(
                                    if (isActive) Color.White
                                    else MaterialTheme.colorScheme.primary
                                ))
                            Spacer(Modifier.width(4.dp))
                        }
                        Text(slot.label)
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor     = Color.White
                )
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

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
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
                ) {
                    Icon(Icons.Default.ArrowBack, "Prev", tint = Color.White,
                        modifier = Modifier.size(18.dp))
                }
            }

            if (pagerState.currentPage < items.lastIndex) {
                IconButton(
                    onClick  = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                    modifier = Modifier.align(Alignment.CenterEnd)
                        .background(Color.Black.copy(0.4f), CircleShape).size(36.dp)
                ) {
                    Icon(Icons.Default.ArrowForward, "Next", tint = Color.White,
                        modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            repeat(items.size) { idx ->
                Box(modifier = Modifier.padding(3.dp)
                    .size(if (idx == pagerState.currentPage) 9.dp else 5.dp)
                    .clip(CircleShape)
                    .background(
                        if (idx == pagerState.currentPage) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.primary.copy(0.3f))
                    .clickable { scope.launch { pagerState.animateScrollToPage(idx) } })
            }
        }

        Spacer(Modifier.height(8.dp))

        LazyRow(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            contentPadding        = PaddingValues(horizontal = 4.dp)
        ) {
            itemsIndexed(items) { idx, item ->
                ThumbnailItem(
                    item          = item,
                    isCurrentPage = pagerState.currentPage == idx,
                    isSelected    = selectedItem?.id == item.id,
                    onClick       = {
                        scope.launch { pagerState.animateScrollToPage(idx) }
                        onItemSelected(item)
                    }
                )
            }
        }

        Spacer(Modifier.height(4.dp))
    }
}

@Composable
fun ClothingSliderCard(
    item: ClosetOrganiserModel, isSelected: Boolean, onClick: () -> Unit
) {
    val imageModel: Any? = item.imageUrl.takeIf { it.isNotBlank() } ?: item.image
    val valid = imageModel != null && imageModel != Uri.EMPTY && imageModel.toString().isNotBlank()

    Card(
        modifier  = Modifier.fillMaxSize()
            .border(if (isSelected) 3.dp else 0.dp,
                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(if (isSelected) 8.dp else 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (valid) {
                AsyncImage(model = imageModel, contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
            } else {
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(
                    MaterialTheme.colorScheme.primary.copy(0.15f),
                    MaterialTheme.colorScheme.primary.copy(0.25f)))),
                    contentAlignment = Alignment.Center) {
                    Text(item.title.take(2).uppercase(), fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary.copy(0.5f))
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
                    Icon(Icons.Default.Check, "Selected", tint = Color.White,
                        modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun TagChip(text: String) {
    Box(modifier = Modifier.clip(RoundedCornerShape(6.dp))
        .background(Color.White.copy(0.25f))
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
    val valid = imageModel != null && imageModel != Uri.EMPTY && imageModel.toString().isNotBlank()

    Box(modifier = Modifier
        .size(56.dp)
        .shadow(if (isCurrentPage) 3.dp else 1.dp, RoundedCornerShape(10.dp))
        .clip(RoundedCornerShape(10.dp))
        .border(
            width = when { isSelected -> 2.dp; isCurrentPage -> 1.dp; else -> 0.dp },
            color = when {
                isSelected    -> MaterialTheme.colorScheme.primary
                isCurrentPage -> MaterialTheme.colorScheme.primary.copy(0.5f)
                else          -> Color.Transparent
            },
            shape = RoundedCornerShape(10.dp))
        .background(Color.LightGray)
        .clickable(onClick = onClick)
        .alpha(if (isCurrentPage) 1f else 0.6f)
    ) {
        if (valid) {
            AsyncImage(model = imageModel, contentDescription = item.title,
                modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
        } else {
            Box(modifier = Modifier.fillMaxSize()
                .background(MaterialTheme.colorScheme.primary.copy(0.12f)),
                contentAlignment = Alignment.Center) {
                Text(item.title.take(1).uppercase(), fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary)
            }
        }

        if (isSelected) {
            Box(modifier = Modifier.align(Alignment.TopEnd).padding(3.dp).size(14.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Check, null, tint = Color.White,
                    modifier = Modifier.size(9.dp))
            }
        }
    }
}

@Composable
fun EmptySlotMessage(categoryName: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        .height(140.dp).clip(RoundedCornerShape(16.dp))
        .background(MaterialTheme.colorScheme.primary.copy(0.06f)),
        contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Checkroom, null,
                tint = MaterialTheme.colorScheme.primary.copy(0.35f),
                modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(6.dp))
            Text("No $categoryName in your wardrobe yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary.copy(0.5f),
                textAlign = TextAlign.Center)
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
    Card(modifier = Modifier.width(130.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                outfit.clothingItems.take(2).forEach { item ->
                    val imageModel: Any? = item.imageUrl.takeIf { it.isNotBlank() } ?: item.image
                    val valid = imageModel != null && imageModel != Uri.EMPTY &&
                            imageModel.toString().isNotBlank()
                    Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray), contentAlignment = Alignment.Center) {
                        if (valid) AsyncImage(model = imageModel, contentDescription = item.title,
                            modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                        else Text(item.title.take(1), fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(5.dp))
            Text(outfit.title, style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${outfit.clothingItems.size} items",
                style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}