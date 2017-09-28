package com.example.anthony.electronicsinventory;

import com.example.anthony.electronicsinventory.data.ItemContract.ItemEntry;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.net.URI;

/**
 * Created by Anthony on 9/12/2017.
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final int PICK_IMAGE_REQUEST = 0;
    /** Identifier for the electronic data loader */
    private static final int EXISTING_ITEM_LOADER = 0;

    /** Content URI for the existing electronic (null if it's a new electronic) */
    private Uri mCurrentElectronicUri;

    /** EditText field to enter the electronic's name */
    private EditText mNameEditText;

    /** EditText field to enter the electronic's supplier */
    private EditText mSupplierEditText;

    /** EditText field to enter the electronic's price */
    private EditText mPriceEditText;

    /** EditText field to enter the electronic's quantity */
    private EditText mQuantityEditText;

    private EditText mEmailEditText;

    private String mSupplier = ItemEntry.UNKNOWN_SUPPLIER;

    ImageView imageView;

    /** Boolean flag that keeps track of whether the electronic has been edited (true) or not (false) */
    private boolean mItemHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mElectronicHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new electronic or editing an existing one.
        Intent intent = getIntent();
        mCurrentElectronicUri = intent.getData();
        long detailItemId = intent.getLongExtra("id",0);
        // If the intent DOES NOT contain a electronic content URI, then we know that we are
        // creating a new electronic.
        if (detailItemId == 0) {
            // This is a new electronic, so change the app bar to say "Add a Electronic"
            setTitle(getString(R.string.editor_activity_title_new_electronic));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a electronic that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing electronic, so change app bar to say "Edit Electronic"
            setTitle(getString(R.string.editor_activity_title_edit_electronic));

            // Initialize a loader to read the electronic data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_electronic_name);
        mSupplierEditText = (EditText) findViewById(R.id.edit_electronic_supplier);
        mPriceEditText = (EditText) findViewById(R.id.edit_electronic_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_electronic_quantity);
        mEmailEditText = (EditText) findViewById(R.id.edit_email);
        imageView = (ImageView) findViewById(R.id.edit_image);
        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mEmailEditText.setOnTouchListener(mTouchListener);
        imageView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                tryToOpenImageSelector();
                mItemHasChanged = true;
            }
        });
        Button decrease = (Button) findViewById(R.id.decrease);
        Button increase = (Button) findViewById(R.id.increase);

        decrease.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                decreaseByOne();
                mItemHasChanged = true;
            }
        });

        increase.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                increaseByOne();
                mItemHasChanged = true;
            }
        });

        Button order = (Button) findViewById(R.id.order);

        order.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                sendOrder();
            }
        });

    }
    public void sendOrder(){


        String[] emails = {mEmailEditText.getText().toString().trim()};
        String subject = "New Order: " + mNameEditText.getText().toString().trim();
        String message = "Please send another shipment.";

        Intent email = new Intent(Intent.ACTION_SEND);
        email.putExtra(Intent.EXTRA_EMAIL, emails);
        email.putExtra(Intent.EXTRA_SUBJECT, subject);
        email.putExtra(Intent.EXTRA_TEXT, message);

        // need this to prompts email client only
        email.setType("message/rfc822");

        startActivity(Intent.createChooser(email, "Choose an Email client :"));
    }

    public void decreaseByOne(){
        String currentQuantity = mQuantityEditText.getText().toString();

        if (currentQuantity.equals("0")){
            return;
        }
        mQuantityEditText.setText(String.valueOf(Integer.parseInt(currentQuantity)-1));
    }

    public void increaseByOne(){
        String currentQuantity = mQuantityEditText.getText().toString();
       mQuantityEditText.setText(String.valueOf(Integer.parseInt(currentQuantity)+1));
    }


    /**
     * Get user input from editor and save electronic into database.
     */
    private void saveElectronic() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String emailString = mEmailEditText.getText().toString().trim();
        String imageString = mCurrentElectronicUri.toString().trim();
        // Check if this is supposed to be a new electronic
        // and check if all the fields in the editor are blank
        if (mCurrentElectronicUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(quantityString) &&
                TextUtils.isEmpty(priceString) && TextUtils.isEmpty(supplierString) &&
                TextUtils.isEmpty(emailString)){
            // Since no fields were modified, we can return early without creating a new electronic.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and electronic attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_ELECTRONIC_NAME, nameString);
        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        values.put(ItemEntry.COLUMN_ELECTRONIC_QUANTITY, quantity);
        if (TextUtils.isEmpty(supplierString)){
            supplierString = mSupplier;
        }
        values.put(ItemEntry.COLUMN_ELECTRONIC_SUPPLIER, imageString);
        // If the price is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
        }
        values.put(ItemEntry.COLUMN_ELECTRONIC_PRICE, price);

        values.put(ItemEntry.COLUMN_EMAIL,emailString);

        values.put(ItemEntry.COLUMN_IMAGE,imageString);

        // Determine if this is a new or existing electronic by checking if mCurrentElectronicUri is null or not
        if (mCurrentElectronicUri == null) {
            // This is a NEW electronic, so insert a new electronic into the provider,
            // returning the content URI for the new electronic.
            Uri newUri = getContentResolver().insert(ItemEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_electronic_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_electronic_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING electronic, so update the electronic with content URI: mCurrentElectronicUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentElectronicUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentElectronicUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_electronic_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_electronic_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void tryToOpenImageSelector() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            return;
        }
        openImageSelector();
    }

    private void openImageSelector() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openImageSelector();
                    // permission was granted
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

            if (resultData != null) {
                mCurrentElectronicUri = resultData.getData();
                imageView.setImageURI(mCurrentElectronicUri);
                imageView.invalidate();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new electronic, hide the "Delete" menu item.
        if (mCurrentElectronicUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save electronic to database
                saveElectronic();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the electronic hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the electronic hasn't changed, continue with handling back button press
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all electronic attributes, define a projection that contains
        // all columns from the electronic table
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ELECTRONIC_NAME,
                ItemEntry.COLUMN_ELECTRONIC_SUPPLIER,
                ItemEntry.COLUMN_ELECTRONIC_QUANTITY,
                ItemEntry.COLUMN_ELECTRONIC_PRICE,
                ItemEntry.COLUMN_EMAIL,
                ItemEntry.COLUMN_IMAGE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentElectronicUri,         // Query the content URI for the current electronic
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of electronic attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ELECTRONIC_NAME);
            int supplierColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ELECTRONIC_SUPPLIER);
            int quantityColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ELECTRONIC_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ELECTRONIC_PRICE);
            int emailColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_EMAIL);
            int imageColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_IMAGE);
            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            String email = cursor.getString(emailColumnIndex);
            String image = cursor.getString(imageColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mSupplierEditText.setText(supplier);
            mQuantityEditText.setText(Integer.toString(quantity));
            mPriceEditText.setText(Integer.toString(price));
            mEmailEditText.setText(email);
            imageView.setImageURI(Uri.parse(image));

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mSupplierEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mEmailEditText.setText("");

    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the electronic.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this electronic.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the electronic.
                deleteElectronic();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the electronic.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the electronic in the database.
     */
    private void deleteElectronic() {
        // Only perform the delete if this is an existing electronic.
        if (mCurrentElectronicUri != null) {
            // Call the ContentResolver to delete the electronic at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentElectronicUri
            // content URI already identifies the electronic that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentElectronicUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_electronic_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_electronic_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }
}