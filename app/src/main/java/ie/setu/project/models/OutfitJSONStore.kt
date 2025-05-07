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
import ie.setu.project.models.outfit.OutfitModel
import ie.setu.project.models.outfit.OutfitStore
import timber.log.Timber
import java.lang.reflect.Type
import java.util.Date

const val OUTFIT_JSON_FILE = "outfits.json"

/**
 * Gson builder for serializing and deserializing Outfit objects.
 * It includes custom type adapters for Uri and Date types.
 */
val outfitGsonBuilder: Gson = GsonBuilder().setPrettyPrinting()
    .registerTypeAdapter(Uri::class.java, UriParser())
    .registerTypeAdapter(Date::class.java, DateParser())
    .create()

/**
 * Type reference for a list of OutfitModel objects used in Gson serialization.
 */
val outfitListType: Type = object : TypeToken<ArrayList<OutfitModel>>() {}.type

/**
 * A store that manages outfits using a JSON file for persistence.
 * It allows creating, reading, updating, and deleting outfit data, and it persists this data
 * to a JSON file in the app's local storage.
 *
 * @param context The context of the application, used for reading and writing the JSON file.
 */
class OutfitJSONStore(private val context: Context) : OutfitStore {

    private var outfits = mutableListOf<OutfitModel>()

    init {
        // If the JSON file with outfits exists, deserialize the data.
        if (exists(context, OUTFIT_JSON_FILE)) {
            deserialize()
        }
    }

    /**
     * Retrieves all outfits from the store.
     *
     * @return A list of all outfits.
     */
    override fun findAll(): List<OutfitModel> {
        logAll()
        return outfits
    }

    /**
     * Creates a new outfit and adds it to the store. Also serializes the updated list of outfits
     * to the JSON file.
     *
     * @param outfit The outfit to create and add to the store.
     */
    override fun create(outfit: OutfitModel) {
        outfit.id = generateRandomId()
        outfits.add(outfit)
        serialize()
    }

    /**
     * Updates an existing outfit in the store. The outfit is identified by its unique ID.
     * The updated list of outfits is then serialized back to the JSON file.
     *
     * @param outfit The outfit with updated data.
     */
    override fun update(outfit: OutfitModel) {
        val outfitList = findAll() as ArrayList<OutfitModel>
        val foundOutfit: OutfitModel? = outfitList.find { o -> o.id == outfit.id }
        if (foundOutfit != null) {
            foundOutfit.title = outfit.title
            foundOutfit.description = outfit.description
            foundOutfit.season = outfit.season
            foundOutfit.lastWorn = outfit.lastWorn
            foundOutfit.clothingItems = outfit.clothingItems
        }
        serialize()
    }

    /**
     * Deletes an outfit from the store. The updated list of outfits is serialized to the JSON file.
     *
     * @param outfit The outfit to delete.
     */
    override fun delete(outfit: OutfitModel) {
        outfits.remove(outfit)
        serialize()
        logAll()
    }

    /**
     * Serializes the current list of outfits into JSON format and writes it to a file.
     */
    private fun serialize() {
        val jsonString = outfitGsonBuilder.toJson(outfits, outfitListType)
        write(context, OUTFIT_JSON_FILE, jsonString)
    }

    /**
     * Deserializes the outfits JSON data from the file and loads it into the store.
     */
    private fun deserialize() {
        val jsonString = read(context, OUTFIT_JSON_FILE)
        outfits = outfitGsonBuilder.fromJson(jsonString, outfitListType)
    }

    /**
     * Logs all outfits currently in the store. This is useful for debugging purposes.
     */
    private fun logAll() {
        outfits.forEach { Timber.i("$it") }
    }
}

/**
 * A custom [JsonDeserializer] and [JsonSerializer] for the Date type, used to handle
 * the conversion between Date objects and their JSON representations (timestamps).
 */
class DateParser : JsonDeserializer<Date>, JsonSerializer<Date> {

    /**
     * Deserializes a JSON element into a [Date] object.
     *
     * @param json The JSON element to deserialize.
     * @param typeOfT The type of the object to deserialize to.
     * @param context The context of the deserialization process.
     * @return The deserialized [Date] object.
     */
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Date {
        return Date(json?.asLong ?: 0)
    }

    /**
     * Serializes a [Date] object into its JSON representation (a timestamp).
     *
     * @param src The [Date] object to serialize.
     * @param typeOfSrc The type of the object to serialize.
     * @param context The context of the serialization process.
     * @return The serialized JSON element.
     */
    override fun serialize(src: Date?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src?.time ?: 0)
    }
}
