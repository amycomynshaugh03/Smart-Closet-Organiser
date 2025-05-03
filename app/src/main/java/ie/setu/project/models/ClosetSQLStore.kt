package ie.setu.project.models

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


// SQLite database constants
private const val DATABASE_NAME = "closet.db"
private const val TABLE_NAME = "clothing_items"
private const val COLUMN_ID = "id"
private const val COLUMN_NAME = "name"
private const val COLUMN_DESCRIPTION = "description"
private const val COLUMN_IMAGE = "image"
private const val COLUMN_CATEGORY = "category"
private const val COLUMN_LAST_WORN = "last_worn"

class ClosetSQLStore(private val context: Context) : ClothingStore {

    private var database: SQLiteDatabase

    init {
        database = ClosetDbHelper(context).writableDatabase
    }

    override fun findAll(): List<ClosetOrganiserModel> {
        TODO("Not yet implemented")
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
                "$COLUMN_NAME TEXT, $COLUMN_DESCRIPTION TEXT, $COLUMN_IMAGE TEXT, " +
                "$COLUMN_CATEGORY TEXT, $COLUMN_LAST_WORN TEXT)"

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(createTableSQL)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }
}