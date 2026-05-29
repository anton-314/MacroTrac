package dev.antonlammers.macrotrac.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.antonlammers.macrotrac.data.local.dao.DailyGoalDao
import dev.antonlammers.macrotrac.data.local.dao.FoodEntryDao
import dev.antonlammers.macrotrac.data.local.dao.WeightEntryDao
import dev.antonlammers.macrotrac.data.local.entity.DailyGoalEntity
import dev.antonlammers.macrotrac.data.local.entity.FoodEntryEntity
import dev.antonlammers.macrotrac.data.local.entity.WeightEntryEntity

@Database(
    entities = [FoodEntryEntity::class, DailyGoalEntity::class, WeightEntryEntity::class],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodEntryDao(): FoodEntryDao
    abstract fun dailyGoalDao(): DailyGoalDao
    abstract fun weightEntryDao(): WeightEntryDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE food_entries ADD COLUMN sugarG REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE food_entries ADD COLUMN fiberG REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE food_entries ADD COLUMN mealCategory TEXT NOT NULL DEFAULT 'SNACK'")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS weight_entries (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        weightKg REAL NOT NULL,
                        date TEXT NOT NULL,
                        timestampMs INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_weight_entries_date ON weight_entries (date)")
            }
        }
    }
}
