package ie.setu.project.models

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import timber.log.Timber.i
import java.util.Date

// SQLite database constants
private const val DATABASE_NAME = "closet.db"
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
        val query = "SELECT * FROM $TABLE_NAME"
        val cursor = database.rawQuery(query, null)

        val items = ArrayList<ClosetOrganiserModel>()

        cursor.use {
            while (it.moveToNext()) {
                items.add(
                    ClosetOrganiserModel(
                        id = it.getLong(0),
                        title = it.getString(1),
                        description = it.getString(2),
                        colourPattern = it.getString(3),
                        size = it.getString(4),
                        season = it.getString(5),
                        lastWorn = Date(it.getLong(6)), // Storing as timestamp
                        image = Uri.parse(it.getString(7))
                    )
                )
            }
        }

        i("closetdb : findAll() - has ${items.size} records")
        return items
    }

    override fun findById(id: Long): ClosetOrganiserModel? {
        TODO("Not yet implemented")
    }

    override fun create(item: ClosetOrganiserModel) {
        TODO("Not yet implemented")
    }

    override fun update(item: ClosetOrganiserModel) {
        TODO("Not yet implemented")
    }

    override fun delete(item: ClosetOrganiserModel) {
        TODO("Not yet implemented")
    }
}

private class ClosetDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {

    private val createTableSQL =
        "CREATE TABLE $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_TITLE TEXT, $COLUMN_DESCRIPTION TEXT, " +
                "$COLUMN_COLOUR_PATTERN TEXT, $COLUMN_SIZE TEXT, " +
                "$COLUMN_SEASON TEXT, $COLUMN_LAST_WORN INTEGER, " + // Storing date as timestamp
                "$COLUMN_IMAGE TEXT)" // Storing URI as string

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(createTableSQL)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }
}