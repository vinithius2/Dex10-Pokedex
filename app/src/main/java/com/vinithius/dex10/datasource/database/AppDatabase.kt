package com.vinithius.dex10.datasource.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        PokemonEntity::class,
        Type::class,
        Stat::class,
        Ability::class,
        PokemonType::class,
        PokemonStat::class,
        PokemonAbility::class,
        TeamEntity::class,
        TeamMemberEntity::class
    ],
    version = 5
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun pokemonDao(): PokemonDao
    abstract fun teamDao(): TeamDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `team` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `created_at` INTEGER NOT NULL)")
                database.execSQL("CREATE TABLE IF NOT EXISTS `team_member` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `team_id` INTEGER NOT NULL, `pokemon_id` INTEGER NOT NULL, `position` INTEGER NOT NULL, FOREIGN KEY(`team_id`) REFERENCES `team`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, FOREIGN KEY(`pokemon_id`) REFERENCES `pokemon`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_team_member_team_id` ON `team_member` (`team_id`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_team_member_pokemon_id` ON `team_member` (`pokemon_id`)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `team_member` ADD COLUMN `nickname` TEXT")
                database.execSQL("ALTER TABLE `team_member` ADD COLUMN `ability` TEXT")
                database.execSQL("ALTER TABLE `team_member` ADD COLUMN `nature` TEXT")
                database.execSQL("ALTER TABLE `team_member` ADD COLUMN `item` TEXT")
                database.execSQL("ALTER TABLE `team_member` ADD COLUMN `move1` TEXT")
                database.execSQL("ALTER TABLE `team_member` ADD COLUMN `move2` TEXT")
                database.execSQL("ALTER TABLE `team_member` ADD COLUMN `move3` TEXT")
                database.execSQL("ALTER TABLE `team_member` ADD COLUMN `move4` TEXT")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `team` ADD COLUMN `format` TEXT DEFAULT '6v6'")
                database.execSQL("ALTER TABLE `team` ADD COLUMN `notes` TEXT")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `team_member` ADD COLUMN `ivs` TEXT")
                database.execSQL("ALTER TABLE `team_member` ADD COLUMN `evs` TEXT")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pokemon_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
