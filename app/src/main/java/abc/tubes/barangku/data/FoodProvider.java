package abc.tubes.barangku.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import abc.tubes.barangku.Utils;

/**
 * Content provider for food database.
 *
 * Created by User on 1/4/2018.
 */

public class FoodProvider extends ContentProvider {
    private FoodDbHelper mDbHelper;

    private static final String ID_SELECTION = FoodContract.FoodEntry._ID + "=?";

    // match codes
    private static final int FOOD = 100;
    private static final int FOOD_ID = 101;

    // initialize the matcher and set the match cases
    private static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        // match case for whole food table
        sUriMatcher.addURI(FoodContract.CONTENT_AUTHORITY, FoodContract.PATH_FOOD, FOOD);
        // match case for single food item
        sUriMatcher.addURI(FoodContract.CONTENT_AUTHORITY, FoodContract.PATH_FOOD + "/#", FOOD_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new FoodDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // match the uri
        int match = sUriMatcher.match(uri);
        switch (match) {
            case FOOD:
                // nothing needs to be changed for this case
                break;
            case FOOD_ID:
                selection = ID_SELECTION;
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                break;
            default:
                throw new IllegalArgumentException("Invalid URI: " + uri);
        }

        Cursor cursor = db.query(FoodContract.FoodEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);

        // set notification uri on the cursor, so we know what the cursor was created for
        // if the data at this uri changes, then we know to update the cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // validate the data
        validateData(values, true);

        values = convertForStorage(values);

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // match the uri
        int match = sUriMatcher.match(uri);
        switch (match) {
            case FOOD:
                long id = db.insert(FoodContract.FoodEntry.TABLE_NAME, null, values);

                if (id != -1) getContext().getContentResolver().notifyChange(uri, null);

                return ContentUris.withAppendedId(uri, id);
            default:
                throw new IllegalArgumentException("Invalid URI: " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // match the uri
        int match = sUriMatcher.match(uri);
        switch (match) {
            case FOOD:
                // delete all the entries
                // we don't actually need to change anything here
                break;
            case FOOD_ID:
                // delete a single entry
                selection = ID_SELECTION;
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                break;
            default:
                throw new IllegalArgumentException("Invalid URI: " + uri);
        }

        int rowsAffected = db.delete(FoodContract.FoodEntry.TABLE_NAME, selection, selectionArgs);

        // notify any listeners
        if (rowsAffected > 0) getContext().getContentResolver().notifyChange(uri, null);

        return rowsAffected;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        validateData(values, false);

        values = convertForStorage(values);

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // match the uri
        int match = sUriMatcher.match(uri);
        switch (match) {
            case FOOD_ID:
                selection = ID_SELECTION;
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                break;
            default:
                throw new IllegalArgumentException("Invalid URI: " + uri);
        }

        int rowsAffected = db.update(FoodContract.FoodEntry.TABLE_NAME, values, selection, selectionArgs);

        // notify any listeners
        if (rowsAffected > 0) getContext().getContentResolver().notifyChange(uri, null);

        return rowsAffected;
    }

    /**
     * Converts amount and price to "absolute" values before storing.
     * Each unit type has an absolute unit to aid with conversion. The stored amount will
     * be in this unit (e.g. mL for volume), and the stored price will be the price per absolute
     * value.
     *
     * @param values values to check for conversion
     * @return values that are ready for storage
     */
    private ContentValues convertForStorage(ContentValues values) {
        if (values.containsKey(FoodContract.FoodEntry.COLUMN_UNIT) &&
                values.containsKey(FoodContract.FoodEntry.COLUMN_AMOUNT)) {
            double amount = values.getAsDouble(FoodContract.FoodEntry.COLUMN_AMOUNT);
            int unit = values.getAsInteger(FoodContract.FoodEntry.COLUMN_UNIT);

            // if available, convert price
            if (values.containsKey(FoodContract.FoodEntry.COLUMN_PRICE_PER)) {
                double price = values.getAsDouble(FoodContract.FoodEntry.COLUMN_PRICE_PER);
                price /= amount;
                values.put(FoodContract.FoodEntry.COLUMN_PRICE_PER, Utils.convert(price, unit, false, getContext()));
            }

            values.put(FoodContract.FoodEntry.COLUMN_AMOUNT, Utils.convert(amount, unit, true, getContext()));
        }
        return values;
    }

    /**
     * Validates the data before inserting into the db.
     *
     * Name, amount, and unit must exist and be of appropriate type.
     *
     * Not super necessary as the app UI will validate this also, but JIC.
     *
     * @param values set of values to insert or update
     * @param inserting true if it is an insert operation, false if it is an update
     */
    private void validateData(ContentValues values, boolean inserting) {
        // if we're inserting, then we must have the values
        // if we're updating, we can use the old values for any that were unfilled

        // check name
        if (inserting || values.containsKey(FoodContract.FoodEntry.COLUMN_NAME)) {
            String nameString = values.getAsString(FoodContract.FoodEntry.COLUMN_NAME);
            if (nameString == null || TextUtils.isEmpty(nameString))
                throw new IllegalArgumentException("Name must have a value.");
        }

        // check amount
        if (inserting || values.containsKey(FoodContract.FoodEntry.COLUMN_AMOUNT)) {
            Float amount = values.getAsFloat(FoodContract.FoodEntry.COLUMN_AMOUNT);
            if (amount == null || amount < 0)
                throw new IllegalArgumentException("Amount must be non-negative.");
        }

        // check unit
        if (inserting || values.containsKey(FoodContract.FoodEntry.COLUMN_UNIT)) {
            Integer unit = values.getAsInteger(FoodContract.FoodEntry.COLUMN_UNIT);
            if (unit == null)
                throw new IllegalArgumentException("Unit must have a value.");
        }
    }
}
