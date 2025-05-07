package ie.setu.project.models.outfit

import android.os.Parcelable
import ie.setu.project.models.clothing.ClosetOrganiserModel
import kotlinx.parcelize.Parcelize
import java.util.Date

/**
 * Data class representing an outfit in the Closet Organiser app.
 *
 * This class contains the details of an outfit, including its title, description, season,
 * the date it was last worn, and the list of clothing items that make up the outfit.
 * The class implements [Parcelable] to allow it to be passed between Android components.
 *
 * @property id The unique identifier of the outfit.
 * @property title The title of the outfit.
 * @property description A brief description of the outfit.
 * @property season The season associated with the outfit (e.g., Summer, Winter).
 * @property lastWorn The date when the outfit was last worn.
 * @property clothingItems A list of [ClosetOrganiserModel] representing the clothing items in the outfit.
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
