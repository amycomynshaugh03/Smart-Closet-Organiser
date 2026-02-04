package ie.setu.project.models.clothing

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

/**
 * Data class representing a clothing item in the closet organiser.
 * This model is used to store details about a clothing item including its title, description,
 * color pattern, size, season, last worn date, and an image URI.
 *
 * @property id The unique identifier for the clothing item.
 * @property title The title or name of the clothing item.
 * @property description A brief description of the clothing item.
 * @property colourPattern The color or pattern description of the clothing item.
 * @property size The size of the clothing item.
 * @property season The season the clothing item is associated with (e.g., summer, winter).
 * @property lastWorn The date the clothing item was last worn.
 * @property image The URI pointing to an image of the clothing item.
 */
@Parcelize
data class ClosetOrganiserModel(
    var id: Long = 0, // Unique identifier for the clothing item.
    var title: String = "", // The title or name of the clothing item.
    var description: String = "", // Description of the clothing item.
    var colourPattern: String = "", // Color or pattern of the clothing item.
    var size: String = "", // Size of the clothing item.
    var season: String = "", // Season associated with the clothing item.
    var category: String = "",
    var lastWorn: Date = Date(), // The last date the item was worn.
    var image: Uri? = Uri.EMPTY // The URI of the image representing the clothing item.
) : Parcelable
