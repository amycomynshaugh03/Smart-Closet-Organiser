package ie.setu.project.models

import android.content.Context
import android.net.Uri
import com.google.gson.*
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

val gsonBuilder: Gson = GsonBuilder()
    .setPrettyPrinting()
    .registerTypeAdapter(Uri::class.java, UriParser())
    .create()

val listType: Type =
    object : TypeToken<ArrayList<ClosetOrganiserModel>>() {}.type

fun generateRandomId(): Long = Random().nextLong()

class ClothingJSONStore(private val context: Context) : ClothingStore {

    private var clothingItems = mutableListOf<ClosetOrganiserModel>()

    init {
        if (exists(context, JSON_FILE)) {
            deserialize()
        }
    }

    override suspend fun findAll(): List<ClosetOrganiserModel> {
        logAll()
        return clothingItems
    }

    override suspend fun create(clothingItem: ClosetOrganiserModel) {
        clothingItem.id = generateRandomId()
        clothingItems.add(clothingItem)
        serialize()
    }

    override suspend fun update(closetItem: ClosetOrganiserModel) {
        val foundItem = clothingItems.find { it.id == closetItem.id }
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

    override suspend fun delete(clothingItem: ClosetOrganiserModel) {
        clothingItems.removeIf { it.id == clothingItem.id }
        serialize()
        logAll()
    }

    override suspend fun findById(id: Long): ClosetOrganiserModel? {
        return clothingItems.find { it.id == id }
    }

    private fun serialize() {
        val jsonString = gsonBuilder.toJson(clothingItems, listType)
        write(context, JSON_FILE, jsonString)
    }

    private fun deserialize() {
        val jsonString = read(context, JSON_FILE)
        clothingItems = gsonBuilder.fromJson(jsonString, listType) ?: mutableListOf()
    }

    private fun logAll() {
        clothingItems.forEach { Timber.i("$it") }
    }
}

class UriParser : JsonDeserializer<Uri>, JsonSerializer<Uri> {

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Uri {
        return Uri.parse(json?.asString)
    }

    override fun serialize(
        src: Uri?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        return JsonPrimitive(src.toString())
    }
}