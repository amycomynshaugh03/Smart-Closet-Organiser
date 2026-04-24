package ie.setu.project.models.clothing

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

/**
 * Represents a single clothing item stored in the Smart Closet Organiser.
 *
 * This data class is [Parcelable] so it can be passed between Android components
 * (Activities, Fragments) via Intents.
 *
 * @property id Unique identifier for the clothing item. Defaults to 0 until assigned by the store.
 * @property title Short display name for the item (e.g. "White Linen Shirt").
 * @property description A brief description of the item's style, fabric, or notable details.
 * @property colourPattern Primary colour or pattern of the item (e.g. "Navy Blue", "Red Floral").
 * @property size The size of the item (e.g. "S", "10", "XL").
 * @property season The season this item is suited for (e.g. "Summer", "Winter", "All").
 * @property category The broad category of the item (e.g. "Tops", "Bottoms", "Shoes").
 * @property subCategory A more specific sub-category within the main category.
 * @property lastWorn The date the item was last worn. Defaults to the current date.
 * @property image A local [Uri] pointing to the clothing item's image on the device.
 * @property imageUrl A remote download URL for the image stored in Firebase Storage.
 */

@Parcelize
data class ClosetOrganiserModel(
    var id: Long = 0,
    var title: String = "",
    var description: String = "",
    var colourPattern: String = "",
    var size: String = "",
    var season: String = "",
    var category: String = "",
    var subCategory: String = "",
    var lastWorn: Date = Date(),
    var image: Uri? = Uri.EMPTY,
    var imageUrl: String = ""
) : Parcelable