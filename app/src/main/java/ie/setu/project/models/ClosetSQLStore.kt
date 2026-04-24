package ie.setu.project.models

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import ie.setu.project.models.clothing.ClosetOrganiserModel
import ie.setu.project.models.clothing.ClothingStore
import java.util.Date


private const val DATABASE_NAME = "closet.db"
private const val DATABASE_VERSION = 3

private const val TABLE_NAME = "clothing_items"
private const val COLUMN_ID = "id"
private const val COLUMN_TITLE = "title"
private const val COLUMN_DESCRIPTION = "description"
private const val COLUMN_COLOUR_PATTERN = "colour_pattern"
private const val COLUMN_SIZE = "size"
private const val COLUMN_SEASON = "season"
private const val COLUMN_CATEGORY = "category"
private const val COLUMN_LAST_WORN = "last_worn"
private const val COLUMN_IMAGE = "image"
private const val COLUMN_IMAGE_URL = "image_url"

/**
 * SQLite-backed implementation of [ClothingStore].
 *
 * Uses [ClosetDbHelper] (an inner [SQLiteOpenHelper]) to manage a local SQLite database
 * named "closet.db". The database contains tables for clothing items, outfits, and
 * outfit-clothing junction data.
 *
 * This store is used as both the primary local store and an offline backup when
 * Firestore is unavailable.
 *
 * @constructor Creates the store and opens the writable database.
 * @param context The application context, used to open the database.
 */
class ClosetSQLStore(private val context: Context) : ClothingStore {

    private val database: SQLiteDatabase = ClosetDbHelper(context).writableDatabase


    /**
     * Returns all clothing items from the database, sorted alphabetically by title.
     */
    override suspend fun findAll(): List<ClosetOrganiserModel> {
        val items = mutableListOf<ClosetOrganiserModel>()
        database.rawQuery("SELECT * FROM $TABLE_NAME ORDER BY $COLUMN_TITLE", null).use { cursor ->
            while (cursor.moveToNext()) {
                items.add(createFromCursor(cursor))
            }
        }
        return items
    }

    /**
     * Looks up a single item by ID. Returns null if not found or on error.
     * @param id The ID of the item to find.
     */
    override suspend fun findById(id: Long): ClosetOrganiserModel? {
        return try {
            database.rawQuery(
                "SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = ?",
                arrayOf(id.toString())
            ).use { cursor ->
                if (cursor.moveToFirst()) createFromCursor(cursor) else null
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Inserts a new clothing item into the database.
     * The item's [ClosetOrganiserModel.id] is updated with the auto-generated row ID.
     * @param clothingItem The item to insert.
     */
    override suspend fun create(clothingItem: ClosetOrganiserModel) {
        val values = toContentValues(clothingItem)
        clothingItem.id = database.insert(TABLE_NAME, null, values)
    }

    /**
     * Updates an existing clothing item row matched by its ID.
     * @param closetItem The item with updated field values.
     */
    override suspend fun update(closetItem: ClosetOrganiserModel) {
        val values = toContentValues(closetItem)
        database.update(
            TABLE_NAME,
            values,
            "$COLUMN_ID = ?",
            arrayOf(closetItem.id.toString())
        )
    }

    /**
     * Deletes a clothing item row by its ID.
     * @param clothingItem The item to delete.
     */
    override suspend fun delete(clothingItem: ClosetOrganiserModel) {
        database.delete(
            TABLE_NAME,
            "$COLUMN_ID = ?",
            arrayOf(clothingItem.id.toString())
        )
    }

    private fun createFromCursor(cursor: Cursor): ClosetOrganiserModel {
        val imageString = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE))
        val imageUri = imageString?.takeIf { it.isNotBlank() }?.let(Uri::parse)


        val imageUrlIndex = cursor.getColumnIndex(COLUMN_IMAGE_URL)
        val imageUrl = if (imageUrlIndex >= 0) cursor.getString(imageUrlIndex).orEmpty() else ""

        return ClosetOrganiserModel(
            id           = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
            title        = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
            description  = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
            colourPattern = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COLOUR_PATTERN)),
            size         = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SIZE)),
            season       = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SEASON)),
            category     = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)) ?: "",
            lastWorn     = Date(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_LAST_WORN))),
            image        = imageUri,
            imageUrl     = imageUrl
        )
    }

    private fun toContentValues(item: ClosetOrganiserModel): ContentValues {
        return ContentValues().apply {
            put(COLUMN_TITLE, item.title)
            put(COLUMN_DESCRIPTION, item.description)
            put(COLUMN_COLOUR_PATTERN, item.colourPattern)
            put(COLUMN_SIZE, item.size)
            put(COLUMN_SEASON, item.season)
            put(COLUMN_CATEGORY, item.category)
            put(COLUMN_LAST_WORN, item.lastWorn.time)
            put(COLUMN_IMAGE, item.image?.toString())
            put(COLUMN_IMAGE_URL, item.imageUrl)
        }
    }

    /**
     * Inner [SQLiteOpenHelper] that manages the creation and migration of "closet.db".
     * Version 3 adds the [COLUMN_IMAGE_URL] column to the clothing_items table.
     */
    private inner class ClosetDbHelper(context: Context) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE clothing_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    description TEXT,
                    colour_pattern TEXT,
                    size TEXT,
                    season TEXT,
                    category TEXT,
                    last_worn INTEGER,
                    image TEXT,
                    image_url TEXT
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE outfits (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    description TEXT,
                    season TEXT,
                    last_worn INTEGER
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE outfit_clothing (
                    outfit_id INTEGER,
                    clothing_id INTEGER,
                    PRIMARY KEY (outfit_id, clothing_id),
                    FOREIGN KEY (outfit_id) REFERENCES outfits(id),
                    FOREIGN KEY (clothing_id) REFERENCES clothing_items(id)
                )
                """.trimIndent()
            )
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            if (oldVersion < 3) {
                db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_IMAGE_URL TEXT DEFAULT ''")
            }
        }
    }
}