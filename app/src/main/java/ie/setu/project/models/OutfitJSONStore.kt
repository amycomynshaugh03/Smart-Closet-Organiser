package ie.setu.project.models

import android.content.Context
import android.net.Uri
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import ie.setu.project.helpers.*
import ie.setu.project.models.outfit.OutfitModel
import ie.setu.project.models.outfit.OutfitStore
import timber.log.Timber
import java.lang.reflect.Type
import java.util.*

const val OUTFIT_JSON_FILE = "outfits.json"

val outfitGsonBuilder: Gson = GsonBuilder().setPrettyPrinting()
    .registerTypeAdapter(Uri::class.java, UriParser())
    .registerTypeAdapter(Date::class.java, DateParser())
    .create()

val outfitListType: Type = object : TypeToken<ArrayList<OutfitModel>>() {}.type


class OutfitJSONStore(private val context: Context) : OutfitStore {

    private var outfits = mutableListOf<OutfitModel>()

    init {
        if (exists(context, OUTFIT_JSON_FILE)) {
            deserialize()
        }
    }

    override fun findAll(): List<OutfitModel> {
        logAll()
        return outfits
    }

    override fun create(outfit: OutfitModel) {
        outfit.id = generateRandomId()
        outfits.add(outfit)
        serialize()
    }

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

    override fun delete(outfit: OutfitModel) {
        outfits.remove(outfit)
        serialize()
        logAll()
    }

    private fun serialize() {
        val jsonString = outfitGsonBuilder.toJson(outfits, outfitListType)
        write(context, OUTFIT_JSON_FILE, jsonString)
    }

    private fun deserialize() {
        val jsonString = read(context, OUTFIT_JSON_FILE)
        outfits = outfitGsonBuilder.fromJson(jsonString, outfitListType)
    }

    private fun logAll() {
        outfits.forEach { Timber.i("$it") }
    }
}


class DateParser : JsonDeserializer<Date>, JsonSerializer<Date> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Date {
        return Date(json?.asLong ?: 0)
    }

    override fun serialize(src: Date?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src?.time ?: 0)
    }
}