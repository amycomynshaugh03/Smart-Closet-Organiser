package ie.setu.project.models

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class OutfitModel(
    var id: Long = 0,
    var title: String = "",
    var description: String = "",
    var season: String = "",
    var lastWorn: Date = Date(),
    var image: Uri = Uri.EMPTY,
    var clothingItems: MutableList<ClosetOrganiserModel> = mutableListOf()
) : Parcelable