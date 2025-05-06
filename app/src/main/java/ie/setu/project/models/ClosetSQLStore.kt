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

class ClosetSQLStore(private val context: Context) : ClothingStore {

    private var database: SQLiteDatabase

    init {
        database = ClosetDbHelper(context).writableDatabase
    }

    override fun findAll(): List<ClosetOrganiserModel> {
        val items = mutableListOf<ClosetOrganiserModel>()
        database.rawQuery("SELECT * FROM $TABLE_NAME", null).use { cursor ->
            while (cursor.moveToNext()) {
                items.add(createFromCursor(cursor))
            }
        }
        return items
    }

    override fun findById(id: Long): ClosetOrganiserModel? {
        return try {
            database.rawQuery(
                "SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = ?",
                arrayOf(id.toString())
            ).use { cursor ->
                if (cursor.moveToFirst()) createFromCursor(cursor) else null
            }
        } catch (e: Exception) {
            i("Error finding item $id: ${e.message}")
            null
        }
    }

    private fun createFromCursor(cursor: Cursor): ClosetOrganiserModel {
        return ClosetOrganiserModel(
            id = cursor.getLong(0),
            title = cursor.getString(1),
            description = cursor.getString(2),
            colourPattern = cursor.getString(3),
            size = cursor.getString(4),
            season = cursor.getString(5),
            lastWorn = Date(cursor.getLong(6)),
            image = Uri.parse(cursor.getString(7))
        )
    }

    override fun create(clothingItem: ClosetOrganiserModel) {
        val values = ContentValues().apply {
            put(COLUMN_TITLE, clothingItem.title)
            put(COLUMN_DESCRIPTION, clothingItem.description)
            put(COLUMN_COLOUR_PATTERN, clothingItem.colourPattern)
            put(COLUMN_SIZE, clothingItem.size)
            put(COLUMN_SEASON, clothingItem.season)
            put(COLUMN_LAST_WORN, clothingItem.lastWorn.time)
            put(COLUMN_IMAGE, clothingItem.image?.toString())
        }
        clothingItem.id = database.insert(TABLE_NAME, null, values)
    }

    override fun update(closetItem: ClosetOrganiserModel) {
        val values = toContentValues(closetItem)
        database.update(
            TABLE_NAME,
            values,
            "$COLUMN_ID = ?",
            arrayOf(closetItem.id.toString())
        )
        i("Updated item ${closetItem.id}")
    }

    override fun delete(clothingItem: ClosetOrganiserModel) {
        database.delete(
            TABLE_NAME,
            "$COLUMN_ID = ?",
            arrayOf(clothingItem.id.toString())
        )
        i("Deleted item ${clothingItem.id}")
    }

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

    private inner class ClosetDbHelper(context: Context) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE $TABLE_NAME (
                    $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_TITLE TEXT,
                    $COLUMN_DESCRIPTION TEXT,
                    $COLUMN_COLOUR_PATTERN TEXT,
                    $COLUMN_SIZE TEXT,
                    $COLUMN_SEASON TEXT,
                    $COLUMN_LAST_WORN INTEGER,
                    $COLUMN_IMAGE TEXT
                )
            """.trimIndent())
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
            onCreate(db)
        }
    }
}