package abc.tubes.barangku.data;

import android.provider.BaseColumns;

/**
 * Database schema contract for units.
 *
 * Created by User on 1/4/2018.
 */

public final class UnitContract {
    // non-instantiable
    private UnitContract() {}

    public static class UnitEntry implements BaseColumns {
        // database table name
        public static final String TABLE_NAME = "units";

        // column names
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_CONVERT = "conversion";
        public static final String COLUMN_TYPE = "type";

        // constants for unit types
        public static final int UNIT_TYPE_ITEM = 0;
        public static final int UNIT_TYPE_VOLUME = 1;
        public static final int UNIT_TYPE_MASS = 2;

        // constants for specific unit
        public static final double UNIT_ITEM = 1;
        // volume conversions
        public static final double ML_IN_ML = 1;
        public static final double ML_IN_L = 1000.0;
        public static final double ML_IN_FLOZ = 29.5735295625;
        public static final double ML_IN_PINT = 473.176473;
        public static final double ML_IN_QUART = 946.352946;
        public static final double ML_IN_GALLON = 3785.411784;
        public static final double ML_IN_TSP = 4.92892159375;
        public static final double ML_IN_TBSP = 14.78676478125;
        public static final double ML_IN_CUP = 236.5882365;

        // mass conversions
        public static final double G_IN_G = 1;
        public static final double G_IN_MG = 0.001;
        public static final double G_IN_KG = 1000.0;
        public static final double G_IN_OZ = 28.349523125;
        public static final double G_IN_POUND = 453.59237;
    }
}
