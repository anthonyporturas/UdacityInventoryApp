package com.example.anthony.electronicsinventory;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.anthony.electronicsinventory.data.ItemContract.ItemEntry;
/**
 * Created by Anthony on 9/12/2017.
 */
public class ItemCursorAdapter  extends CursorAdapter {

    private final CatalogActivity parent;
    /**
     * Constructs a new {@link ItemCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ItemCursorAdapter(CatalogActivity context, Cursor c) {
        super(context, c, 0 /* flags */);
        this.parent = context;
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }



    /**
     * This method binds the electronic data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current electronic can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView supplierTextView = (TextView) view.findViewById(R.id.supplier);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        ImageView image = (ImageView) view.findViewById(R.id.item_image);

        // Find the columns of electronic attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ELECTRONIC_NAME);
        int supplierColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ELECTRONIC_SUPPLIER);
        int priceColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ELECTRONIC_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ELECTRONIC_QUANTITY);
        int imageColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_IMAGE);
        // Read the electronic attributes from the Cursor for the current electronic
        String electronicName = cursor.getString(nameColumnIndex);
        String electronicSupplier = cursor.getString(supplierColumnIndex);
        String electronicPrice =  "$" + cursor.getString(priceColumnIndex);
        final int quantity = cursor.getInt(quantityColumnIndex);
        String electronicQuantity = "Quantity: "+quantity;
        image.setImageURI(Uri.parse(cursor.getString(imageColumnIndex)));

        // If the electronic supplier is empty string or null, then use some default text
        // that says "Unknown supplier", so the TextView isn't blank.
        if (TextUtils.isEmpty(electronicSupplier)) {
            electronicSupplier = context.getString(R.string.unknown_supplier);
        }

        // Update the TextViews with the attributes for the current electronic
        nameTextView.setText(electronicName);
        supplierTextView.setText(electronicSupplier);
        priceTextView.setText(electronicPrice);
        quantityTextView.setText(electronicQuantity);

        final long id = cursor.getLong(cursor.getColumnIndex(ItemEntry._ID));
        Button sale = (Button) view.findViewById(R.id.sale);

        sale.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                parent.decrementQuantity(id,quantity);
            }
        });

        view.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                parent.itemDetails(id);
            }
        });
    }



}