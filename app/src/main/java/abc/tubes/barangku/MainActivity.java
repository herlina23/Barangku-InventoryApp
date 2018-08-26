package abc.tubes.barangku;

import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import abc.tubes.barangku.data.FoodContract;

/**
 * Activity to display the full list of items available.
 *
 * Created by User on 1/4/2018.
 */
public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private FoodCursorAdapter mCursorAdapter;
    private View mEmptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // create the cursor adapter
        mCursorAdapter = new FoodCursorAdapter(this, null);

        // assign the cursor to the listview
        ListView foodList = (ListView) findViewById(R.id.list_view);
        foodList.setAdapter(mCursorAdapter);

        // set empty view
        mEmptyView = findViewById(R.id.empty_view);
        foodList.setEmptyView(mEmptyView);

        // set click listener for listview
        foodList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long id) {
                Uri uri = ContentUris.withAppendedId(FoodContract.FoodEntry.CONTENT_URI, id);
                Intent intent = new Intent(MainActivity.this, FoodViewActivity.class);
                intent.putExtra(FoodContract.FoodEntry.FOOD_URI_KEY, uri);
                startActivity(intent);
            }
        });

        // initialize the loader
        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_delete_all_entries) {
            // show confirmation for delete all entries
            showDeleteDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = new String[] {
                FoodContract.FoodEntry._ID,
                FoodContract.FoodEntry.COLUMN_NAME,
                FoodContract.FoodEntry.COLUMN_AMOUNT,
                FoodContract.FoodEntry.COLUMN_UNIT
        };
        return new CursorLoader(this, FoodContract.FoodEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() > 0) {
            mEmptyView.setVisibility(View.GONE);
        }
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // remove reference to cursor
        mCursorAdapter.swapCursor(null);
    }

    /**
     * Shows the delete dialog when user requests to delete all items.
     */
    private void showDeleteDialog() {
        // get the view
        View view = LayoutInflater.from(this).inflate(R.layout.delete_all_dialog, null);

        // create alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        ((TextView) view.findViewById(R.id.delete_all_dialog_instruct))
                .setText(String.format(getString(R.string.delete_all_dialog_instruct), getString(R.string.confirm_delete_keyword)));
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteAllItems();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) dialogInterface.dismiss();
            }
        });
        final EditText input = (EditText) view.findViewById(R.id.delete_all_prompt_input);
        final AlertDialog dialog = builder.create();

        // show the dialog, with the delete button disabled
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // nothing to do
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // also nothing
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // check if it contains the word "delete"
                String entered = editable.toString().toLowerCase();
                if (entered.equals(getString(R.string.confirm_delete_keyword)))
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                else
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }
        });
    }

    /**
     * Deletes all the items from persistent storage.
     * Invoked from delete dialog.
     */
    private void deleteAllItems() {
        int rowsAffected = getContentResolver().delete(FoodContract.FoodEntry.CONTENT_URI, null, null);
        if (rowsAffected > 0) {
            Toast.makeText(this, "All items deleted.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error deleting items. No changes made.", Toast.LENGTH_SHORT).show();
        }
    }
}
