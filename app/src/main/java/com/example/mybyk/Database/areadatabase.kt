package com.example.mybyk.Database




import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class areadatabase(context: Context) : SQLiteOpenHelper(context, "MYBYKDatabase", null, 1) {

    override fun onCreate(db: SQLiteDatabase?) {

        db?.execSQL("CREATE TABLE area_table(id INTEGER PRIMARY KEY AUTOINCREMENT,  area_name TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

        db?.execSQL("DROP TABLE IF EXISTS area_table")
        onCreate(db)
    }

    // Method to insert data into area_table
    fun insertArea(areaName: String): Long {
        val db = writableDatabase
        val cv = ContentValues()

        cv.put("area_name", areaName)
        return db.insert("area_table", null, cv)
    }

    // Method to retrieve all areas
    fun getAllAreas(): Cursor {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM area_table", null)
    }

    // Method to update an area by ID
    fun updateArea(id: Int,  areaName: String): Int {
        val db = writableDatabase
        val cv = ContentValues()

        cv.put("area_name", areaName)
        return db.update("area_table", cv, "id = ?", arrayOf(id.toString()))
    }



    // Method to delete an area by ID
    fun deleteArea(id: Int): Int {
        val db = writableDatabase
        return db.delete("area_table", "id = ?", arrayOf(id.toString()))
    }
}
