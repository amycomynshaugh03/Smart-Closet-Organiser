package ie.setu.project.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ClosetOrganiserModel(
    var id: Long = 0,
    var title: String = "",
    var description: String = ""
) : Parcelable