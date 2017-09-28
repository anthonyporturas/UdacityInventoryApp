package com.example.anthony.electronicsinventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.app.LoaderManager;
import android.support.design.widget.FloatingActionButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.anthony.electronicsinventory.data.ItemContract.ItemEntry;
import com.example.anthony.electronicsinventory.data.ItemDbHelper;

public class CatalogActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the Item data loader
     */
    private static final int ITEM_LOADER = 0;

    /**
     * Adapter for the ListView
     */
    ItemCursorAdapter mCursorAdapter;
    ItemDbHelper dbHelper = new ItemDbHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);
        dbHelper = new ItemDbHelper(this);
        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the electronic data
        ListView itemListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        itemListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of electronic data in the Cursor.
        // There is no electronic data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new ItemCursorAdapter(this, null);
        itemListView.setAdapter(mCursorAdapter);



        // Kick off the loader
        getLoaderManager().initLoader(ITEM_LOADER, null, this);
    }

    /**
     * Helper method to insert hardcoded electronic data into the database. For debugging purposes only.
     */
    private void insertElectronic() {
        // Create a ContentValues object where column names are the keys,
        // and Toto's electronic attributes are the values.
        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_ELECTRONIC_NAME, "Earphones");
        values.put(ItemEntry.COLUMN_ELECTRONIC_SUPPLIER, "Bose");
        values.put(ItemEntry.COLUMN_ELECTRONIC_PRICE, 10);
        values.put(ItemEntry.COLUMN_ELECTRONIC_QUANTITY, 10);
        values.put(ItemEntry.COLUMN_EMAIL, "support@bose.com");
        values.put(ItemEntry.COLUMN_IMAGE, "android.resource://com.example.anthony.electronicsinventory/drawable/earphones");

        // Insert a new row for Toto into the provider using the ContentResolver.
        // Use the {@link ItemEntry#CONTENT_URI} to indicate that we want to insert
        // into the electronics database table.
        // Receive the new content URI that will allow us to access Toto's data in the future.
        Uri newUri = getContentResolver().insert(ItemEntry.CONTENT_URI, values);
    }

    /**
     * Helper method to delete all electronics in the database.
     */
    private void deleteAllElectronics() {
        int rowsDeleted = getContentResolver().delete(ItemEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from electronic database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertElectronic();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllElectronics();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ELECTRONIC_NAME,
                ItemEntry.COLUMN_ELECTRONIC_SUPPLIER,
                ItemEntry.COLUMN_ELECTRONIC_PRICE,
                ItemEntry.COLUMN_ELECTRONIC_QUANTITY,
                ItemEntry.COLUMN_EMAIL,
                ItemEntry.COLUMN_IMAGE
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                ItemEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link ItemCursorAdapter} with this new cursor containing updated electronic data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }


    public void decrementQuantity(long id, int quantity) {
        dbHelper.decrementQuantityByOne(id, quantity);
        mCursorAdapter.swapCursor(dbHelper.getItem());
    }

    public void itemDetails(long id){
        Intent intent = new Intent(this, EditorActivity.class);
        intent.putExtra("id",id);
        Uri currentItemUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, id);

        // Set the URI on the data field of the intent
        intent.setData(currentItemUri);
        startActivity(intent);
    }
}
