package ie.setu.project.models

import android.content.Context
import android.net.Uri
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import ie.setu.project.helpers.*
import timber.log.Timber
import java.lang.reflect.Type
import java.util.*

const val JSON_FILE = "clothingitems.json"

val gsonBuilder: Gson = GsonBuilder().setPrettyPrinting()
    .registerTypeAdapter(Uri::class.java, UriParser())
    .create()

val listType: Type = object : TypeToken<ArrayList<ClosetOrganiserModel>>() {}.type

fun generateRandomId(): Long = Random().nextLong()

class ClosetJSONStore(private val context: Context) : ClothingStore {

    private var clothingItems = mutableListOf<ClosetOrganiserModel>()

    init {
        if (exists(context, JSON_FILE)) {
            deserialize()
        }
    }

    override fun findAll(): List<ClosetOrganiserModel> {
        logAll()
        return clothingItems
    }

    override fun create(clothingItem: ClosetOrganiserModel) {
        clothingItem.id = generateRandomId()
        clothingItems.add(clothingItem)
        serialize()
    }

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


    override fun delete(clothingItem: ClosetOrganiserModel) {
        clothingItems.remove(clothingItem)
        serialize()
        logAll()
    }

    override fun findById(id: Long): ClosetOrganiserModel? {
        return clothingItems.find { it.id == id }
    }


    private fun serialize() {
        val jsonString = gsonBuilder.toJson(clothingItems, listType)
        write(context, JSON_FILE, jsonString)
    }

    private fun deserialize() {
        val jsonString = read(context, JSON_FILE)
        clothingItems = gsonBuilder.fromJson(jsonString, listType)
    }

    private fun logAll() {
        clothingItems.forEach { Timber.i("$it") }
    }
}

class UriParser : JsonDeserializer<Uri>, JsonSerializer<Uri> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Uri {
        return Uri.parse(json?.asString)
    }

    override fun serialize(src: Uri?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src.toString())
    }
}
