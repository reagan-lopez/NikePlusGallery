/**
    Copyright © 2014 Reagan Lopez
    [This program is licensed under the "MIT License"]
    Please see the file LICENSE in the source
    distribution of this software for license terms
*/
package com.nike.plusgps.nikeplusgallery;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Helper class containing the methods for CRUD operations in SQLite.
 */

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "GalleryDB.db";
    public static final int DATABASE_VERSION = 1;

    public static final String RESPONSE_TABLE_NAME = "response";
    public static final String RESPONSE_COLUMN_ID = "id";
    public static final String RESPONSE_COLUMN_TXT = "txt";

    public static final String MEDIA_TABLE_NAME = "media";
    public static final String MEDIA_COLUMN_ID = "id";
    public static final String MEDIA_COLUMN_URL = "url";
    public static final String MEDIA_COLUMN_TITLE = "title";


    private HashMap hp;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME , null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + RESPONSE_TABLE_NAME  +
                        " ( " + RESPONSE_COLUMN_ID + " INTEGER PRIMARY KEY, " +  RESPONSE_COLUMN_TXT + " TEXT )"
        );
        db.execSQL(
                "CREATE TABLE " + MEDIA_TABLE_NAME  +
                        " ( " + MEDIA_COLUMN_ID + " INTEGER PRIMARY KEY, " + MEDIA_COLUMN_URL + " TEXT, " +  MEDIA_COLUMN_TITLE + " TEXT )"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + RESPONSE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MEDIA_TABLE_NAME);
        onCreate(db);
    }

    public boolean insertResponse(String txt) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(RESPONSE_COLUMN_TXT, txt);
        db.insert(RESPONSE_TABLE_NAME, null, contentValues);
        return true;
    }

    public boolean insertMedia(String url, String title) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MEDIA_COLUMN_URL, url);
        contentValues.put(MEDIA_COLUMN_TITLE, title);
        db.insert(MEDIA_TABLE_NAME, null, contentValues);
        return true;
    }

    public boolean deleteAllResponse() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(RESPONSE_TABLE_NAME, null, null);
        return true;
    }

    public boolean deleteAllMedia() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(RESPONSE_TABLE_NAME, null, null);
        return true;
    }

    public Cursor getResponse(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT * FROM " + RESPONSE_TABLE_NAME + " WHERE " + RESPONSE_COLUMN_ID + " = " + id + "", null );
        return res;
    }

    public Cursor getMedia(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT * FROM " + MEDIA_TABLE_NAME + " WHERE " + MEDIA_COLUMN_ID + " = " + id + "", null );
        return res;
    }

    public int countResponse(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, RESPONSE_TABLE_NAME);
        return numRows;
    }

    public int countMedia(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, MEDIA_TABLE_NAME);
        return numRows;
    }

    public ArrayList getAllResponse() {
        ArrayList array_list = new ArrayList();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT * FROM " + RESPONSE_TABLE_NAME + "", null );
        res.moveToFirst();
        while(res.isAfterLast() == false){
            array_list.add(res.getString(res.getColumnIndex(RESPONSE_COLUMN_TXT)));
            res.moveToNext();
        }
        return array_list;
    }

    public ArrayList getAllMedia() {
        ArrayList array_list = new ArrayList();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT * FROM " + MEDIA_TABLE_NAME + "", null );
        res.moveToFirst();
        while(res.isAfterLast() == false){
            array_list.add(res.getString(res.getColumnIndex(MEDIA_COLUMN_URL)));
            res.moveToNext();
        }
        return array_list;
    }


}