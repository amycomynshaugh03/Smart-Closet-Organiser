package ie.setu.project.models.outfit

import android.os.Parcelable
import ie.setu.project.models.clothing.ClosetOrganiserModel
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class OutfitModel(
    var id: Long = 0,
    var title: String = "",
    var description: String = "",
    var season: String = "",
    var lastWorn: Date = Date(),
    var clothingItems: MutableList<ClosetOrganiserModel> = mutableListOf()
) : Parcelable