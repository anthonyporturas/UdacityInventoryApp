package com.example.anthony.electronicsinventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import com.example.anthony.electronicsinventory.data.ItemContract.ItemEntry;
/**
 * Created by Anthony on 9/12/2017.
 */
public class ItemProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = ItemProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the electronics table
     */
    private static final int ELECTRONICS = 100;

    /**
     * URI matcher code for the content URI for a single electronic in the electronics table
     */
    private static final int ELECTRONIC_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.example.anthony.electronicsinventory/electronics" will map to the
        // integer code {@link #ELECTRONICS}. This URI is used to provide access to MULTIPLE rows
        // of the electronics table.
        sUriMatcher.addURI(ItemContract.CONTENT_AUTHORITY, ItemContract.PATH_ELECTRONICS, ELECTRONICS);

        // The content URI of the form "content://com.example.anthony.electronicsinventory/electronics/#" will map to the
        // integer code {@link #ELECTRONIC_ID}. This URI is used to provide access to ONE single row
        // of the electronics table.
        //
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.example.anthony.electronicsinventory/electronics/3" matches, but
        // "content://com.example.anthony.electronicsinventory/electronics" (without a number at the end) doesn't match.
        sUriMatcher.addURI(ItemContract.CONTENT_AUTHORITY, ItemContract.PATH_ELECTRONICS + "/#", ELECTRONIC_ID);
    }

    /**
     * Database helper object
     */
    private ItemDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new ItemDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case ELECTRONICS:
                // For the ELECTRONICS code, query the electronics table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the electronics table.
                cursor = database.query(ItemEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case ELECTRONIC_ID:
                // For the ELECTRONIC_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.anthony.electronicsinventory/electronics/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the electronics table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(ItemEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ELECTRONICS:
                return insertElectronic(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a electronic into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertElectronic(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(ItemEntry.COLUMN_ELECTRONIC_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Electronic requires a name");
        }

        // Check that the price is valid
        Integer price = values.getAsInteger(ItemEntry.COLUMN_ELECTRONIC_PRICE);
        if (price == null || price < 0) {
            throw new IllegalArgumentException("Electronic requires valid price");
        }

        // If the quantity is provided, check that it's greater than or equal to 0 kg
        Integer quantity = values.getAsInteger(ItemEntry.COLUMN_ELECTRONIC_QUANTITY);
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("Electronic requires valid quantity");
        }

        String email = values.getAsString(ItemEntry.COLUMN_EMAIL);
        if (email == null){
            throw new IllegalArgumentException("Supplier requires valid email");
        }

        String image = values.getAsString(ItemEntry.COLUMN_IMAGE);
        if (image == null){
            throw new IllegalArgumentException("Electronic requires valid image");
        }



        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new electronic with the given values
        long id = database.insert(ItemEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the electronic content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ELECTRONICS:
                return updateElectronic(uri, contentValues, selection, selectionArgs);
            case ELECTRONIC_ID:
                // For the ELECTRONIC_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateElectronic(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update electronics in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more electronics).
     * Return the number of rows that were successfully updated.
     */
    private int updateElectronic(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link ItemEntry#COLUMN_ELECTRONIC_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(ItemEntry.COLUMN_ELECTRONIC_NAME)) {
            String name = values.getAsString(ItemEntry.COLUMN_ELECTRONIC_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Pet requires a name");
            }
        }

        // If the {@link ItemEntry#COLUMN_ELECTRONIC_PRICE} key is present,
        // check that the price value is valid.
        if (values.containsKey(ItemEntry.COLUMN_ELECTRONIC_PRICE)) {
            Integer price = values.getAsInteger(ItemEntry.COLUMN_ELECTRONIC_PRICE);
            if (price == null || price < 0) {
                throw new IllegalArgumentException("Pet requires valid price");
            }
        }

        // If the {@link ItemEntry#COLUMN_ELECTRONIC_QUANTITY} key is present,
        // check that the quantity value is valid.
        if (values.containsKey(ItemEntry.COLUMN_ELECTRONIC_QUANTITY)) {
            // Check that the quantity is greater than or equal to 0 kg
            Integer quantity = values.getAsInteger(ItemEntry.COLUMN_ELECTRONIC_QUANTITY);
            if (quantity == null || quantity < 0) {
                throw new IllegalArgumentException("Pet requires valid quantity");
            }
        }



        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(ItemEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ELECTRONICS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(ItemEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ELECTRONIC_ID:
                // Delete a single row given by the ID in the URI
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(ItemEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ELECTRONICS:
                return ItemEntry.CONTENT_LIST_TYPE;
            case ELECTRONIC_ID:
                return ItemEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
