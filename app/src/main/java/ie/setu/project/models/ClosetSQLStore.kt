package ie.setu.project.models

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import ie.setu.project.models.clothing.ClosetOrganiserModel
import ie.setu.project.models.clothing.ClothingStore
import timber.log.Timber.i
import java.util.Date

// Constants for the database schema
private const val DATABASE_NAME = "closet.db"
private const val DATABASE_VERSION = 1
private const val TABLE_NAME = "clothing_items"
private const val COLUMN_ID = "id"
private const val COLUMN_TITLE = "title"
private const val COLUMN_DESCRIPTION = "description"
private const val COLUMN_COLOUR_PATTERN = "colour_pattern"
private const val COLUMN_SIZE = "size"
private const val COLUMN_SEASON = "season"
private const val COLUMN_LAST_WORN = "last_worn"
private const val COLUMN_IMAGE = "image"

/**
 * A store that manages clothing items using an SQLite database for persistence.
 * This store allows performing CRUD operations on clothing items (create, read, update, delete).
 *
 * @param context The context of the application, used for interacting with the SQLite database.
 */
class ClosetSQLStore(private val context: Context) : ClothingStore {

    private var database: SQLiteDatabase

    init {
        // Initialize the database using the helper class
        database = ClosetDbHelper(context).writableDatabase
    }

    /**
     * Retrieves all clothing items from the database.
     *
     * @return A list of all clothing items sorted by title.
     */
    override fun findAll(): List<ClosetOrganiserModel> {
        val items = mutableListOf<ClosetOrganiserModel>()
        database.rawQuery("SELECT * FROM $TABLE_NAME ORDER BY $COLUMN_TITLE", null).use { cursor ->
            while (cursor.moveToNext()) {
                items.add(createFromCursor(cursor))
            }
        }
        i("Retrieved ${items.size} clothing items from database")
        return items
    }

    /**
     * Retrieves a clothing item by its ID from the database.
     *
     * @param id The ID of the clothing item.
     * @return The clothing item with the given ID, or null if not found.
     */
    override fun findById(id: Long): ClosetOrganiserModel? {
        return try {
            database.rawQuery(
                "SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = ?",
                arrayOf(id.toString())
            ).use { cursor ->
                if (cursor.moveToFirst()) {
                    i("Found clothing item with ID $id")
                    createFromCursor(cursor)
                } else {
                    i("Clothing item with ID $id not found")
                    null
                }
            }
        } catch (e: Exception) {
            i("Error finding clothing item $id: ${e.message}")
            null
        }
    }

    /**
     * Adds a new clothing item to the database.
     *
     * @param clothingItem The clothing item to be added.
     */
    override fun create(clothingItem: ClosetOrganiserModel) {
        val values = toContentValues(clothingItem)
        clothingItem.id = database.insert(TABLE_NAME, null, values)
        i("Created new clothing item with ID ${clothingItem.id}")
    }

    /**
     * Updates an existing clothing item in the database.
     *
     * @param closetItem The clothing item with updated data.
     */
    override fun update(closetItem: ClosetOrganiserModel) {
        val values = toContentValues(closetItem)
        val rowsAffected = database.update(
            TABLE_NAME,
            values,
            "$COLUMN_ID = ?",
            arrayOf(closetItem.id.toString())
        )
        i("Updated clothing item ${closetItem.id}. Rows affected: $rowsAffected")
    }

    /**
     * Deletes a clothing item from the database.
     *
     * @param clothingItem The clothing item to be deleted.
     */
    override fun delete(clothingItem: ClosetOrganiserModel) {
        val rowsDeleted = database.delete(
            TABLE_NAME,
            "$COLUMN_ID = ?",
            arrayOf(clothingItem.id.toString())
        )
        i("Deleted clothing item ${clothingItem.id}. Rows deleted: $rowsDeleted")
    }

    /**
     * Converts a Cursor object to a ClosetOrganiserModel.
     *
     * @param cursor The Cursor object that holds the result of the query.
     * @return A ClosetOrganiserModel object created from the cursor data.
     */
    private fun createFromCursor(cursor: Cursor): ClosetOrganiserModel {
        return ClosetOrganiserModel(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
            title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
            description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
            colourPattern = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COLOUR_PATTERN)),
            size = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SIZE)),
            season = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SEASON)),
            lastWorn = Date(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_LAST_WORN))),
            image = Uri.parse(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE)))
        )
    }

    /**
     * Converts a ClosetOrganiserModel object to ContentValues for database insertion or update.
     *
     * @param item The ClosetOrganiserModel object to be converted.
     * @return A ContentValues object representing the clothing item.
     */
    private fun toContentValues(item: ClosetOrganiserModel): ContentValues {
        return ContentValues().apply {
            put(COLUMN_TITLE, item.title)
            put(COLUMN_DESCRIPTION, item.description)
            put(COLUMN_COLOUR_PATTERN, item.colourPattern)
            put(COLUMN_SIZE, item.size)
            put(COLUMN_SEASON, item.season)
            put(COLUMN_LAST_WORN, item.lastWorn.time)
            put(COLUMN_IMAGE, item.image?.toString())
        }
    }

    /**
     * A helper class that manages the creation and upgrade of the SQLite database.
     * This class is responsible for creating the database schema and handling upgrades
     * when the database version changes.
     */
    private inner class ClosetDbHelper(context: Context) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

        /**
         * Creates the necessary tables when the database is first created.
         *
         * @param db The SQLiteDatabase object.
         */
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
                    last_worn INTEGER,
                    image TEXT
                )
                """
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
                """
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
                """
            )
        }

        /**
         * Handles database upgrades. If the database version is increased, old tables are dropped
         * and new ones are created.
         *
         * @param db The SQLiteDatabase object.
         * @param oldVersion The old database version.
         * @param newVersion The new database version.
         */
        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS clothing_items")
            db.execSQL("DROP TABLE IF EXISTS outfits")
            db.execSQL("DROP TABLE IF EXISTS outfit_clothing")
            onCreate(db)
        }
    }
}
