package ie.setu.project.models

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ClosetOrganiserModel(
    var id: Long = 0,
    var title: String = "",
    var description: String = "",
    var colourPattern: String = "",
    var size: String = "",
    var season: String = "",
    var lastWorn: String = "",
    var image: Uri = Uri.EMPTY
) : Parcelable