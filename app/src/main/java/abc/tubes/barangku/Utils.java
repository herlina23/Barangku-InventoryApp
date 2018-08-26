package abc.tubes.barangku;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import abc.tubes.barangku.data.FoodDbHelper;
import abc.tubes.barangku.data.UnitContract;

/**
 * Utility functions.
 *
 * Created by User on 1/4/2018.
 */

public final class Utils {
    // not instantiable
    private Utils() {}

    /**
     * Converts amounts to or from the "absolute" units.
     *
     * @param amount to convert
     * @param unit units to convert from or to
     * @param forStorage if true, will convert to "absolute" units. If false, will convert to unit.
     * @param context
     * @return converted amount
     */
    public static double convert(double amount, int unit, boolean forStorage, Context context) {
        // get conversion factor for unit in question
        FoodDbHelper dbHelper = new FoodDbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = new String[] {UnitContract.UnitEntry.COLUMN_CONVERT};
        String selection = UnitContract.UnitEntry._ID + "=?";
        String[] selectionArgs = new String[] {String.valueOf(unit) };
        Cursor c = db.query(UnitContract.UnitEntry.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
        c.moveToFirst();
        double conversionFactor = c.getDouble(c.getColumnIndex(UnitContract.UnitEntry.COLUMN_CONVERT));

        c.close();
        db.close();

        if (forStorage) {
            return amount * conversionFactor;
        } else {
            return amount / conversionFactor;
        }
    }

    /**
     * Get the user-readable name of the unit.
     *
     * @param unit to find
     * @param context
     * @return name of the unit
     */
    public static String getUnitString(int unit, Context context) {
        FoodDbHelper dbHelper = new FoodDbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = new String[]{UnitContract.UnitEntry.COLUMN_NAME};
        String selection = UnitContract.UnitEntry._ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(unit)};
        Cursor c = db.query(UnitContract.UnitEntry.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
        c.moveToFirst();
        String unitString = c.getString(c.getColumnIndex(UnitContract.UnitEntry.COLUMN_NAME));

        c.close();
        db.close();

        return unitString;
    }

    /**
     * Returns the code for unit type (mass, volume, item).
     *
     * @param unit to find
     * @param context
     * @return code for type of unit
     */
    public static int getUnitType(int unit, Context context) {
        FoodDbHelper dbHelper = new FoodDbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = new String[] {UnitContract.UnitEntry.COLUMN_TYPE};
        String selection = UnitContract.UnitEntry._ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(unit)};
        Cursor c = db.query(UnitContract.UnitEntry.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
        c.moveToFirst();
        int unitType = c.getInt(c.getColumnIndex(UnitContract.UnitEntry.COLUMN_TYPE));

        c.close();
        db.close();

        return unitType;
    }
}
