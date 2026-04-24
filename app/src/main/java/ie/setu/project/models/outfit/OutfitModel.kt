package ie.setu.project.models.outfit

import android.os.Parcelable
import ie.setu.project.models.clothing.ClosetOrganiserModel
import kotlinx.parcelize.Parcelize
import java.util.Date

/**
 * Represents a saved outfit composed of one or more clothing items.
 *
 * Implements [Parcelable] so it can be passed between Activities via Intents.
 *
 * @property id Unique identifier for the outfit. Defaults to 0 until assigned by the store.
 * @property title Display name of the outfit (e.g. "Summer Work Look").
 * @property description A brief description of the outfit's style or occasion.
 * @property season The season this outfit is suited for (e.g. "Summer", "Winter", "All").
 * @property lastWorn The date this outfit was last worn. Defaults to the current date.
 * @property clothingItems The list of [ClosetOrganiserModel] items that make up this outfit.
 */

@Parcelize
data class OutfitModel(
    var id: Long = 0,
    var title: String = "",
    var description: String = "",
    var season: String = "",
    var lastWorn: Date = Date(),
    var clothingItems: MutableList<ClosetOrganiserModel> = mutableListOf()
) : Parcelable
