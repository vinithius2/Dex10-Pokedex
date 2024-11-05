package com.vinithius.poke10.datasource.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "pokemon_database.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_POKEMON = "pokemon"
        const val TABLE_STAT = "stats"
        const val TABLE_ABILITY = "abilities"
        const val TABLE_TYPE = "types"
        const val TABLE_POKEMON_STAT = "pokemon_stat"
        const val TABLE_POKEMON_ABILITY = "pokemon_ability"
        const val TABLE_POKEMON_TYPE = "pokemon_type"

        private const val CREATE_TABLE_STAT = """
            CREATE TABLE $TABLE_STAT (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                base_stat INTEGER NOT NULL,
                effort INTEGER NOT NULL
            )
        """

        private const val CREATE_TABLE_ABILITY = """
            CREATE TABLE $TABLE_ABILITY (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                is_hidden INTEGER NOT NULL, -- 1 for true, 0 for false
                slot INTEGER NOT NULL
            )
        """

        private const val CREATE_TABLE_TYPE = """
            CREATE TABLE $TABLE_TYPE (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                type_name TEXT NOT NULL
            )
        """

        private const val CREATE_TABLE_POKEMON = """
            CREATE TABLE $TABLE_POKEMON (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                image_path TEXT
            )
        """

        private const val CREATE_TABLE_POKEMON_STAT = """
            CREATE TABLE $TABLE_POKEMON_STAT (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                pokemon_id INTEGER NOT NULL,
                stat_id INTEGER NOT NULL,
                FOREIGN KEY(pokemon_id) REFERENCES $TABLE_POKEMON(id),
                FOREIGN KEY(stat_id) REFERENCES $TABLE_STAT(id)
            )
        """

        private const val CREATE_TABLE_POKEMON_ABILITY = """
            CREATE TABLE $TABLE_POKEMON_ABILITY (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                pokemon_id INTEGER NOT NULL,
                ability_id INTEGER NOT NULL,
                FOREIGN KEY(pokemon_id) REFERENCES $TABLE_POKEMON(id),
                FOREIGN KEY(ability_id) REFERENCES $TABLE_ABILITY(id)
            )
        """

        private const val CREATE_TABLE_POKEMON_TYPE = """
            CREATE TABLE $TABLE_POKEMON_TYPE (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                pokemon_id INTEGER NOT NULL,
                type_id INTEGER NOT NULL,
                FOREIGN KEY(pokemon_id) REFERENCES $TABLE_POKEMON(id),
                FOREIGN KEY(type_id) REFERENCES $TABLE_TYPE(id)
            )
        """
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_TABLE_STAT)
        db?.execSQL(CREATE_TABLE_ABILITY)
        db?.execSQL(CREATE_TABLE_TYPE)
        db?.execSQL(CREATE_TABLE_POKEMON)
        db?.execSQL(CREATE_TABLE_POKEMON_STAT)
        db?.execSQL(CREATE_TABLE_POKEMON_ABILITY)
        db?.execSQL(CREATE_TABLE_POKEMON_TYPE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_POKEMON_ABILITY")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_POKEMON_STAT")
        db.execSQL("DROP TABLE IF EXISTS $CREATE_TABLE_POKEMON_TYPE")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_POKEMON")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TYPE")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ABILITY")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_STAT")
        onCreate(db)
    }
}
