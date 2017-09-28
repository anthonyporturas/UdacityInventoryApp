package com.example.anthony.electronicsinventory.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.anthony.electronicsinventory.data.ItemContract.ItemEntry;
/**
 * Created by Anthony on 9/12/2017.
 */
public class ItemDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = ItemDbHelper.class.getSimpleName();

    /** Name of the database file */
    private static final String DATABASE_NAME = "store.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link ItemDbHelper}.
     *
     * @param context of the app
     */
    public ItemDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the pets table
        String SQL_CREATE_ELECTRONICS_TABLE =  "CREATE TABLE " + ItemEntry.TABLE_NAME + " ("
                + ItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ItemEntry.COLUMN_ELECTRONIC_NAME + " TEXT NOT NULL, "
                + ItemEntry.COLUMN_ELECTRONIC_SUPPLIER + " TEXT, "
                + ItemEntry.COLUMN_ELECTRONIC_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + ItemEntry.COLUMN_ELECTRONIC_PRICE + " INTEGER NOT NULL DEFAULT 0, "
                + ItemEntry.COLUMN_EMAIL+ " TEXT NOT NULL, "
                + ItemEntry.COLUMN_IMAGE + " TEXT NOT NULL);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_ELECTRONICS_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }

    public Cursor getItem(){
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ELECTRONIC_NAME,
                ItemEntry.COLUMN_ELECTRONIC_SUPPLIER,
                ItemEntry.COLUMN_ELECTRONIC_QUANTITY,
                ItemEntry.COLUMN_ELECTRONIC_PRICE,
                ItemEntry.COLUMN_EMAIL,
                ItemEntry.COLUMN_IMAGE
        };

        Cursor cursor = db.query(
                ItemEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );
        return cursor;
    }

    public void decrementQuantityByOne(long id, int quantity){
        SQLiteDatabase db = getWritableDatabase();
        if (quantity > 0){
            quantity--;
        }
        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_ELECTRONIC_QUANTITY, quantity);
        String selection = ItemEntry._ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(id)};
        db.update(ItemEntry.TABLE_NAME, values, selection, selectionArgs);
    }
}
