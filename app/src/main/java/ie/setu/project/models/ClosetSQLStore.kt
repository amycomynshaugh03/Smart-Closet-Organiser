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
        database.rawQuery("SELECT * FROM $TABLE_NAME ORDER BY $COLUMN_TITLE", null).use { cursor ->
            while (cursor.moveToNext()) {
                items.add(createFromCursor(cursor))
            }
        }
        i("Retrieved ${items.size} clothing items from database")
        return items
    }

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

    override fun create(clothingItem: ClosetOrganiserModel) {
        val values = toContentValues(clothingItem)
        clothingItem.id = database.insert(TABLE_NAME, null, values)
        i("Created new clothing item with ID ${clothingItem.id}")
    }

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

    override fun delete(clothingItem: ClosetOrganiserModel) {
        val rowsDeleted = database.delete(
            TABLE_NAME,
            "$COLUMN_ID = ?",
            arrayOf(clothingItem.id.toString())
        )
        i("Deleted clothing item ${clothingItem.id}. Rows deleted: $rowsDeleted")
    }

    private fun createFromCursor(cursor: Cursor): ClosetOrganiserModel {
        return ClosetOrganiserModel(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
            title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
            description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
            colourPattern = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COLOUR_PATTERN)),
            size = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SIZE)),
            season = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SEASON)),
            lastWorn = Date(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_LAST_WORN))),
            image = Uri.parse(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE))))
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
                    $COLUMN_TITLE TEXT NOT NULL,
                    $COLUMN_DESCRIPTION TEXT,
                    $COLUMN_COLOUR_PATTERN TEXT,
                    $COLUMN_SIZE TEXT,
                    $COLUMN_SEASON TEXT,
                    $COLUMN_LAST_WORN INTEGER,
                    $COLUMN_IMAGE TEXT
                )
            """.trimIndent())
            i("Created database table $TABLE_NAME")
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
            i("Upgraded database from version $oldVersion to $newVersion")
            onCreate(db)
        }
    }
}