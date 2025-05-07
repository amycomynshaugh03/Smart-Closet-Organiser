// package ie.setu.project.models
//
// import android.content.ContentValues
// import android.content.Context
// import android.database.Cursor
// import android.database.sqlite.SQLiteDatabase
// import android.database.sqlite.SQLiteOpenHelper
// import android.net.Uri
// import ie.setu.project.models.clothing.ClosetOrganiserModel
// import ie.setu.project.models.outfit.OutfitModel
// import ie.setu.project.models.outfit.OutfitStore
// import timber.log.Timber.i
// import java.util.Date
//
// private const val DATABASE_NAME = "closet.db" // Should match ClosetSQLStore
// private const val DATABASE_VERSION = 1
// private const val OUTFIT_TABLE = "outfits"
// private const val COLUMN_ID = "id"
// private const val COLUMN_TITLE = "title"
// private const val COLUMN_DESCRIPTION = "description"
// private const val COLUMN_SEASON = "season"
// private const val COLUMN_LAST_WORN = "last_worn"
//
// private const val JUNCTION_TABLE = "outfit_clothing"
// private const val COLUMN_OUTFIT_ID = "outfit_id"
// private const val COLUMN_CLOTHING_ID = "clothing_id"
//
// private const val CLOTHING_TABLE = "clothing_items"
// private const val CLOTHING_COLUMN_ID = "id"
// private const val CLOTHING_COLUMN_TITLE = "title"
//
// class OutfitSQLStore(private val context: Context) : OutfitStore {
//
//    private lateinit var database: SQLiteDatabase
//    private val dbHelper = OutfitDbHelper(context)
//
//    // Add this initialization method
//    fun initialize() {
//        database = dbHelper.writableDatabase
//        // Verify tables exist
//        verifyTables()
//    }
//
//    private fun verifyTables() {
//        val tables = listOf(OUTFIT_TABLE, JUNCTION_TABLE)
//        tables.forEach { tableName ->
//            if (!isTableExists(tableName)) {
//                throw IllegalStateException("Table $tableName doesn't exist")
//            }
//        }
//    }
//
//    private fun isTableExists(tableName: String): Boolean {
//        database.rawQuery(
//            "SELECT name FROM sqlite_master WHERE type='table' AND name='$tableName'",
//            null
//        ).use { cursor ->
//            return cursor.moveToFirst()
//        }
//    }
//
//    override fun findAll(): List<OutfitModel> {
//        val outfits = mutableListOf<OutfitModel>()
//        database.rawQuery("SELECT * FROM $OUTFIT_TABLE ORDER BY $COLUMN_TITLE", null).use { cursor ->
//            while (cursor.moveToNext()) {
//                val outfit = createOutfitFromCursor(cursor)
//                outfit.clothingItems = getClothingItemsForOutfit(outfit.id)
//                outfits.add(outfit)
//            }
//        }
//        i("Retrieved ${outfits.size} outfits from database")
//        return outfits
//    }
//
//    override fun create(outfit: OutfitModel) {
//        val values = ContentValues().apply {
//            put(COLUMN_TITLE, outfit.title)
//            put(COLUMN_DESCRIPTION, outfit.description)
//            put(COLUMN_SEASON, outfit.season)
//            put(COLUMN_LAST_WORN, outfit.lastWorn.time)
//        }
//
//        database.beginTransaction()
//        try {
//            outfit.id = database.insert(OUTFIT_TABLE, null, values)
//            i("Created new outfit with ID ${outfit.id}")
//
//            for (item in outfit.clothingItems) {
//                val junctionValues = ContentValues().apply {
//                    put(COLUMN_OUTFIT_ID, outfit.id)
//                    put(COLUMN_CLOTHING_ID, item.id)
//                }
//                database.insert(JUNCTION_TABLE, null, junctionValues)
//                i("Added clothing item ${item.id} to outfit ${outfit.id}")
//            }
//
//            database.setTransactionSuccessful()
//        } finally {
//            database.endTransaction()
//        }
//    }
//
//    override fun update(outfit: OutfitModel) {
//        val values = ContentValues().apply {
//            put(COLUMN_TITLE, outfit.title)
//            put(COLUMN_DESCRIPTION, outfit.description)
//            put(COLUMN_SEASON, outfit.season)
//            put(COLUMN_LAST_WORN, outfit.lastWorn.time)
//        }
//
//        database.beginTransaction()
//        try {
//            val rowsAffected = database.update(
//                OUTFIT_TABLE,
//                values,
//                "$COLUMN_ID = ?",
//                arrayOf(outfit.id.toString())
//            )
//            i("Updated outfit ${outfit.id}. Rows affected: $rowsAffected")
//
//            val itemsDeleted = database.delete(
//                JUNCTION_TABLE,
//                "$COLUMN_OUTFIT_ID = ?",
//                arrayOf(outfit.id.toString())
//            )
//            i("Removed $itemsDeleted clothing items from outfit ${outfit.id}")
//
//            for (item in outfit.clothingItems) {
//                val junctionValues = ContentValues().apply {
//                    put(COLUMN_OUTFIT_ID, outfit.id)
//                    put(COLUMN_CLOTHING_ID, item.id)
//                }
//                database.insert(JUNCTION_TABLE, null, junctionValues)
//                i("Added clothing item ${item.id} to outfit ${outfit.id}")
//            }
//
//            database.setTransactionSuccessful()
//        } finally {
//            database.endTransaction()
//        }
//    }
//
//    override fun delete(outfit: OutfitModel) {
//        database.beginTransaction()
//        try {
//            val itemsDeleted = database.delete(
//                JUNCTION_TABLE,
//                "$COLUMN_OUTFIT_ID = ?",
//                arrayOf(outfit.id.toString())
//            )
//            i("Deleted $itemsDeleted clothing items from outfit ${outfit.id}")
//
//            val outfitDeleted = database.delete(
//                OUTFIT_TABLE,
//                "$COLUMN_ID = ?",
//                arrayOf(outfit.id.toString())
//            )
//            i("Deleted outfit ${outfit.id}. Rows deleted: $outfitDeleted")
//
//            database.setTransactionSuccessful()
//        } finally {
//            database.endTransaction()
//        }
//    }
//
//    private fun createOutfitFromCursor(cursor: Cursor): OutfitModel {
//        return OutfitModel(
//            id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
//            title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
//            description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
//            season = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SEASON)),
//            lastWorn = Date(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_LAST_WORN)))
//        )
//    }
//
//    private fun getClothingItemsForOutfit(outfitId: Long): MutableList<ClosetOrganiserModel> {
//        val items = mutableListOf<ClosetOrganiserModel>()
//        val query = """
//            SELECT c.* FROM $JUNCTION_TABLE oc
//            JOIN $CLOTHING_TABLE c ON oc.$COLUMN_CLOTHING_ID = c.$CLOTHING_COLUMN_ID
//            WHERE oc.$COLUMN_OUTFIT_ID = ?
//            ORDER BY c.$CLOTHING_COLUMN_TITLE
//        """.trimIndent()
//
//        database.rawQuery(query, arrayOf(outfitId.toString())).use { cursor ->
//            while (cursor.moveToNext()) {
//                items.add(createClothingItemFromCursor(cursor))
//            }
//        }
//        i("Retrieved ${items.size} clothing items for outfit $outfitId")
//        return items
//    }
//
//    private fun createClothingItemFromCursor(cursor: Cursor): ClosetOrganiserModel {
//        return ClosetOrganiserModel(
//            id = cursor.getLong(cursor.getColumnIndexOrThrow(CLOTHING_COLUMN_ID)),
//            title = cursor.getString(cursor.getColumnIndexOrThrow(CLOTHING_COLUMN_TITLE)),
//            description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
//            colourPattern = cursor.getString(cursor.getColumnIndexOrThrow("colour_pattern")),
//            size = cursor.getString(cursor.getColumnIndexOrThrow("size")),
//            season = cursor.getString(cursor.getColumnIndexOrThrow("season")),
//            lastWorn = Date(cursor.getLong(cursor.getColumnIndexOrThrow("last_worn"))),
//            image = Uri.parse(cursor.getString(cursor.getColumnIndexOrThrow("image")))
//        )
//    }
//    private inner class OutfitDbHelper(context: Context) :
//        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
//
//        override fun onConfigure(db: SQLiteDatabase) {
//            super.onConfigure(db)
//            db.setForeignKeyConstraintsEnabled(true)
//        }
//
//        override fun onCreate(db: SQLiteDatabase) {
//            try {
//                // Create outfits table
//                db.execSQL("""
//                    CREATE TABLE $OUTFIT_TABLE (
//                        $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
//                        $COLUMN_TITLE TEXT NOT NULL,
//                        $COLUMN_DESCRIPTION TEXT,
//                        $COLUMN_SEASON TEXT,
//                        $COLUMN_LAST_WORN INTEGER
//                    )
//                """.trimIndent())
//
//                // Create junction table
//                db.execSQL("""
//                    CREATE TABLE $JUNCTION_TABLE (
//                        $COLUMN_OUTFIT_ID INTEGER,
//                        $COLUMN_CLOTHING_ID INTEGER,
//                        PRIMARY KEY ($COLUMN_OUTFIT_ID, $COLUMN_CLOTHING_ID),
//                        FOREIGN KEY ($COLUMN_OUTFIT_ID) REFERENCES $OUTFIT_TABLE($COLUMN_ID),
//                        FOREIGN KEY ($COLUMN_CLOTHING_ID) REFERENCES $CLOTHING_TABLE($CLOTHING_COLUMN_ID)
//                    )
//                """.trimIndent())
//
//                i("Successfully created database tables")
//            } catch (e: Exception) {
//                i("FATAL ERROR creating tables: ${e.message}")
//                throw RuntimeException("Failed to create database tables", e)
//            }
//        }
//
//        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
//            try {
//                db.execSQL("DROP TABLE IF EXISTS $JUNCTION_TABLE")
//                db.execSQL("DROP TABLE IF EXISTS $OUTFIT_TABLE")
//                onCreate(db)
//                i("Database upgraded from $oldVersion to $newVersion")
//            } catch (e: Exception) {
//                i("FATAL ERROR upgrading database: ${e.message}")
//                throw RuntimeException("Failed to upgrade database", e)
//            }
//        }
//    }
// }
//
