package com.example.anthony.electronicsinventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Anthony on 9/12/2017.
 */
public final class ItemContract {

    private ItemContract() {}

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.anthony.electronicsinventory";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.anthony.electronicsinventory/electronics is a valid path for
     * looking at electronic data. content://com.example.anthony.electronicsinventory/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_ELECTRONICS = "electronics";

    /**
     * Inner class that defines constant values for the electronics database table.
     * Each entry in the table represents a single electronic.
     */
    public static final class ItemEntry implements BaseColumns {

        /** The content URI to access the electronics data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ELECTRONICS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of electronics.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ELECTRONICS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single electronic.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ELECTRONICS;

        /** Name of database table for electronics */
        public final static String TABLE_NAME = "electronics";

        /**
         * Unique ID number for the electronic (only for use in the database table).
         *
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the electronic.
         *
         * Type: TEXT
         */
        public final static String COLUMN_ELECTRONIC_NAME ="name";

        /**
         * Supplier of the electronic.
         *
         * Type: TEXT
         */
        public final static String COLUMN_ELECTRONIC_SUPPLIER = "supplier";
        public final static String UNKNOWN_SUPPLIER = "Unknown";
        /**
         * Quantity of the electronic.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_ELECTRONIC_QUANTITY = "quantity";

        /**
         * Price of the electronic.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_ELECTRONIC_PRICE = "price";

        public final static String COLUMN_IMAGE = "image";

        public final static String COLUMN_EMAIL = "email";




    }

}
