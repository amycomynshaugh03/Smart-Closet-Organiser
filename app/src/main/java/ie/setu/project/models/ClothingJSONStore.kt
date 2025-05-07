package ie.setu.project.models

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import ie.setu.project.helpers.exists
import ie.setu.project.helpers.read
import ie.setu.project.helpers.write
import ie.setu.project.models.clothing.ClosetOrganiserModel
import ie.setu.project.models.clothing.ClothingStore
import timber.log.Timber
import java.lang.reflect.Type
import java.util.Random

const val JSON_FILE = "clothingitems.json"

/**
 * Gson builder for serializing and deserializing Clothing objects.
 * It includes a custom type adapter for Uri.
 */
val gsonBuilder: Gson = GsonBuilder().setPrettyPrinting()
    .registerTypeAdapter(Uri::class.java, UriParser())
    .create()

/**
 * Type reference for a list of ClosetOrganiserModel objects used in Gson serialization.
 */
val listType: Type = object : TypeToken<ArrayList<ClosetOrganiserModel>>() {}.type

/**
 * Generates a random ID for a new clothing item.
 *
 * @return A randomly generated ID as a Long value.
 */
fun generateRandomId(): Long = Random().nextLong()

/**
 * A store that manages clothing items using a JSON file for persistence.
 * It allows creating, reading, updating, and deleting clothing data, and it persists this data
 * to a JSON file in the app's local storage.
 *
 * @param context The context of the application, used for reading and writing the JSON file.
 */
class ClothingJSONStore(private val context: Context) : ClothingStore {

    private var clothingItems = mutableListOf<ClosetOrganiserModel>()

    init {
        // If the JSON file with clothing items exists, deserialize the data.
        if (exists(context, JSON_FILE)) {
            deserialize()
        }
    }

    /**
     * Retrieves all clothing items from the store.
     *
     * @return A list of all clothing items.
     */
    override fun findAll(): List<ClosetOrganiserModel> {
        logAll()
        return clothingItems
    }

    /**
     * Creates a new clothing item and adds it to the store. Also serializes the updated list of clothing
     * items to the JSON file.
     *
     * @param clothingItem The clothing item to create and add to the store.
     */
    override fun create(clothingItem: ClosetOrganiserModel) {
        clothingItem.id = generateRandomId()
        clothingItems.add(clothingItem)
        serialize()
    }

    /**
     * Updates an existing clothing item in the store. The clothing item is identified by its unique ID.
     * The updated list of clothing items is then serialized back to the JSON file.
     *
     * @param closetItem The clothing item with updated data.
     */
    override fun update(closetItem: ClosetOrganiserModel) {
        val clothingList = findAll() as ArrayList<ClosetOrganiserModel>
        val foundItem: ClosetOrganiserModel? = clothingList.find { c -> c.id == closetItem.id }
        if (foundItem != null) {
            foundItem.title = closetItem.title
            foundItem.description = closetItem.description
            foundItem.colourPattern = closetItem.colourPattern
            foundItem.size = closetItem.size
            foundItem.season = closetItem.season
            foundItem.lastWorn = closetItem.lastWorn
            foundItem.image = closetItem.image
        }
        serialize()
    }

    /**
     * Deletes a clothing item from the store. The updated list of clothing items is serialized to the JSON file.
     *
     * @param clothingItem The clothing item to delete.
     */
    override fun delete(clothingItem: ClosetOrganiserModel) {
        clothingItems.remove(clothingItem)
        serialize()
        logAll()
    }

    /**
     * Retrieves a clothing item by its ID.
     *
     * @param id The ID of the clothing item.
     * @return The clothing item with the matching ID, or null if not found.
     */
    override fun findById(id: Long): ClosetOrganiserModel? {
        return clothingItems.find { it.id == id }
    }

    /**
     * Serializes the current list of clothing items into JSON format and writes it to a file.
     */
    private fun serialize() {
        val jsonString = gsonBuilder.toJson(clothingItems, listType)
        write(context, JSON_FILE, jsonString)
    }

    /**
     * Deserializes the clothing items JSON data from the file and loads it into the store.
     */
    private fun deserialize() {
        val jsonString = read(context, JSON_FILE)
        clothingItems = gsonBuilder.fromJson(jsonString, listType)
    }

    /**
     * Logs all clothing items currently in the store. This is useful for debugging purposes.
     */
    private fun logAll() {
        clothingItems.forEach { Timber.i("$it") }
    }
}

/**
 * A custom [JsonDeserializer] and [JsonSerializer] for the Uri type, used to handle
 * the conversion between Uri objects and their JSON representations (strings).
 */
class UriParser : JsonDeserializer<Uri>, JsonSerializer<Uri> {

    /**
     * Deserializes a JSON element into a [Uri] object.
     *
     * @param json The JSON element to deserialize.
     * @param typeOfT The type of the object to deserialize to.
     * @param context The context of the deserialization process.
     * @return The deserialized [Uri] object.
     */
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Uri {
        return Uri.parse(json?.asString)
    }

    /**
     * Serializes a [Uri] object into its JSON representation (a string).
     *
     * @param src The [Uri] object to serialize.
     * @param typeOfSrc The type of the object to serialize.
     * @param context The context of the serialization process.
     * @return The serialized JSON element.
     */
    override fun serialize(src: Uri?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src.toString())
    }
}
