package com.vinithius.poke10.datasource.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "pokemon_database.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_POKEMON = "POKEMON"
        const val TABLE_EVOLUTION = "EVOLUTION"
        const val TABLE_TYPE = "TYPE"

        private const val CREATE_TABLE_EVOLUTION = """
            CREATE TABLE $TABLE_EVOLUTION (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                stage TEXT NOT NULL
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
                type_id INTEGER,
                evolution_id INTEGER,
                image_path TEXT,
                FOREIGN KEY(type_id) REFERENCES $TABLE_TYPE(id),
                FOREIGN KEY(evolution_id) REFERENCES $TABLE_EVOLUTION(id)
            )
        """
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_TABLE_EVOLUTION)
        db?.execSQL(CREATE_TABLE_TYPE)
        db?.execSQL(CREATE_TABLE_POKEMON)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_POKEMON")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EVOLUTION")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TYPE")
        onCreate(db)
    }

}
