package abc.tubes.barangku;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import abc.tubes.barangku.data.FoodContract;
import abc.tubes.barangku.data.FoodDbHelper;
import abc.tubes.barangku.data.UnitContract;

/**
 * Activity to insert and edit food entries.
 *
 * Created by User on 1/4/2018.
 */

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, com.wdullaer.materialdatetimepicker.date.DatePickerDialog.OnDateSetListener {
    private final String LOG_TAG = EditorActivity.class.getName();

    // uri passed to intent when the activity started
    private Uri mUri;

    // database helper
    private FoodDbHelper mDbHelper;

    // member variables to easily access text fields
    private EditText mNameTextView;
    private EditText mAmountTextView;
    private EditText mStoreTextView;
    private EditText mPriceTextView;
    private EditText mExpTextView;
    private ArrayList<EditText> mEditTexts;
    private Spinner mUnitSpinner;
    private ImageView mPhotoView;

    // storage variables for inputs to prevent data loss
    private int mUnit;
    private double mAmount;
    private long mExpDate;
    private String mPhotoPath;

    // add photo button
    private Button mPhotoButton;

    // image request code (arbitrary)
    private static final int IMAGE_REQUEST_CODE = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // get db helper
        mDbHelper = new FoodDbHelper(this);

        // get references to textviews to read data from
        mNameTextView = (EditText) findViewById(R.id.edit_item_name);
        mAmountTextView = (EditText) findViewById(R.id.edit_item_amount);
        mStoreTextView = (EditText) findViewById(R.id.edit_item_store);
        mPriceTextView = (EditText) findViewById(R.id.edit_item_price);
        mExpTextView = (EditText) findViewById(R.id.edit_item_expiration);
        mUnitSpinner = (Spinner) findViewById(R.id.edit_item_unit);
        mPhotoView = (ImageView) findViewById(R.id.edit_item_photo);

        // add text fields to arraylist for easier handling
        // TODO: this may be unnecessary or stupid, check it out
        mEditTexts = new ArrayList<>();
        mEditTexts.add(mNameTextView);
        mEditTexts.add(mAmountTextView);
        mEditTexts.add(mStoreTextView);
        mEditTexts.add(mPriceTextView);
        mEditTexts.add(mExpTextView);

        View.OnClickListener expClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar now = Calendar.getInstance();
                com.wdullaer.materialdatetimepicker.date.DatePickerDialog dpd =
                        com.wdullaer.materialdatetimepicker.date.DatePickerDialog.newInstance(
                                EditorActivity.this,
                                now.get(Calendar.YEAR),
                                now.get(Calendar.MONTH),
                                now.get(Calendar.DAY_OF_MONTH)
                        );
                dpd.show(getFragmentManager(), "Datepickerdialog");
            }
        };

        mExpTextView.setOnClickListener(expClickListener);

        mPhotoButton = (Button) findViewById(R.id.action_add_photo);
        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // start the photo intent
                Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // confirm that we have an app that can take photos
                if (pictureIntent.resolveActivity(getPackageManager()) != null) {
                    // create the file where the photo will go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Error creating file for photo.", e);
                    }
                    // if we have the file
                    if (photoFile != null) {
                        // get the uri from the fileprovider
                        Uri photoURI = FileProvider.getUriForFile(EditorActivity.this, "abc.tubes.barangku.fileprovider", photoFile);
                        pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        // start the camera
                        startActivityForResult(pictureIntent, IMAGE_REQUEST_CODE);
                    }
                }
            }
        });

        setupSpinner();

        // check for a passed uri
        Uri uri = getIntent().getParcelableExtra(FoodContract.FoodEntry.FOOD_URI_KEY);
        if (uri != null) {
            mUri = uri;
            getSupportLoaderManager().initLoader(0, null, this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case R.id.editor_save:
                boolean saved = saveItem();
                if (saved) {
                    finish();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, mUri, null, null, null, null);
    }

    /**
     * Callback triggered when the loader returns with the FoodEntry.
     * Sets all the fields with the retrieved data.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            // set name
            String name = data.getString(data.getColumnIndex(FoodContract.FoodEntry.COLUMN_NAME));
            mNameTextView.setText(name);

            // set units
            mUnit = data.getInt(data.getColumnIndex(FoodContract.FoodEntry.COLUMN_UNIT));
            mUnitSpinner.setSelection(mUnit-1);

            // set amount
            double amount = data.getDouble(data.getColumnIndex(FoodContract.FoodEntry.COLUMN_AMOUNT));
            mAmount = Utils.convert(amount, mUnit, false, this);
            BigDecimal amountBd = new BigDecimal(mAmount).setScale(2, BigDecimal.ROUND_HALF_UP);
            String amountString = amountBd.toString();
            mAmountTextView.setText(amountString);

            // set store
            String store = data.getString(data.getColumnIndex(FoodContract.FoodEntry.COLUMN_STORE));
            if (!TextUtils.isEmpty(store)) {
                mStoreTextView.setText(store);
            }
            // set expiration
            String expString = data.getString(data.getColumnIndex(FoodContract.FoodEntry.COLUMN_EXPIRATION));
            if (!TextUtils.isEmpty(expString)) {
                Calendar now = Calendar.getInstance();
                mExpDate = Long.parseLong(expString);
                now.setTimeInMillis(mExpDate);
                String dateString = (now.get(Calendar.MONTH)+1) + "/" + now.get(Calendar.DAY_OF_MONTH) + "/" + now.get(Calendar.YEAR);
                mExpTextView.setText(dateString);
            }

            // set price per
            String priceString = data.getString(data.getColumnIndex(FoodContract.FoodEntry.COLUMN_PRICE_PER));
            if (!TextUtils.isEmpty(priceString)) {
                // TODO: this could also cause precision loss but I think it's less likely
                double price = Double.parseDouble(priceString);
                Log.d(LOG_TAG, "absolute price before conversion: " + price);
                price = mAmount * Utils.convert(price, mUnit, true, this);
                BigDecimal priceBd = new BigDecimal(price).setScale(2, BigDecimal.ROUND_HALF_UP);
                mPriceTextView.setText(priceBd.toString());
            }

            // set photo
            String photoPath = data.getString(data.getColumnIndex(FoodContract.FoodEntry.COLUMN_PHOTO));
            if (!TextUtils.isEmpty(photoPath)) {
                setPhotoView(photoPath);
                mPhotoButton.setText(R.string.button_photo_change);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // null all the text views
        for (EditText view : mEditTexts) {
            view.setText(null);
        }
    }

    /**
     * Callback triggered when user selects a date on the date picker.
     * Sets the user-facing TextView and the date long for use in db storage.
     */
    @Override
    public void onDateSet(com.wdullaer.materialdatetimepicker.date.DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        String date = (monthOfYear+1)+"/"+dayOfMonth+"/"+year;
        mExpTextView.setText(date);
        Calendar now = Calendar.getInstance();
        now.set(year, monthOfYear, dayOfMonth);
        mExpDate = now.getTimeInMillis();
    }

    /**
     * Callback triggered when the camera intent returns.
     * Sets the photo view and changes the photo button.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            setPhotoView(mPhotoPath);
            mPhotoButton.setText(R.string.button_photo_change);
        }
    }

    /**
     * Helper method to save input values to persistent storage.
     *
     * @return true if item was saved, false otherwise
     */
    private boolean saveItem() {
        // get values from editor fields
        String name = mNameTextView.getText().toString();
        String store = mStoreTextView.getText().toString();
        String priceString = mPriceTextView.getText().toString();
        String amountString = mAmountTextView.getText().toString();
        String expiration = mExpTextView.getText().toString();

        // add values to object
        ContentValues values = new ContentValues();
        // validate and add name
        if (!TextUtils.isEmpty(name)) {
            values.put(FoodContract.FoodEntry.COLUMN_NAME, name);
        } else {
            Toast.makeText(this, "The item must have a name!", Toast.LENGTH_SHORT).show();
            return false;
        }

        // validate amount
        if (TextUtils.isEmpty(amountString)) {
            Toast.makeText(this, "The item must have an amount!", Toast.LENGTH_SHORT).show();
            return false;
        }
        values.put(FoodContract.FoodEntry.COLUMN_UNIT, mUnit);

        if (!TextUtils.isEmpty(expiration)) {
            values.put(FoodContract.FoodEntry.COLUMN_EXPIRATION, mExpDate);
        }

        if (!TextUtils.isEmpty(store)) {
            values.put(FoodContract.FoodEntry.COLUMN_STORE, store);
        }

        if (!TextUtils.isEmpty(mPhotoPath)) {
            values.put(FoodContract.FoodEntry.COLUMN_PHOTO, mPhotoPath);
        }

        // convert numeric values and add to object
        float amount = Float.parseFloat(amountString);
        // make sure that the amount is different, otherwise use cached
        if (Math.abs(amount - mAmount) > 0.005) {
            values.put(FoodContract.FoodEntry.COLUMN_AMOUNT, amount);
        } else {
            values.put(FoodContract.FoodEntry.COLUMN_AMOUNT, mAmount);
        }

        if (!TextUtils.isEmpty(priceString)) {
            float price = Float.parseFloat(priceString);
            values.put(FoodContract.FoodEntry.COLUMN_PRICE_PER, price);
        }

        // insert using contentresolver
        if (mUri == null) {
            getContentResolver().insert(FoodContract.FoodEntry.CONTENT_URI, values);
        } else {
            getContentResolver().update(mUri, values, null, null);
        }

        return true;
    }

    /**
     * Helper method to setup the unit spinner.
     * Retrieves units from UnitDB and populates spinner, and sets click listeners.
     */
    private void setupSpinner() {
        final SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] columns = new String[] {UnitContract.UnitEntry.COLUMN_NAME};
        Cursor cursor = db.query(UnitContract.UnitEntry.TABLE_NAME, columns, null, null, null, null, UnitContract.UnitEntry._ID);
        final ArrayList<String> unitArray = new ArrayList<>();
        while (cursor.moveToNext()) {
            unitArray.add(cursor.getString(cursor.getColumnIndex(UnitContract.UnitEntry.COLUMN_NAME)));
        }
        cursor.close();
        db.close();
        Log.d(LOG_TAG, "First three units: " + unitArray.get(0) + ", " + unitArray.get(1) + ", " + unitArray.get(2));

        // create array adapter for spinner
        ArrayAdapter unitSpinnerAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, unitArray);

        // set dropdown style
        unitSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dialog_item);

        // bind the adapter to the spinner
        mUnitSpinner.setAdapter(unitSpinnerAdapter);

        // set the on item click listener
        mUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                Log.d(LOG_TAG, "Position: " + position + ", ID: " + l);
                mUnit = position + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mUnit = 1;
            }
        });
    }

    /**
     * Helper method to display the image.
     * Decodes the image with the correct orientation and sets the imageView.
     *
     * @param photoPath absolute path to the image
     */
    private void setPhotoView(String photoPath) {
        // TODO: fix this so it decodes after knowing the size and orientation needed, I think via a stream
        // decode image
        Bitmap bitmap = BitmapFactory.decodeFile(photoPath);

        // get image orientation
        try {
            ExifInterface exif = new ExifInterface(photoPath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Matrix matrix = new Matrix();
            switch (orientation) {
                case 6:
                    matrix.postRotate(90);
                    break;
                case 3:
                    matrix.postRotate(180);
                    break;
                case 8:
                    matrix.postRotate(270);
                    break;
            }
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (IOException e) {
            Log.e(LOG_TAG, "problem loading image", e);
        }

        mPhotoView.setImageBitmap(bitmap);
    }

    /**
     * Helper method to create an image file in which to store a photo result.
     *
     * @return empty image file
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        // create a unique file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "FOOD_" + timeStamp;

        // set up the file with the picture directory
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);

        // save the file
        mPhotoPath = imageFile.getAbsolutePath();
        return imageFile;
    }
}
