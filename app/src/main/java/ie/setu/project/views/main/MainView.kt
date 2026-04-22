package ie.setu.project.views.main

import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import ie.setu.project.R
import ie.setu.project.models.clothing.ClosetOrganiserModel
import ie.setu.project.ui.theme.ClosetOrganiserTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class MainView : AppCompatActivity() {

    private lateinit var presenter: MainPresenter

    private var titleState by mutableStateOf("")
    private var descriptionState by mutableStateOf("")
    private var colourState by mutableStateOf("")
    private var sizeState by mutableStateOf("")
    private var seasonState by mutableStateOf("")
    private var categoryState by mutableStateOf("")
    private var subCategoryState by mutableStateOf("")
    private var lastWornState by mutableStateOf("")
    private var imageUriState by mutableStateOf<Uri?>(null)
    private var isEditState by mutableStateOf(false)
    var removeBgState by mutableStateOf(false)
    var isScanningState by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = MainPresenter(this)
        isEditState = presenter.edit

        titleState = presenter.closetOrganiser.title
        descriptionState = presenter.closetOrganiser.description
        colourState = presenter.closetOrganiser.colourPattern
        sizeState = presenter.closetOrganiser.size
        seasonState = presenter.closetOrganiser.season
        categoryState = presenter.closetOrganiser.category
        subCategoryState = presenter.closetOrganiser.subCategory

        if (categoryState.isBlank()) categoryState = "Tops"

        if (seasonState.isBlank()) {
            val seasons = resources.getStringArray(R.array.seasons_array)
            if (seasons.isNotEmpty()) seasonState = seasons.first()
        }

        lastWornState = try {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(presenter.closetOrganiser.lastWorn)
        } catch (e: Exception) { "" }

        imageUriState = presenter.closetOrganiser.image.takeIf { it != Uri.EMPTY }

        setContent { ClosetOrganiserTheme {
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()

            MainScreen(
                title = titleState,
                onTitleChange = { titleState = it },
                description = descriptionState,
                onDescriptionChange = { descriptionState = it },
                colour = colourState,
                onColourChange = { colourState = it },
                size = sizeState,
                onSizeChange = { sizeState = it },
                season = seasonState,
                onSeasonChange = { seasonState = it },
                category = categoryState,
                onCategoryChange = { categoryState = it },
                subCategory = subCategoryState,
                onSubCategoryChange = { subCategoryState = it },
                lastWornText = lastWornState,
                onPickLastWorn = { presenter.showDatePicker() },
                imageUri = imageUriState,
                onChooseImage = { presenter.doSelectImage() },
                isEdit = isEditState,
                onCancel = { presenter.doCancel() },
                onSave = {
                    if (titleState.isBlank()) {
                        scope.launch { snackbarHostState.showSnackbar("Please enter missing item") }
                    } else {
                        presenter.doAddOrSave(
                            titleState, descriptionState, colourState,
                            sizeState, seasonState, categoryState, subCategoryState
                        )
                    }
                },
                snackbarHostState = snackbarHostState,
                removeBg = removeBgState,
                onRemoveBgChange = { removeBgState = it },
                isScanning = isScanningState,
                onScanImage = { presenter.doScanImage() }
            )
        } }
    }

    fun showClosetItem(item: ClosetOrganiserModel) {
        isEditState = true
        titleState = item.title
        descriptionState = item.description
        colourState = item.colourPattern
        sizeState = item.size
        seasonState = item.season
        categoryState = item.category
        subCategoryState = item.subCategory
        lastWornState = try {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(item.lastWorn)
        } catch (e: Exception) { "" }
        imageUriState = item.image.takeIf { it != Uri.EMPTY }
    }

    fun updateImage(image: Uri) { imageUriState = image }
    fun updateLastWornDate(date: String) { lastWornState = date }

    fun updateFromAnalysis(title: String, description: String, colour: String, category: String) {
        if (title.isNotBlank())       titleState       = title
        if (description.isNotBlank()) descriptionState = description
        if (colour.isNotBlank())      colourState      = colour
        if (category.isNotBlank())    categoryState    = category
    }

    fun setScanning(scanning: Boolean) { isScanningState = scanning }
}