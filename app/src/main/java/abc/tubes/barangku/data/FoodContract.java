package abc.tubes.barangku.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Specifies the schema of the food database.
 *
 * Created by User on 1/4/2018.
 */

public final class FoodContract {
    // non-instantiable
    private FoodContract() {}

    // base content uri
    public static final String CONTENT_AUTHORITY = "abc.tubes.barangku";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // uri path for food table
    public static final String PATH_FOOD = "food";

    public static class FoodEntry implements BaseColumns {
        // full content uri for food table
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_FOOD);

        // key for parceling
        public static final String FOOD_URI_KEY = "data_food_item";

        // database table name
        public static final String TABLE_NAME = "food";

        // column names
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_UNIT = "unit";
        public static final String COLUMN_AMOUNT = "amount";
        public static final String COLUMN_PRICE_PER = "priceper";
        public static final String COLUMN_STORE = "store";
        public static final String COLUMN_EXPIRATION = "expiration";
        public static final String COLUMN_PHOTO = "photo";
    }
}
